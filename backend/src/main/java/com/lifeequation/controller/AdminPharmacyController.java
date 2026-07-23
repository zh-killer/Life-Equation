package com.lifeequation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeequation.common.Result;
import com.lifeequation.entity.Pharmacy;
import com.lifeequation.service.PharmacyService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/pharmacies")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPharmacyController {

    private final PharmacyService pharmacyService;

    public AdminPharmacyController(PharmacyService pharmacyService) {
        this.pharmacyService = pharmacyService;
    }

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword) {
        Page<Pharmacy> result = pharmacyService.getPharmacyPageAdmin(page, size, keyword);
        return Result.ok(result);
    }

    @PostMapping
    public Result<?> create(@RequestBody Pharmacy pharmacy) {
        pharmacyService.createPharmacy(pharmacy);
        return Result.ok("创建成功");
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Pharmacy pharmacy) {
        pharmacyService.updatePharmacy(id, pharmacy);
        return Result.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        pharmacyService.deletePharmacy(id);
        return Result.ok("删除成功");
    }

    @PutMapping("/{id}/status")
    public Result<?> toggleStatus(@PathVariable Long id) {
        pharmacyService.togglePharmacyStatus(id);
        return Result.ok("状态已切换");
    }
}
