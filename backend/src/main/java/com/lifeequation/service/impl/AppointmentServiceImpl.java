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
        appt.setStatus("UNPAID");
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

    @Override
    @Transactional
    public void cancelAppointment(Long id, Long userId) {
        Appointment appt = this.getById(id);
        if (appt == null) {
            throw new RuntimeException("预约不存在");
        }
        if (!appt.getUserId().equals(userId)) {
            throw new RuntimeException("无权取消他人的预约");
        }
        // 先做状态 CAS：只有真正把 UNPAID/PENDING 改成 CANCELLED 的那一次请求才允许退号。
        // 并发双击时其余请求会得到 0，不会重复归还号源（否则号源会被越退越多）。
        int changed = baseMapper.cancelIfActive(id, userId);
        if (changed == 0) {
            if ("COMPLETED".equals(appt.getStatus())) {
                throw new RuntimeException("该预约已完成，无法取消");
            }
            throw new RuntimeException("该预约已取消");
        }
        releaseSlot(appt);
    }

    @Override
    @Transactional
    public void payAppointment(Long id, Long userId) {
        Appointment appt = this.getById(id);
        if (appt == null) {
            throw new RuntimeException("预约不存在");
        }
        if (!appt.getUserId().equals(userId)) {
            throw new RuntimeException("无权支付他人的预约");
        }
        // 状态 CAS：只有 UNPAID 能支付。避免"另一台设备刚取消、这台又支付成功"
        // 导致预约复活但号源已被退回，同一个号被卖两次。
        int changed = baseMapper.payIfUnpaid(id, userId);
        if (changed == 0) {
            if ("CANCELLED".equals(appt.getStatus())) {
                throw new RuntimeException("该预约已取消，无法支付");
            }
            throw new RuntimeException("该预约无需支付");
        }
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

    /** 允许的预约状态，防止后台接口写入任意字符串 */
    private static final java.util.Set<String> VALID_STATUS =
            java.util.Set.of("UNPAID", "PENDING", "CONFIRMED", "COMPLETED", "CANCELLED");

    @Override
    @Transactional
    public void updateAppointmentStatus(Long id, String status) {
        if (status == null || !VALID_STATUS.contains(status)) {
            throw new RuntimeException("非法的预约状态: " + status);
        }
        Appointment a = this.getById(id);
        if (a == null) throw new RuntimeException("预约不存在");
        String old = a.getStatus();
        if (status.equals(old)) return;

        // 显式状态机：已完成是终态，不允许再改；只有未完成状态才能取消
        if ("COMPLETED".equals(old)) {
            throw new RuntimeException("已完成的预约不能再变更状态");
        }
        if ("CANCELLED".equals(status)) {
            if (!"UNPAID".equals(old) && !"PENDING".equals(old) && !"CONFIRMED".equals(old)) {
                throw new RuntimeException("当前状态不可取消");
            }
            releaseSlot(a);
        } else if ("CANCELLED".equals(old)) {
            // 从已取消恢复：需要重新占用号源，占不到就失败
            occupySlot(a);
        }
        a.setStatus(status);
        this.updateById(a);
    }

    /** 按 医生+日期+时段 找到对应排班；找不到说明该排班已被删除 */
    private DoctorSchedule findSchedule(Appointment a) {
        return scheduleMapper.selectOne(
                new LambdaQueryWrapper<DoctorSchedule>()
                        .eq(DoctorSchedule::getDoctorId, a.getDoctorId())
                        .eq(DoctorSchedule::getScheduleDate, a.getAppointmentDate())
                        .eq(DoctorSchedule::getTimeSlot, a.getTimeSlot()));
    }

    private void releaseSlot(Appointment a) {
        DoctorSchedule s = findSchedule(a);
        if (s != null) {
            scheduleMapper.atomicDecrementBookedCount(s.getId());
        }
    }

    private void occupySlot(Appointment a) {
        DoctorSchedule s = findSchedule(a);
        if (s == null) return;
        int updated = scheduleMapper.atomicIncrementBookedCount(s.getId(), s.getMaxSlots());
        if (updated == 0) {
            throw new RuntimeException("该时段号源已满，无法恢复为接诊状态");
        }
    }

    private void populateAppointmentNames(java.util.List<Appointment> list) {
        for (Appointment a : list) {
            Doctor d = doctorMapper.selectById(a.getDoctorId());
            if (d != null) {
                a.setDoctorName(d.getName());
                a.setDepartment(d.getDepartment());
                a.setAmount(d.getConsultationFee());
            }
            Hospital h = hospitalMapper.selectById(a.getHospitalId());
            if (h != null) a.setHospitalName(h.getName());
        }
    }
}
