package com.lifeequation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lifeequation.entity.Pharmacy;
import com.lifeequation.mapper.PharmacyMapper;
import com.lifeequation.service.PharmacyService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PharmacyServiceImpl extends ServiceImpl<PharmacyMapper, Pharmacy> implements PharmacyService {

    @Override
    public Page<Pharmacy> getPharmacyPage(int page, int size, String keyword, String city, Integer is24h) {
        LambdaQueryWrapper<Pharmacy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Pharmacy::getStatus, 1);

        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                .like(Pharmacy::getName, keyword)
                .or().like(Pharmacy::getAddress, keyword)
            );
        }

        if (city != null && !city.isBlank()) {
            wrapper.eq(Pharmacy::getCity, city);
        }

        if (is24h != null && is24h == 1) {
            wrapper.eq(Pharmacy::getIs24h, 1);
        }

        wrapper.orderByDesc(Pharmacy::getIs24h).orderByDesc(Pharmacy::getCreateTime);
        return this.page(new Page<>(page, size), wrapper);
    }

    @Override
    public Pharmacy getPharmacyDetail(Long id) {
        Pharmacy pharmacy = this.getById(id);
        if (pharmacy == null) {
            throw new RuntimeException("药店不存在");
        }
        return pharmacy;
    }

    @Override
    public List<String> getPharmacyCities() {
        return this.lambdaQuery()
            .eq(Pharmacy::getStatus, 1)
            .select(Pharmacy::getCity)
            .list()
            .stream()
            .map(Pharmacy::getCity)
            .filter(c -> c != null && !c.isBlank())
            .distinct()
            .collect(Collectors.toList());
    }

    // ==================== Admin methods ====================

    @Override
    public Page<Pharmacy> getPharmacyPageAdmin(int page, int size, String keyword) {
        LambdaQueryWrapper<Pharmacy> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                .like(Pharmacy::getName, keyword)
                .or().like(Pharmacy::getAddress, keyword)
            );
        }
        wrapper.orderByDesc(Pharmacy::getIs24h).orderByDesc(Pharmacy::getCreateTime);
        return this.page(new Page<>(page, size), wrapper);
    }

    @Override
    public void createPharmacy(Pharmacy pharmacy) {
        pharmacy.setStatus(1);
        this.save(pharmacy);
    }

    @Override
    public void updatePharmacy(Long id, Pharmacy pharmacy) {
        if (this.getById(id) == null) throw new RuntimeException("药店不存在");
        pharmacy.setId(id);
        this.updateById(pharmacy);
    }

    @Override
    public void deletePharmacy(Long id) {
        if (this.getById(id) == null) throw new RuntimeException("药店不存在");
        this.removeById(id);
    }

    @Override
    public void togglePharmacyStatus(Long id) {
        Pharmacy p = this.getById(id);
        if (p == null) throw new RuntimeException("药店不存在");
        p.setStatus(p.getStatus() != null && p.getStatus() == 1 ? 0 : 1);
        this.updateById(p);
    }
}
