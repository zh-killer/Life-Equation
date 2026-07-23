package com.lifeequation.controller;

import com.lifeequation.common.Result;
import com.lifeequation.entity.MedicationReminder;
import com.lifeequation.security.SecurityUtils;
import com.lifeequation.service.MedicationReminderService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/medication-reminders")
public class MedicationReminderController {

    private final MedicationReminderService medicationReminderService;

    public MedicationReminderController(MedicationReminderService medicationReminderService) {
        this.medicationReminderService = medicationReminderService;
    }

    @GetMapping
    public Result<?> list() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<MedicationReminder> reminders = medicationReminderService.getUserReminders(userId);
        return Result.ok(reminders);
    }

    @PostMapping
    public Result<?> create(@RequestBody MedicationReminder reminder) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.ok(medicationReminderService.createReminder(userId, reminder));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody MedicationReminder reminder) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.ok(medicationReminderService.updateReminder(userId, id, reminder));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        medicationReminderService.deleteReminder(userId, id);
        return Result.ok("删除成功");
    }

    @PutMapping("/{id}/toggle")
    public Result<?> toggle(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        medicationReminderService.toggleActive(userId, id);
        return Result.ok("操作成功");
    }

    @GetMapping("/due")
    public Result<?> due() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.ok(medicationReminderService.getDueReminders(userId));
    }
}
