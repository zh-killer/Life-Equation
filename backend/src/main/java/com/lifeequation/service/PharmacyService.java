package com.lifeequation.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lifeequation.entity.Pharmacy;
import java.util.List;

public interface PharmacyService extends IService<Pharmacy> {
    Page<Pharmacy> getPharmacyPage(int page, int size, String keyword, String city, Integer is24h);
    Pharmacy getPharmacyDetail(Long id);
    List<String> getPharmacyCities();

    // Admin methods
    Page<Pharmacy> getPharmacyPageAdmin(int page, int size, String keyword);
    void createPharmacy(Pharmacy pharmacy);
    void updatePharmacy(Long id, Pharmacy pharmacy);
    void deletePharmacy(Long id);
    void togglePharmacyStatus(Long id);
}
