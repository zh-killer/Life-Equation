package com.lifeequation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeequation.common.Result;
import com.lifeequation.entity.Disease;
import com.lifeequation.service.DiseaseService;
import org.springframework.web.bind.annotation.*;

 @RestController
@RequestMapping("/api/diseases")
public class DiseaseController {

    private final DiseaseService diseaseService;

    public DiseaseController(DiseaseService diseaseService) {
        this.diseaseService = diseaseService;
    }

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String category,
                          @RequestParam(required = false) String keyword) {
        Page<Disease> result = diseaseService.getDiseasePage(page, size, category, keyword);
        return Result.ok(result);
    }

    @GetMapping("/categories")
    public Result<?> categories() {
        return Result.ok(diseaseService.getDiseaseCategories());
    }

    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return Result.ok(diseaseService.getDiseaseDetail(id));
    }
}
