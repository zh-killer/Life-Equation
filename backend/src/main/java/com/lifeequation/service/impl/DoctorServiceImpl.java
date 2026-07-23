package com.lifeequation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lifeequation.entity.Doctor;
import com.lifeequation.entity.Hospital;
import com.lifeequation.mapper.DoctorMapper;
import com.lifeequation.mapper.HospitalMapper;
import com.lifeequation.service.DoctorService;
import org.springframework.stereotype.Service;

@Service
public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor> implements DoctorService {

    private final HospitalMapper hospitalMapper;

    public DoctorServiceImpl(HospitalMapper hospitalMapper) {
        this.hospitalMapper = hospitalMapper;
    }

    @Override
    public Page<Doctor> getDoctorPage(int page, int size, Long hospitalId, String department, String keyword) {
        LambdaQueryWrapper<Doctor> w = new LambdaQueryWrapper<>();
        if (hospitalId != null) w.eq(Doctor::getHospitalId, hospitalId);
        if (department != null && !department.isBlank()) w.eq(Doctor::getDepartment, department);
        if (keyword != null && !keyword.isBlank()) {
            w.and(q -> q.like(Doctor::getName, keyword).or().like(Doctor::getSpecialty, keyword));
        }
        w.eq(Doctor::getIsAvailable, 1).orderByDesc(Doctor::getCreateTime);
        Page<Doctor> result = this.page(new Page<>(page, size), w);
        for (Doctor d : result.getRecords()) {
            Hospital h = hospitalMapper.selectById(d.getHospitalId());
            if (h != null) {
                d.setHospitalName(h.getName());
                d.setHospitalLevel(h.getLevel());
            }
        }
        return result;
    }

    @Override
    public Doctor getDoctorDetail(Long id) {
        Doctor d = this.getById(id);
        if (d == null) throw new RuntimeException("医生不存在");
        Hospital h = hospitalMapper.selectById(d.getHospitalId());
        if (h != null) d.setHospitalName(h.getName());
        return d;
    }

    // ==================== Admin methods ====================

    @Override
    public Page<Doctor> getDoctorPageAdmin(int page, int size, String keyword, Long hospitalId) {
        LambdaQueryWrapper<Doctor> w = new LambdaQueryWrapper<>();
        if (hospitalId != null) w.eq(Doctor::getHospitalId, hospitalId);
        if (keyword != null && !keyword.isBlank()) {
            w.and(q -> q.like(Doctor::getName, keyword).or().like(Doctor::getSpecialty, keyword));
        }
        w.orderByDesc(Doctor::getCreateTime);
        Page<Doctor> result = this.page(new Page<>(page, size), w);
        for (Doctor d : result.getRecords()) {
            Hospital h = hospitalMapper.selectById(d.getHospitalId());
            if (h != null) {
                d.setHospitalName(h.getName());
                d.setHospitalLevel(h.getLevel());
            }
        }
        return result;
    }

    @Override
    public void createDoctor(Doctor doctor) {
        doctor.setIsAvailable(1);
        this.save(doctor);
    }

    @Override
    public void updateDoctor(Long id, Doctor doctor) {
        if (this.getById(id) == null) throw new RuntimeException("医生不存在");
        doctor.setId(id);
        this.updateById(doctor);
    }

    @Override
    public void deleteDoctor(Long id) {
        if (this.getById(id) == null) throw new RuntimeException("医生不存在");
        this.removeById(id);
    }

    @Override
    public void toggleDoctorStatus(Long id) {
        Doctor d = this.getById(id);
        if (d == null) throw new RuntimeException("医生不存在");
        d.setIsAvailable(d.getIsAvailable() != null && d.getIsAvailable() == 1 ? 0 : 1);
        this.updateById(d);
    }
}
