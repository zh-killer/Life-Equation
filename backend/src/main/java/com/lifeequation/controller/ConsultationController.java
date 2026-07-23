package com.lifeequation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeequation.common.Result;
import com.lifeequation.entity.Consultation;
import com.lifeequation.security.SecurityUtils;
import com.lifeequation.service.ConsultationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

    private final ConsultationService consultationService;

    public ConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    @GetMapping
    public Result<?> publicList(@RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) String keyword) {
        Page<Consultation> result = consultationService.getPublicPage(page, size, keyword);
        return Result.ok(result);
    }

    @GetMapping("/my")
    public Result<?> myList(@RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<Consultation> result = consultationService.getUserPage(page, size, userId);
        return Result.ok(result);
    }

    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return Result.ok(consultationService.getDetail(id));
    }

    @PostMapping
    public Result<?> create(@RequestBody Consultation consultation) {
        Long userId = SecurityUtils.getCurrentUserId();
        consultation.setUserId(userId);
        consultationService.createQuestion(consultation);
        return Result.ok(null);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        consultationService.deleteQuestion(id, userId);
        return Result.ok(null);
    }
}
