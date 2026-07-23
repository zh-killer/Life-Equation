package com.lifeequation.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lifeequation.entity.Doctor;

public interface DoctorService extends IService<Doctor> {
    Page<Doctor> getDoctorPage(int page, int size, Long hospitalId, String department, String keyword);
    Doctor getDoctorDetail(Long id);

    // Admin methods
    Page<Doctor> getDoctorPageAdmin(int page, int size, String keyword, Long hospitalId);
    void createDoctor(Doctor doctor);
    void updateDoctor(Long id, Doctor doctor);
    void deleteDoctor(Long id);
    void toggleDoctorStatus(Long id);
}
