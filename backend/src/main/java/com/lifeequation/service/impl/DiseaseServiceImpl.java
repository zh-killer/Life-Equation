package com.lifeequation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lifeequation.entity.Disease;
import com.lifeequation.mapper.DiseaseMapper;
import com.lifeequation.service.DiseaseService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DiseaseServiceImpl extends ServiceImpl<DiseaseMapper, Disease> implements DiseaseService {

    @Override
    public Page<Disease> getDiseasePage(int page, int size, String category, String keyword) {
        LambdaQueryWrapper<Disease> w = new LambdaQueryWrapper<>();
        if (category != null && !category.isBlank()) w.eq(Disease::getCategory, category);
        if (keyword != null && !keyword.isBlank()) {
            w.and(q -> q.like(Disease::getName, keyword).or().like(Disease::getSymptoms, keyword));
        }
        w.orderByDesc(Disease::getIsCommon).orderByAsc(Disease::getId);
        return this.page(new Page<>(page, size), w);
    }

    @Override
    public Disease getDiseaseDetail(Long id) {
        Disease d = this.getById(id);
        if (d == null) throw new RuntimeException("疾病信息不存在");
        return d;
    }

    @Override
    public List<Map<String, Object>> getDiseaseCategories() {
        // 返回所有分类及每个分类下的疾病数量
        List<Disease> all = this.list();
        Map<String, Long> countMap = new HashMap<>();
        for (Disease d : all) {
            countMap.merge(d.getCategory(), 1L, Long::sum);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Long> e : countMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", e.getKey());
            item.put("count", e.getValue());
            result.add(item);
        }
        return result;
    }

    // ==================== Admin methods ====================

    @Override
    public void createDisease(Disease disease) {
        this.save(disease);
    }

    @Override
    public void updateDisease(Long id, Disease disease) {
        if (this.getById(id) == null) throw new RuntimeException("疾病信息不存在");
        disease.setId(id);
        this.updateById(disease);
    }

    @Override
    public void deleteDisease(Long id) {
        if (this.getById(id) == null) throw new RuntimeException("疾病信息不存在");
        this.removeById(id);
    }
}
