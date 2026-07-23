package com.lifeequation.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lifeequation.entity.AiHealthAdvice;

public interface AiHealthAdviceService extends IService<AiHealthAdvice> {
    AiHealthAdvice createAdvice(Long userId, AiHealthAdvice input);
    Page<AiHealthAdvice> getHistoryPage(int page, int size, Long userId);
    AiHealthAdvice getDetail(Long id);
}
