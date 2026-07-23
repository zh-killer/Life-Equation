package com.lifeequation.controller;

import com.lifeequation.common.Result;
import com.lifeequation.entity.DoctorSchedule;
import com.lifeequation.security.SecurityUtils;
import com.lifeequation.service.DoctorScheduleService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/doctor/schedule")
public class DoctorScheduleController {

    private final DoctorScheduleService scheduleService;

    public DoctorScheduleController(DoctorScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public Result<?> list(@RequestParam int year,
                          @RequestParam int month) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.ok(scheduleService.getMySchedules(userId, year, month));
    }

    @PostMapping
    public Result<?> add(@RequestBody DoctorSchedule schedule) {
        Long userId = SecurityUtils.getCurrentUserId();
        scheduleService.addSchedule(userId, schedule);
        return Result.ok("排班添加成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        scheduleService.deleteSchedule(userId, id);
        return Result.ok("排班已删除");
    }

    @PostMapping("/stop")
    public Result<?> stop(@RequestBody Map<String, String> params) {
        Long userId = SecurityUtils.getCurrentUserId();
        scheduleService.applyStop(userId, params.get("date"), params.get("reason"));
        return Result.ok("停诊申请已提交");
    }
}
