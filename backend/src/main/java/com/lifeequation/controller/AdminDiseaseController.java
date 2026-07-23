package com.lifeequation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeequation.common.Result;
import com.lifeequation.entity.Disease;
import com.lifeequation.service.DiseaseService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/diseases")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDiseaseController {

    private final DiseaseService diseaseService;

    public AdminDiseaseController(DiseaseService diseaseService) {
        this.diseaseService = diseaseService;
    }

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword) {
        Page<Disease> result = diseaseService.getDiseasePage(page, size, null, keyword);
        return Result.ok(result);
    }

    @PostMapping
    public Result<?> create(@RequestBody Disease disease) {
        diseaseService.createDisease(disease);
        return Result.ok("创建成功");
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Disease disease) {
        diseaseService.updateDisease(id, disease);
        return Result.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        diseaseService.deleteDisease(id);
        return Result.ok("删除成功");
    }
}
