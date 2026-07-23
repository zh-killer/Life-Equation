package com.lifeequation.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lifeequation.entity.AiReportInterpretation;
import org.springframework.web.multipart.MultipartFile;

public interface AiReportInterpretationService extends IService<AiReportInterpretation> {
    AiReportInterpretation createInterpretation(Long userId, MultipartFile image, String reportType, String reportData);
    Page<AiReportInterpretation> getHistoryPage(int page, int size, Long userId);
    AiReportInterpretation getDetail(Long id);
    java.util.Map<String, Object> diagnoseVisionStatus();
}
