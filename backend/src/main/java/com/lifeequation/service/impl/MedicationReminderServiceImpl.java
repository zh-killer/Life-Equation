package com.lifeequation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lifeequation.entity.MedicationReminder;
import com.lifeequation.mapper.MedicationReminderMapper;
import com.lifeequation.service.MedicationReminderService;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MedicationReminderServiceImpl extends ServiceImpl<MedicationReminderMapper, MedicationReminder> implements MedicationReminderService {

    @Override
    public List<MedicationReminder> getUserReminders(Long userId) {
        LambdaQueryWrapper<MedicationReminder> w = new LambdaQueryWrapper<>();
        w.eq(MedicationReminder::getUserId, userId).orderByDesc(MedicationReminder::getCreateTime);
        return this.list(w);
    }

    @Override
    public MedicationReminder createReminder(Long userId, MedicationReminder reminder) {
        reminder.setUserId(userId);
        reminder.setIsActive(1);
        this.save(reminder);
        return reminder;
    }

    @Override
    public MedicationReminder updateReminder(Long userId, Long id, MedicationReminder reminder) {
        MedicationReminder existing = this.getById(id);
        if (existing == null || !existing.getUserId().equals(userId))
            throw new RuntimeException("用药提醒不存在");
        reminder.setId(id);
        reminder.setUserId(userId);
        this.updateById(reminder);
        return this.getById(id);
    }

    @Override
    public void deleteReminder(Long userId, Long id) {
        MedicationReminder existing = this.getById(id);
        if (existing == null || !existing.getUserId().equals(userId))
            throw new RuntimeException("用药提醒不存在");
        this.removeById(id);
    }

    @Override
    public void toggleActive(Long userId, Long id) {
        MedicationReminder existing = this.getById(id);
        if (existing == null || !existing.getUserId().equals(userId))
            throw new RuntimeException("用药提醒不存在");
        existing.setIsActive(existing.getIsActive() == 1 ? 0 : 1);
        this.updateById(existing);
    }

    @Override
    public List<MedicationReminder> getDueReminders(Long userId) {
        List<MedicationReminder> all = getUserReminders(userId);
        List<MedicationReminder> due = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);

        for (MedicationReminder r : all) {
            if (r.getIsActive() != 1) continue;
            // 日期范围检查
            if (r.getStartDate() != null && today.isBefore(r.getStartDate())) continue;
            if (r.getEndDate() != null && today.isAfter(r.getEndDate())) continue;
            // 提醒时间检查（±15分钟窗口）
            String times = r.getReminderTimes();
            if (times == null || times.isBlank()) continue;
            for (String t : times.split("[,，]")) {
                try {
                    LocalTime reminderTime = LocalTime.parse(t.trim());
                    long diff = Math.abs(now.toSecondOfDay() - reminderTime.toSecondOfDay());
                    if (diff <= 900) { // 15 minutes = 900 seconds
                        due.add(r);
                        break;
                    }
                } catch (Exception ignored) {}
            }
        }
        return due;
    }
}
