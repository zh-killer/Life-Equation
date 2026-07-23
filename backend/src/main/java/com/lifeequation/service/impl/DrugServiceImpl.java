package com.lifeequation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lifeequation.entity.Drug;
import com.lifeequation.mapper.DrugMapper;
import com.lifeequation.service.DrugService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DrugServiceImpl extends ServiceImpl<DrugMapper, Drug> implements DrugService {

    @Override
    public Page<Drug> getDrugPage(int page, int size, String keyword, String category) {
        LambdaQueryWrapper<Drug> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                .like(Drug::getName, keyword)
                .or().like(Drug::getTradeName, keyword)
                .or().like(Drug::getIndications, keyword)
            );
        }

        if (category != null && !category.isBlank()) {
            wrapper.eq(Drug::getCategory, category);
        }

        wrapper.orderByDesc(Drug::getIsOtc).orderByAsc(Drug::getId);
        return this.page(new Page<>(page, size), wrapper);
    }

    @Override
    public Drug getDrugDetail(Long id) {
        Drug drug = this.getById(id);
        if (drug == null) {
            throw new RuntimeException("药品不存在");
        }
        return drug;
    }

    @Override
    public List<Map<String, Object>> getDrugCategories() {
        List<Drug> drugs = this.list();
        Map<String, Long> categoryMap = drugs.stream()
            .filter(d -> d.getCategory() != null && !d.getCategory().isBlank())
            .collect(Collectors.groupingBy(Drug::getCategory, LinkedHashMap::new, Collectors.counting()));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : categoryMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("count", entry.getValue());
            result.add(item);
        }
        return result;
    }

    // ==================== Admin methods ====================

    @Override
    public void createDrug(Drug drug) {
        this.save(drug);
    }

    @Override
    public void updateDrug(Long id, Drug drug) {
        if (this.getById(id) == null) throw new RuntimeException("药品不存在");
        drug.setId(id);
        this.updateById(drug);
    }

    @Override
    public void deleteDrug(Long id) {
        if (this.getById(id) == null) throw new RuntimeException("药品不存在");
        this.removeById(id);
    }
}
