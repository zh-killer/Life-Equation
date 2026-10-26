package com.lifeequation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeequation.common.Result;
import com.lifeequation.entity.Hospital;
import com.lifeequation.service.HospitalService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hospitals")
public class HospitalController {

    private final HospitalService hospitalService;

    public HospitalController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) String level,
                          @RequestParam(required = false) String city) {
        Page<Hospital> result = hospitalService.getHospitalPage(page, size, keyword, level, city);
        return Result.ok(result);
    }

    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return Result.ok(hospitalService.getHospitalDetail(id));
    }
}
