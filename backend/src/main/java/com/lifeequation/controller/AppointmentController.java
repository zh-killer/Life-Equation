package com.lifeequation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeequation.common.Result;
import com.lifeequation.entity.Appointment;
import com.lifeequation.security.SecurityUtils;
import com.lifeequation.service.AppointmentService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public Result<?> create(@RequestBody Map<String, Object> params) {
        params.put("userId", SecurityUtils.getCurrentUserId());
        Long id = appointmentService.createAppointment(params);
        return Result.ok(id);
    }

    @GetMapping("/user")
    public Result<?> myAppointments(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<Appointment> result = appointmentService.getUserAppointments(userId, page, size);
        return Result.ok(result);
    }

    /** 取消预约并归还号源 */
    @PutMapping("/{id}/cancel")
    public Result<?> cancel(@PathVariable Long id) {
        appointmentService.cancelAppointment(id, SecurityUtils.getCurrentUserId());
        return Result.ok();
    }

    /** 模拟支付挂号费 */
    @PutMapping("/{id}/pay")
    public Result<?> pay(@PathVariable Long id) {
        appointmentService.payAppointment(id, SecurityUtils.getCurrentUserId());
        return Result.ok();
    }
}
