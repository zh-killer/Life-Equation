package com.lifeequation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeequation.common.Result;
import com.lifeequation.entity.AiConsultation;
import com.lifeequation.security.SecurityUtils;
import com.lifeequation.service.AiConsultationService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiConsultationController {

    private final AiConsultationService aiConsultationService;

    public AiConsultationController(AiConsultationService aiConsultationService) {
        this.aiConsultationService = aiConsultationService;
    }

    @PostMapping("/consultation")
    public Result<AiConsultation> createConsultation(@RequestBody Map<String, String> body) {
        String symptoms = body.get("symptoms");
        if (symptoms == null || symptoms.isBlank()) {
            return Result.error("请输入症状描述");
        }
        Long userId = SecurityUtils.getCurrentUserId();
        AiConsultation consultation = aiConsultationService.createConsultation(userId, symptoms);
        return Result.ok(consultation);
    }

    @GetMapping("/consultation/history")
    public Result<Map<String, Object>> getHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<AiConsultation> historyPage = aiConsultationService.getHistoryPage(page, size, userId);
        return Result.ok(Map.of(
            "records", historyPage.getRecords(),
            "total", historyPage.getTotal(),
            "pages", historyPage.getPages()
        ));
    }

    @GetMapping("/consultation/{id}")
    public Result<AiConsultation> getDetail(@PathVariable Long id) {
        return Result.ok(aiConsultationService.getDetail(id));
    }

    @DeleteMapping("/consultation/{id}")
    public Result<?> deleteConsultation(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        AiConsultation record = aiConsultationService.getById(id);
        if (record == null) return Result.error("记录不存在");
        if (!record.getUserId().equals(userId)) return Result.error(403, "无权删除他人记录");
        aiConsultationService.removeById(id);
        return Result.ok("删除成功");
    }
}
