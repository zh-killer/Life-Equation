package com.lifeequation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeequation.common.Result;
import com.lifeequation.entity.Consultation;
import com.lifeequation.service.ConsultationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/consultations")
@PreAuthorize("hasRole('ADMIN')")
public class AdminConsultationController {

    private final ConsultationService consultationService;

    public AdminConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) String status) {
        Page<Consultation> result = consultationService.getConsultationPageAdmin(page, size, keyword, status);
        return Result.ok(result);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        consultationService.deleteConsultationAdmin(id);
        return Result.ok("删除成功");
    }
}
