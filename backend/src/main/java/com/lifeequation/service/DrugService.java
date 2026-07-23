package com.lifeequation.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lifeequation.entity.Drug;
import java.util.List;
import java.util.Map;

public interface DrugService extends IService<Drug> {
    Page<Drug> getDrugPage(int page, int size, String keyword, String category);
    Drug getDrugDetail(Long id);
    List<Map<String, Object>> getDrugCategories();

    // Admin methods
    void createDrug(Drug drug);
    void updateDrug(Long id, Drug drug);
    void deleteDrug(Long id);
}
