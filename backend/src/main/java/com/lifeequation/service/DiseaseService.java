package com.lifeequation.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lifeequation.entity.Disease;

import java.util.List;
import java.util.Map;

public interface DiseaseService extends IService<Disease> {
    Page<Disease> getDiseasePage(int page, int size, String category, String keyword);
    Disease getDiseaseDetail(Long id);
    List<Map<String, Object>> getDiseaseCategories();

    // Admin methods
    void createDisease(Disease disease);
    void updateDisease(Long id, Disease disease);
    void deleteDisease(Long id);
}
