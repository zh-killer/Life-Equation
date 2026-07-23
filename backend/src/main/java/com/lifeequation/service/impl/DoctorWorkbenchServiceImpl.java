package com.lifeequation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifeequation.dto.DoctorWorkbenchDTO;
import com.lifeequation.entity.Appointment;
import com.lifeequation.entity.Consultation;
import com.lifeequation.entity.Doctor;
import com.lifeequation.entity.User;
import com.lifeequation.mapper.AppointmentMapper;
import com.lifeequation.mapper.ConsultationMapper;
import com.lifeequation.mapper.DoctorMapper;
import com.lifeequation.mapper.UserMapper;
import com.lifeequation.service.DoctorWorkbenchService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class DoctorWorkbenchServiceImpl implements DoctorWorkbenchService {

    private final DoctorMapper doctorMapper;
    private final AppointmentMapper appointmentMapper;
    private final ConsultationMapper consultationMapper;
    private final UserMapper userMapper;

    public DoctorWorkbenchServiceImpl(DoctorMapper doctorMapper, AppointmentMapper appointmentMapper,
                                       ConsultationMapper consultationMapper, UserMapper userMapper) {
        this.doctorMapper = doctorMapper;
        this.appointmentMapper = appointmentMapper;
        this.consultationMapper = consultationMapper;
        this.userMapper = userMapper;
    }

    @Override
    public DoctorWorkbenchDTO getWorkbenchOverview(Long userId) {
        // 1. 通过 userId 找到 doctor 记录
        Doctor doctor = doctorMapper.selectOne(
                new LambdaQueryWrapper<Doctor>().eq(Doctor::getUserId, userId));
        if (doctor == null) throw new RuntimeException("未找到医生信息");
        Long doctorId = doctor.getId();

        LocalDate today = LocalDate.now();
        DoctorWorkbenchDTO dto = new DoctorWorkbenchDTO();

        // 2. 查询该医生的所有有效预约（排除已取消的）
        List<Appointment> todayAppts = appointmentMapper.selectList(
                new LambdaQueryWrapper<Appointment>()
                        .eq(Appointment::getDoctorId, doctorId)
                        .ne(Appointment::getStatus, "CANCELLED")
                        .orderByAsc(Appointment::getAppointmentDate)
                        .orderByAsc(Appointment::getTimeSlot));
        dto.setTodayAppointmentCount(todayAppts.size());
        dto.setIsAvailable(doctor.getIsAvailable());

        // 3. 统计已完成 / 待处理咨询
        List<Consultation> consultations = consultationMapper.selectList(
                new LambdaQueryWrapper<Consultation>()
                        .eq(Consultation::getDoctorId, doctorId));
        int answered = 0, pending = 0;
        for (Consultation c : consultations) {
            if ("ANSWERED".equals(c.getStatus())) answered++;
            else if ("PENDING".equals(c.getStatus())) pending++;
        }
        dto.setCompletedConsultCount(answered);
        dto.setPendingConsultCount(pending);

        // 4. 按日期+时段排序显示
        todayAppts.sort((a, b) -> {
            int dateCmp = a.getAppointmentDate().compareTo(b.getAppointmentDate());
            if (dateCmp != 0) return dateCmp;
            return a.getTimeSlot().compareTo(b.getTimeSlot());
        });
        List<DoctorWorkbenchDTO.TodayPatient> patients = new ArrayList<>();
        for (Appointment a : todayAppts) {
            User u = userMapper.selectById(a.getUserId());
            if (u == null) continue;
            DoctorWorkbenchDTO.TodayPatient tp = new DoctorWorkbenchDTO.TodayPatient();
            tp.setAppointmentId(a.getId());
            tp.setPatientId(u.getId());
            tp.setPatientName(u.getRealName() != null ? u.getRealName() : u.getUsername());
            tp.setPatientPhone(u.getPhone());
            tp.setAppointmentDate(a.getAppointmentDate().toString());
            tp.setTimeSlot(a.getTimeSlot());
            tp.setSymptomDesc(a.getSymptomDesc());
            tp.setStatus(a.getStatus());
            patients.add(tp);
        }
        dto.setTodayPatients(patients);

        return dto;
    }
}
