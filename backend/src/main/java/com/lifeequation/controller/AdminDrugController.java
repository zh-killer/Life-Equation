package com.lifeequation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeequation.common.Result;
import com.lifeequation.entity.Drug;
import com.lifeequation.service.DrugService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/drugs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDrugController {

    private final DrugService drugService;

    public AdminDrugController(DrugService drugService) {
        this.drugService = drugService;
    }

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword) {
        Page<Drug> result = drugService.getDrugPage(page, size, keyword, null);
        return Result.ok(result);
    }

    @PostMapping
    public Result<?> create(@RequestBody Drug drug) {
        drugService.createDrug(drug);
        return Result.ok("创建成功");
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Drug drug) {
        drugService.updateDrug(id, drug);
        return Result.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        drugService.deleteDrug(id);
        return Result.ok("删除成功");
    }
}
