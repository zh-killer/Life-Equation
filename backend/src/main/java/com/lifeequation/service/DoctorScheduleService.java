package com.lifeequation.service;

import com.lifeequation.entity.DoctorSchedule;
import java.util.List;

public interface DoctorScheduleService {
    List<DoctorSchedule> getMySchedules(Long userId, int year, int month);
    List<DoctorSchedule> getDoctorSchedulesPublic(Long doctorId, int year, int month);
    void addSchedule(Long userId, DoctorSchedule schedule);
    void deleteSchedule(Long userId, Long scheduleId);
    void applyStop(Long userId, String date, String reason);
}
