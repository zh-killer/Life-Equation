package com.lifeequation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeequation.common.Result;
import com.lifeequation.entity.AiReportInterpretation;
import com.lifeequation.security.SecurityUtils;
import com.lifeequation.service.AiReportInterpretationService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/ai/report")
public class AiReportInterpretationController {

    private final AiReportInterpretationService aiReportService;

    public AiReportInterpretationController(AiReportInterpretationService aiReportService) {
        this.aiReportService = aiReportService;
    }

    @PostMapping("/interpret")
    public Result<AiReportInterpretation> createInterpretation(
            @RequestParam("image") MultipartFile image,
            @RequestParam(required = false, defaultValue = "other") String reportType,
            @RequestParam(required = false, defaultValue = "") String reportData) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (image.isEmpty()) {
            return Result.error("请上传报告图片");
        }
        AiReportInterpretation report = aiReportService.createInterpretation(userId, image, reportType, reportData);
        return Result.ok(report);
    }

    @GetMapping("/interpret/history")
    public Result<Map<String, Object>> getHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<AiReportInterpretation> historyPage = aiReportService.getHistoryPage(page, size, userId);
        return Result.ok(Map.of(
            "records", historyPage.getRecords(),
            "total", historyPage.getTotal(),
            "pages", historyPage.getPages()
        ));
    }

    @GetMapping("/interpret/{id}")
    public Result<AiReportInterpretation> getDetail(@PathVariable Long id) {
        AiReportInterpretation record = aiReportService.getDetail(id);
        Long userId = SecurityUtils.getCurrentUserId();
        if (record == null || !record.getUserId().equals(userId)) {
            return Result.error(403, "无权查看他人记录");
        }
        return Result.ok(record);
    }

    @DeleteMapping("/interpret/{id}")
    public Result<?> deleteInterpretation(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        AiReportInterpretation record = aiReportService.getById(id);
        if (record == null) return Result.error("记录不存在");
        if (!record.getUserId().equals(userId)) return Result.error(403, "无权删除他人记录");
        aiReportService.removeById(id);
        return Result.ok("删除成功");
    }

    /**
     * 诊断接口：测试 DeepSeek 视觉服务连接状态（现场画一张带检验数值的小图发给模型，能读对才算通）
     */
    @GetMapping("/diagnose")
    public Result<?> diagnoseVisionService() {
        return Result.ok(aiReportService.diagnoseVisionStatus());
    }
}
