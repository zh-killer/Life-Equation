package com.lifeequation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeequation.common.Result;
import com.lifeequation.entity.Hospital;
import com.lifeequation.service.HospitalService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/hospitals")
@PreAuthorize("hasRole('ADMIN')")
public class AdminHospitalController {

    private final HospitalService hospitalService;

    public AdminHospitalController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) String level,
                          @RequestParam(required = false) String city) {
        Page<Hospital> result = hospitalService.getHospitalPageAdmin(page, size, keyword, level, city);
        return Result.ok(result);
    }

    @PostMapping
    public Result<?> create(@RequestBody Hospital hospital) {
        hospitalService.createHospital(hospital);
        return Result.ok("创建成功");
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Hospital hospital) {
        hospitalService.updateHospital(id, hospital);
        return Result.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        hospitalService.deleteHospital(id);
        return Result.ok("删除成功");
    }

    @PutMapping("/{id}/status")
    public Result<?> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        hospitalService.updateHospitalStatus(id, status);
        return Result.ok(status == 1 ? "已启用" : "已禁用");
    }
}
