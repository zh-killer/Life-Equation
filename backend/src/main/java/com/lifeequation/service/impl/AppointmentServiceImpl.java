package com.lifeequation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lifeequation.entity.Appointment;
import com.lifeequation.entity.Doctor;
import com.lifeequation.entity.DoctorSchedule;
import com.lifeequation.entity.Hospital;
import com.lifeequation.mapper.AppointmentMapper;
import com.lifeequation.mapper.DoctorMapper;
import com.lifeequation.mapper.DoctorScheduleMapper;
import com.lifeequation.mapper.HospitalMapper;
import com.lifeequation.service.AppointmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    private final DoctorMapper doctorMapper;
    private final HospitalMapper hospitalMapper;
    private final DoctorScheduleMapper scheduleMapper;

    public AppointmentServiceImpl(DoctorMapper doctorMapper, HospitalMapper hospitalMapper,
                                   DoctorScheduleMapper scheduleMapper) {
        this.doctorMapper = doctorMapper;
        this.hospitalMapper = hospitalMapper;
        this.scheduleMapper = scheduleMapper;
    }

    @Override
    @Transactional
    public Long createAppointment(Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        Long doctorId = Long.valueOf(params.get("doctorId").toString());
        Long hospitalId = Long.valueOf(params.get("hospitalId").toString());
        LocalDate appointmentDate = LocalDate.parse(params.get("appointmentDate").toString());
        String timeSlot = params.get("timeSlot").toString();

        // 1. 检查医生接诊状态
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null || doctor.getIsAvailable() == 0) {
            throw new RuntimeException("该医生暂不接诊");
        }

        // 2. 查找排班记录
        DoctorSchedule schedule = scheduleMapper.selectOne(
                new LambdaQueryWrapper<DoctorSchedule>()
                        .eq(DoctorSchedule::getDoctorId, doctorId)
                        .eq(DoctorSchedule::getScheduleDate, appointmentDate)
                        .eq(DoctorSchedule::getTimeSlot, timeSlot));

        if (schedule == null) {
            throw new RuntimeException("该时段暂无排班，请选择其他时间");
        }
        if (schedule.getStatus() == 0) {
            throw new RuntimeException("该时段已停诊，无法预约");
        }
        if (schedule.getBookedCount() >= schedule.getMaxSlots()) {
            throw new RuntimeException("该时段号源已满，无法预约");
        }

        // 3. 原子扣减号源（防并发超售）
        int updated = scheduleMapper.atomicIncrementBookedCount(
                schedule.getId(), schedule.getMaxSlots());
        if (updated == 0) {
            throw new RuntimeException("号源已被抢完，请刷新后重试");
        }

        // 4. 创建预约
        Appointment appt = new Appointment();
        appt.setUserId(userId);
        appt.setDoctorId(doctorId);
        appt.setHospitalId(hospitalId);
        appt.setAppointmentDate(appointmentDate);
        appt.setTimeSlot(timeSlot);
        appt.setSymptomDesc(params.getOrDefault("symptomDesc", "").toString());
        appt.setStatus("PENDING");
        this.save(appt);
        return appt.getId();
    }

    @Override
    public Page<Appointment> getUserAppointments(Long userId, int page, int size) {
        LambdaQueryWrapper<Appointment> w = new LambdaQueryWrapper<>();
        w.eq(Appointment::getUserId, userId).orderByDesc(Appointment::getCreateTime);
        Page<Appointment> result = this.page(new Page<>(page, size), w);
        populateAppointmentNames(result.getRecords());
        return result;
    }

    // ==================== Admin methods ====================

    @Override
    public Page<Appointment> getAppointmentPageAdmin(int page, int size, String status) {
        LambdaQueryWrapper<Appointment> w = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) {
            w.eq(Appointment::getStatus, status);
        }
        w.orderByDesc(Appointment::getCreateTime);
        Page<Appointment> result = this.page(new Page<>(page, size), w);
        populateAppointmentNames(result.getRecords());
        return result;
    }

    @Override
    public void updateAppointmentStatus(Long id, String status) {
        Appointment a = this.getById(id);
        if (a == null) throw new RuntimeException("预约不存在");
        a.setStatus(status);
        this.updateById(a);
    }

    private void populateAppointmentNames(java.util.List<Appointment> list) {
        for (Appointment a : list) {
            Doctor d = doctorMapper.selectById(a.getDoctorId());
            if (d != null) {
                a.setDoctorName(d.getName());
                a.setDepartment(d.getDepartment());
            }
            Hospital h = hospitalMapper.selectById(a.getHospitalId());
            if (h != null) a.setHospitalName(h.getName());
        }
    }
}
