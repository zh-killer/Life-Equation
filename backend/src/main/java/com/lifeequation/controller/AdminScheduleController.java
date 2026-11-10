package com.lifeequation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifeequation.common.Result;
import com.lifeequation.entity.DoctorSchedule;
import com.lifeequation.mapper.DoctorScheduleMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * 管理端排班管理接口。
 *
 * 与医生端的区别：
 *   - 管理员可以改门诊类型（影响挂号费与号表）
 *   - 管理员可以把号源上限设得更大（不再受 50 的限制）
 *   - 管理员可以整条停诊 / 恢复
 *   - 已预约数仍然只读，任何角色都不能手改
 */
@RestController
@RequestMapping("/api/admin/schedules")
public class AdminScheduleController {

    private final DoctorScheduleMapper scheduleMapper;

    public AdminScheduleController(DoctorScheduleMapper scheduleMapper) {
        this.scheduleMapper = scheduleMapper;
    }

    private int toInt(Object v, int def) {
        if (v == null) return def;
        try {
            return Integer.parseInt(String.valueOf(v).trim());
        } catch (Exception e) {
            throw new RuntimeException("数值格式不正确");
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> list(@RequestParam Long doctorId,
                          @RequestParam int year,
                          @RequestParam int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        List<DoctorSchedule> list = scheduleMapper.selectList(
                new LambdaQueryWrapper<DoctorSchedule>()
                        .eq(DoctorSchedule::getDoctorId, doctorId)
                        .between(DoctorSchedule::getScheduleDate, start, end)
                        .orderByAsc(DoctorSchedule::getScheduleDate)
                        .orderByAsc(DoctorSchedule::getTimeSlot));
        return Result.ok(list);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Result<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        DoctorSchedule s = scheduleMapper.selectById(id);
        if (s == null) throw new RuntimeException("排班不存在");

        int booked = s.getBookedCount() == null ? 0 : s.getBookedCount();

        if (body.containsKey("type")) {
            String t = String.valueOf(body.get("type")).trim();
            if (!t.isEmpty()) {
                if (!t.equals("普通门诊") && !t.equals("专家门诊") && !t.equals("特需门诊")) {
                    throw new RuntimeException("门诊类型不合法");
                }
                s.setType(t);
            }
        }

        int maxSlots = s.getMaxSlots() == null ? 20 : s.getMaxSlots();
        if (body.containsKey("maxSlots")) {
            maxSlots = toInt(body.get("maxSlots"), maxSlots);
        }
        if (maxSlots < booked) {
            throw new RuntimeException("号源上限不能小于已预约数（当前 " + booked + "）");
        }
        if (maxSlots > 500) {
            throw new RuntimeException("号源上限不能超过 500");
        }
        s.setMaxSlots(maxSlots);

        if (body.containsKey("status")) {
            int st = toInt(body.get("status"), s.getStatus() == null ? 1 : s.getStatus());
            s.setStatus(st == 0 ? 0 : 1);
        }

        scheduleMapper.updateById(s);
        return Result.ok("排班已更新");
    }
}
