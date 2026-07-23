package com.lifeequation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeequation.common.Result;
import com.lifeequation.entity.Doctor;
import com.lifeequation.service.DoctorService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/doctors")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDoctorController {

    private final DoctorService doctorService;

    public AdminDoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) Long hospitalId) {
        Page<Doctor> result = doctorService.getDoctorPageAdmin(page, size, keyword, hospitalId);
        return Result.ok(result);
    }

    @PostMapping
    public Result<?> create(@RequestBody Doctor doctor) {
        doctorService.createDoctor(doctor);
        return Result.ok("创建成功");
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Doctor doctor) {
        doctorService.updateDoctor(id, doctor);
        return Result.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return Result.ok("删除成功");
    }

    @PutMapping("/{id}/status")
    public Result<?> toggleStatus(@PathVariable Long id) {
        doctorService.toggleDoctorStatus(id);
        return Result.ok("状态已切换");
    }
}
