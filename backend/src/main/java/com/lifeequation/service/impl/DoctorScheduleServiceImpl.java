package com.lifeequation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lifeequation.entity.Doctor;
import com.lifeequation.entity.DoctorSchedule;
import com.lifeequation.mapper.DoctorMapper;
import com.lifeequation.mapper.DoctorScheduleMapper;
import com.lifeequation.service.DoctorScheduleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class DoctorScheduleServiceImpl implements DoctorScheduleService {

    private final DoctorScheduleMapper scheduleMapper;
    private final DoctorMapper doctorMapper;

    public DoctorScheduleServiceImpl(DoctorScheduleMapper scheduleMapper, DoctorMapper doctorMapper) {
        this.scheduleMapper = scheduleMapper;
        this.doctorMapper = doctorMapper;
    }

    private Long getDoctorId(Long userId) {
        Doctor doctor = doctorMapper.selectOne(
                new LambdaQueryWrapper<Doctor>().eq(Doctor::getUserId, userId));
        if (doctor == null) throw new RuntimeException("未找到医生信息");
        return doctor.getId();
    }

    @Override
    public List<DoctorSchedule> getMySchedules(Long userId, int year, int month) {
        Long doctorId = getDoctorId(userId);
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        return scheduleMapper.selectList(new LambdaQueryWrapper<DoctorSchedule>()
                .eq(DoctorSchedule::getDoctorId, doctorId)
                .between(DoctorSchedule::getScheduleDate, start, end)
                .orderByAsc(DoctorSchedule::getScheduleDate)
                .orderByAsc(DoctorSchedule::getTimeSlot));
    }

    @Override
    public List<DoctorSchedule> getDoctorSchedulesPublic(Long doctorId, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        return scheduleMapper.selectList(new LambdaQueryWrapper<DoctorSchedule>()
                .eq(DoctorSchedule::getDoctorId, doctorId)
                .between(DoctorSchedule::getScheduleDate, start, end)
                .orderByAsc(DoctorSchedule::getScheduleDate)
                .orderByAsc(DoctorSchedule::getTimeSlot));
    }

    @Override
    public void addSchedule(Long userId, DoctorSchedule schedule) {
        Long doctorId = getDoctorId(userId);
        schedule.setDoctorId(doctorId);

        // 校验：只能排当月
        LocalDate now = LocalDate.now();
        YearMonth thisMonth = YearMonth.from(now);
        YearMonth scheduleMonth = YearMonth.from(schedule.getScheduleDate());
        if (!scheduleMonth.equals(thisMonth)) {
            throw new RuntimeException("只能排当月班次（" + now.getMonthValue() + "月）");
        }

        // 校验：不能重复（同医生+同日期+同时段）
        Long count = scheduleMapper.selectCount(new LambdaQueryWrapper<DoctorSchedule>()
                .eq(DoctorSchedule::getDoctorId, doctorId)
                .eq(DoctorSchedule::getScheduleDate, schedule.getScheduleDate())
                .eq(DoctorSchedule::getTimeSlot, schedule.getTimeSlot()));
        if (count > 0) {
            throw new RuntimeException("该日期时段已有排班，请勿重复添加");
        }

        if (schedule.getMaxSlots() == null || schedule.getMaxSlots() < 1) {
            schedule.setMaxSlots(20);
        }
        schedule.setBookedCount(0);
        schedule.setStatus(1);
        scheduleMapper.insert(schedule);
    }

    @Override
    public void deleteSchedule(Long userId, Long scheduleId) {
        Long doctorId = getDoctorId(userId);
        DoctorSchedule s = scheduleMapper.selectById(scheduleId);
        if (s == null || !s.getDoctorId().equals(doctorId)) {
            throw new RuntimeException("无权删除此排班");
        }
        scheduleMapper.deleteById(scheduleId);
    }

    @Override
    @Transactional
    public void applyStop(Long userId, String date, String reason) {
        Long doctorId = getDoctorId(userId);
        LocalDate stopDate = LocalDate.parse(date);
        // 只更新 status。不能用整实体 updateById：它会把实体里读出来的旧 booked_count
        // 一起写回数据库，覆盖掉并发中刚 +1 的计数，导致该时段能卖出超过 max_slots 个号。
        scheduleMapper.update(null, new LambdaUpdateWrapper<DoctorSchedule>()
                .eq(DoctorSchedule::getDoctorId, doctorId)
                .eq(DoctorSchedule::getScheduleDate, stopDate)
                .set(DoctorSchedule::getStatus, 0));
    }
}
