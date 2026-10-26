package com.lifeequation.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lifeequation.entity.Hospital;

public interface HospitalService extends IService<Hospital> {
    Page<Hospital> getHospitalPage(int page, int size, String keyword, String level, String city);
    Hospital getHospitalDetail(Long id);

    // Admin methods
    Page<Hospital> getHospitalPageAdmin(int page, int size, String keyword, String level, String city);
    void createHospital(Hospital hospital);
    void updateHospital(Long id, Hospital hospital);
    void deleteHospital(Long id);
    void updateHospitalStatus(Long id, Integer status);
}
