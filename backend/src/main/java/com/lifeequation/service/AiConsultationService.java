package com.lifeequation.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lifeequation.entity.AiConsultation;

public interface AiConsultationService extends IService<AiConsultation> {
    AiConsultation createConsultation(Long userId, String symptomsInput);
    Page<AiConsultation> getHistoryPage(int page, int size, Long userId);
    AiConsultation getDetail(Long id);
}
