package com.lifeequation.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lifeequation.entity.Consultation;

public interface ConsultationService extends IService<Consultation> {
    Page<Consultation> getPublicPage(int page, int size, String keyword);
    Page<Consultation> getUserPage(int page, int size, Long userId);
    Consultation getDetail(Long id);
    void createQuestion(Consultation c);
    void deleteQuestion(Long id, Long userId);

    // Admin methods
    Page<Consultation> getConsultationPageAdmin(int page, int size, String keyword, String status);
    void deleteConsultationAdmin(Long id);
}
