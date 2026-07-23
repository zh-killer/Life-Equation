package com.lifeequation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifeequation.common.Result;
import com.lifeequation.entity.*;
import com.lifeequation.entity.User;
import com.lifeequation.mapper.*;
import com.lifeequation.security.SecurityUtils;
import com.lifeequation.service.DoctorWorkbenchService;
import com.lifeequation.service.ReviewService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/doctor")
@PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
public class DoctorWorkbenchController {

    private final DoctorWorkbenchService workbenchService;
    private final DoctorMapper doctorMapper;
    private final ConsultationMapper consultationMapper;
    private final HealthRecordMapper healthRecordMapper;
    private final AppointmentMapper appointmentMapper;
    private final UserMapper userMapper;
    private final ReviewService reviewService;

    public DoctorWorkbenchController(DoctorWorkbenchService workbenchService,
                                      DoctorMapper doctorMapper,
                                      ConsultationMapper consultationMapper,
                                      HealthRecordMapper healthRecordMapper,
                                      AppointmentMapper appointmentMapper,
                                      UserMapper userMapper,
                                      ReviewService reviewService) {
        this.workbenchService = workbenchService;
        this.doctorMapper = doctorMapper;
        this.consultationMapper = consultationMapper;
        this.healthRecordMapper = healthRecordMapper;
        this.appointmentMapper = appointmentMapper;
        this.userMapper = userMapper;
        this.reviewService = reviewService;
    }

    // ===== 工作台首页 =====
    @GetMapping("/workbench")
    public Result<?> workbench() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.ok(workbenchService.getWorkbenchOverview(userId));
    }

    // ===== 一键停诊/恢复 =====
    @PutMapping("/toggle-availability")
    public Result<?> toggleAvailability() {
        Long userId = SecurityUtils.getCurrentUserId();
        Doctor doctor = doctorMapper.selectOne(
                new LambdaQueryWrapper<Doctor>().eq(Doctor::getUserId, userId));
        if (doctor == null) return Result.error("未找到医生信息");
        int newStatus = (doctor.getIsAvailable() != null && doctor.getIsAvailable() == 1) ? 0 : 1;
        doctor.setIsAvailable(newStatus);
        doctorMapper.updateById(doctor);
        return Result.ok(newStatus == 1 ? "已恢复接诊" : "已暂停接诊");
    }

    // ===== 查看患者病历 =====
    @GetMapping("/patient/{patientId}/records")
    public Result<?> patientRecords(@PathVariable Long patientId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Doctor doctor = doctorMapper.selectOne(
                new LambdaQueryWrapper<Doctor>().eq(Doctor::getUserId, userId));
        if (doctor == null) return Result.error("无权访问");

        return Result.ok(healthRecordMapper.selectList(
                new LambdaQueryWrapper<HealthRecord>()
                        .eq(HealthRecord::getUserId, patientId)
                        .eq(HealthRecord::getIsPublic, 1)
                        .orderByDesc(HealthRecord::getRecordDate)));
    }

    // ===== 确认就诊完成 =====
    @PutMapping("/complete/{appointmentId}")
    public Result<?> completeAppointment(@PathVariable Long appointmentId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Doctor doctor = doctorMapper.selectOne(
                new LambdaQueryWrapper<Doctor>().eq(Doctor::getUserId, userId));
        if (doctor == null) return Result.error("无权操作");

        Appointment appt = appointmentMapper.selectById(appointmentId);
        if (appt == null || !appt.getDoctorId().equals(doctor.getId())) {
            return Result.error("无权操作此预约");
        }
        appt.setStatus("COMPLETED");
        appointmentMapper.updateById(appt);
        return Result.ok("已标记为就诊完成");
    }

    // ===== 我的咨询列表 =====
    @GetMapping("/my-consultations")
    public Result<?> myConsultations(@RequestParam(defaultValue = "PENDING") String status) {
        Long userId = SecurityUtils.getCurrentUserId();
        Doctor doctor = doctorMapper.selectOne(
                new LambdaQueryWrapper<Doctor>().eq(Doctor::getUserId, userId));
        if (doctor == null) return Result.error("未找到医生信息");
        java.util.List<Consultation> list = consultationMapper.selectList(
                new LambdaQueryWrapper<Consultation>()
                        .eq(Consultation::getDoctorId, doctor.getId())
                        .eq(Consultation::getStatus, status)
                        .orderByDesc(Consultation::getCreateTime));
        // 填充用户名
        for (Consultation c : list) {
            if (c.getUserId() != null) {
                User u = userMapper.selectById(c.getUserId());
                if (u != null) c.setUserName(u.getRealName() != null ? u.getRealName() : u.getUsername());
            }
        }
        return Result.ok(list);
    }

    // ===== 回复咨询 =====
    @PutMapping("/reply-consultation/{id}")
    public Result<?> replyConsultation(@PathVariable Long id,
                                        @RequestBody Map<String, String> params) {
        Long userId = SecurityUtils.getCurrentUserId();
        Doctor doctor = doctorMapper.selectOne(
                new LambdaQueryWrapper<Doctor>().eq(Doctor::getUserId, userId));
        if (doctor == null) return Result.error("无权操作");

        Consultation c = consultationMapper.selectById(id);
        if (c == null || !c.getDoctorId().equals(doctor.getId())) {
            return Result.error("无权回复此咨询");
        }
        c.setAnswer(params.get("answer"));
        c.setStatus("ANSWERED");
        c.setAnsweredAt(LocalDateTime.now());
        consultationMapper.updateById(c);
        return Result.ok("回复成功");
    }

    // ===== 查看我的评价 =====
    @GetMapping("/reviews")
    public Result<?> myReviews(@RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        Doctor doctor = doctorMapper.selectOne(
                new LambdaQueryWrapper<Doctor>().eq(Doctor::getUserId, userId));
        if (doctor == null) return Result.error("未找到医生信息");
        return Result.ok(reviewService.getTargetReviews("doctor", doctor.getId(), page, size));
    }
}
