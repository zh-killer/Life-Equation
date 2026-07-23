package com.lifeequation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lifeequation.entity.MedicationReminder;
import java.util.List;

public interface MedicationReminderService extends IService<MedicationReminder> {
    List<MedicationReminder> getUserReminders(Long userId);
    MedicationReminder createReminder(Long userId, MedicationReminder reminder);
    MedicationReminder updateReminder(Long userId, Long id, MedicationReminder reminder);
    void deleteReminder(Long userId, Long id);
    void toggleActive(Long userId, Long id);
    List<MedicationReminder> getDueReminders(Long userId);
}
