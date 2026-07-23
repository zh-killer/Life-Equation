package com.lifeequation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lifeequation.entity.Hospital;
import com.lifeequation.mapper.HospitalMapper;
import com.lifeequation.service.HospitalService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HospitalServiceImpl extends ServiceImpl<HospitalMapper, Hospital> implements HospitalService {

    @Override
    public Page<Hospital> getHospitalPage(int page, int size, String keyword, String level, String city) {
        LambdaQueryWrapper<Hospital> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            w.and(q -> q.like(Hospital::getName, keyword).or().like(Hospital::getAddress, keyword));
        }
        if (level != null && !level.isBlank()) {
            w.eq(Hospital::getLevel, level);
        }
        if (city != null && !city.isBlank()) {
            w.eq(Hospital::getCity, city);
        }
        w.eq(Hospital::getStatus, 1).orderByDesc(Hospital::getCreateTime);
        return this.page(new Page<>(page, size), w);
    }

    @Override
    public Hospital getHospitalDetail(Long id) {
        Hospital h = this.getById(id);
        if (h == null) throw new RuntimeException("医院不存在");
        return h;
    }

    @Override
    public List<String> getHospitalCities() {
        // 从已有数据中提取不重复的城市列表
        return this.lambdaQuery()
                .select(Hospital::getCity)
                .eq(Hospital::getStatus, 1)
                .isNotNull(Hospital::getCity)
                .groupBy(Hospital::getCity)
                .list()
                .stream()
                .map(Hospital::getCity)
                .distinct()
                .toList();
    }

    // ==================== Admin methods ====================

    @Override
    public Page<Hospital> getHospitalPageAdmin(int page, int size, String keyword, String level, String city) {
        LambdaQueryWrapper<Hospital> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            w.and(q -> q.like(Hospital::getName, keyword).or().like(Hospital::getAddress, keyword));
        }
        if (level != null && !level.isBlank()) {
            w.eq(Hospital::getLevel, level);
        }
        if (city != null && !city.isBlank()) {
            w.eq(Hospital::getCity, city);
        }
        w.orderByDesc(Hospital::getCreateTime);
        return this.page(new Page<>(page, size), w);
    }

    @Override
    public void createHospital(Hospital hospital) {
        hospital.setStatus(1);
        this.save(hospital);
    }

    @Override
    public void updateHospital(Long id, Hospital hospital) {
        Hospital existing = this.getById(id);
        if (existing == null) throw new RuntimeException("医院不存在");
        hospital.setId(id);
        this.updateById(hospital);
    }

    @Override
    public void deleteHospital(Long id) {
        if (this.getById(id) == null) throw new RuntimeException("医院不存在");
        this.removeById(id);
    }

    @Override
    public void updateHospitalStatus(Long id, Integer status) {
        Hospital h = this.getById(id);
        if (h == null) throw new RuntimeException("医院不存在");
        h.setStatus(status);
        this.updateById(h);
    }
}
