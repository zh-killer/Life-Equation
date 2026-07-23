package com.lifeequation.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lifeequation.entity.Appointment;

import java.util.Map;

public interface AppointmentService extends IService<Appointment> {

    Long createAppointment(Map<String, Object> params);

    Page<Appointment> getUserAppointments(Long userId, int page, int size);

    // Admin methods
    Page<Appointment> getAppointmentPageAdmin(int page, int size, String status);
    void updateAppointmentStatus(Long id, String status);
}
