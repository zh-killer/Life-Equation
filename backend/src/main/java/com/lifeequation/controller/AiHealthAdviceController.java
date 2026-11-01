package com.lifeequation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeequation.common.Result;
import com.lifeequation.entity.AiHealthAdvice;
import com.lifeequation.security.SecurityUtils;
import com.lifeequation.service.AiHealthAdviceService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai/health-advice")
public class AiHealthAdviceController {

    private final AiHealthAdviceService aiHealthAdviceService;

    public AiHealthAdviceController(AiHealthAdviceService aiHealthAdviceService) {
        this.aiHealthAdviceService = aiHealthAdviceService;
    }

    @PostMapping
    public Result<AiHealthAdvice> createAdvice(@RequestBody AiHealthAdvice input) {
        Long userId = SecurityUtils.getCurrentUserId();
        AiHealthAdvice advice = aiHealthAdviceService.createAdvice(userId, input);
        return Result.ok(advice);
    }

    @GetMapping("/history")
    public Result<Map<String, Object>> getHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<AiHealthAdvice> historyPage = aiHealthAdviceService.getHistoryPage(page, size, userId);
        return Result.ok(Map.of(
            "records", historyPage.getRecords(),
            "total", historyPage.getTotal(),
            "pages", historyPage.getPages()
        ));
    }

    @GetMapping("/{id}")
    public Result<AiHealthAdvice> getDetail(@PathVariable Long id) {
        AiHealthAdvice record = aiHealthAdviceService.getDetail(id);
        Long userId = SecurityUtils.getCurrentUserId();
        if (record == null || !record.getUserId().equals(userId)) {
            return Result.error(403, "无权查看他人记录");
        }
        return Result.ok(record);
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteAdvice(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        AiHealthAdvice record = aiHealthAdviceService.getById(id);
        if (record == null) return Result.error("记录不存在");
        if (!record.getUserId().equals(userId)) return Result.error(403, "无权删除他人记录");
        aiHealthAdviceService.removeById(id);
        return Result.ok("删除成功");
    }
}
