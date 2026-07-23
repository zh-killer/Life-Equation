package com.lifeequation.controller;

import com.lifeequation.common.Result;
import com.lifeequation.mapper.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final UserMapper userMapper;
    private final DoctorMapper doctorMapper;
    private final HospitalMapper hospitalMapper;
    private final AppointmentMapper appointmentMapper;
    private final ConsultationMapper consultationMapper;
    private final ArticleMapper articleMapper;
    private final DrugMapper drugMapper;
    private final DiseaseMapper diseaseMapper;
    private final PharmacyMapper pharmacyMapper;

    public AdminDashboardController(UserMapper userMapper,
                                     DoctorMapper doctorMapper,
                                     HospitalMapper hospitalMapper,
                                     AppointmentMapper appointmentMapper,
                                     ConsultationMapper consultationMapper,
                                     ArticleMapper articleMapper,
                                     DrugMapper drugMapper,
                                     DiseaseMapper diseaseMapper,
                                     PharmacyMapper pharmacyMapper) {
        this.userMapper = userMapper;
        this.doctorMapper = doctorMapper;
        this.hospitalMapper = hospitalMapper;
        this.appointmentMapper = appointmentMapper;
        this.consultationMapper = consultationMapper;
        this.articleMapper = articleMapper;
        this.drugMapper = drugMapper;
        this.diseaseMapper = diseaseMapper;
        this.pharmacyMapper = pharmacyMapper;
    }

    @GetMapping
    public Result<?> dashboard() {
        Map<String, Object> data = new HashMap<>();

        data.put("totalUsers", userMapper.selectCount(null));
        data.put("totalDoctors", doctorMapper.selectCount(null));
        data.put("totalHospitals", hospitalMapper.selectCount(null));
        data.put("totalArticles", articleMapper.selectCount(null));
        data.put("totalDrugs", drugMapper.selectCount(null));
        data.put("totalDiseases", diseaseMapper.selectCount(null));
        data.put("totalPharmacies", pharmacyMapper.selectCount(null));

        // 今日预约数
        data.put("todayAppointments", appointmentMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.lifeequation.entity.Appointment>()
                        .eq(com.lifeequation.entity.Appointment::getAppointmentDate, LocalDate.now())));

        // 待处理咨询数
        data.put("pendingConsultations", consultationMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.lifeequation.entity.Consultation>()
                        .eq(com.lifeequation.entity.Consultation::getStatus, "PENDING")));

        return Result.ok(data);
    }
}
