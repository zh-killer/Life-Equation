package com.lifeequation.controller;

import com.lifeequation.common.Result;
import com.lifeequation.service.DoctorScheduleService;
import org.springframework.web.bind.annotation.*;

/**
 * 患者端排班查询（公开接口，无需医生角色）
 * 路径 /api/doctors/{doctorId}/schedules 自动匹配 SecurityConfig 中
 * HttpMethod.GET "/api/doctors/**" 的公开规则
 */
@RestController
@RequestMapping("/api/doctors")
public class DoctorSchedulePublicController {

    private final DoctorScheduleService scheduleService;

    public DoctorSchedulePublicController(DoctorScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("/{doctorId}/schedules")
    public Result<?> list(@PathVariable Long doctorId,
                          @RequestParam int year,
                          @RequestParam int month) {
        return Result.ok(scheduleService.getDoctorSchedulesPublic(doctorId, year, month));
    }
}
