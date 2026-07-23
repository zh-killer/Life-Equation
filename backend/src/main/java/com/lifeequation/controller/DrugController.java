package com.lifeequation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeequation.common.Result;
import com.lifeequation.entity.Drug;
import com.lifeequation.service.DrugService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drugs")
public class DrugController {

    private final DrugService drugService;

    public DrugController(DrugService drugService) {
        this.drugService = drugService;
    }

    @GetMapping
    public Result<Map<String, Object>> getDrugs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        Page<Drug> drugPage = drugService.getDrugPage(page, size, keyword, category);
        return Result.ok(Map.of(
            "records", drugPage.getRecords(),
            "total", drugPage.getTotal(),
            "pages", drugPage.getPages()
        ));
    }

    @GetMapping("/categories")
    public Result<List<Map<String, Object>>> getCategories() {
        return Result.ok(drugService.getDrugCategories());
    }

    @GetMapping("/{id}")
    public Result<Drug> getDetail(@PathVariable Long id) {
        return Result.ok(drugService.getDrugDetail(id));
    }
}
