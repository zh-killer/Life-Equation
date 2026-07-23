package com.lifeequation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeequation.common.Result;
import com.lifeequation.entity.Pharmacy;
import com.lifeequation.service.PharmacyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pharmacies")
public class PharmacyController {

    private final PharmacyService pharmacyService;

    public PharmacyController(PharmacyService pharmacyService) {
        this.pharmacyService = pharmacyService;
    }

    @GetMapping
    public Result<Map<String, Object>> getPharmacies(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Integer is24h) {
        Page<Pharmacy> pharmacyPage = pharmacyService.getPharmacyPage(page, size, keyword, city, is24h);
        return Result.ok(Map.of(
            "records", pharmacyPage.getRecords(),
            "total", pharmacyPage.getTotal(),
            "pages", pharmacyPage.getPages()
        ));
    }

    @GetMapping("/cities")
    public Result<List<String>> getCities() {
        return Result.ok(pharmacyService.getPharmacyCities());
    }

    @GetMapping("/{id}")
    public Result<Pharmacy> getDetail(@PathVariable Long id) {
        return Result.ok(pharmacyService.getPharmacyDetail(id));
    }
}
