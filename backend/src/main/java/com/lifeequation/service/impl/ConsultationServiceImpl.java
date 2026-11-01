package com.lifeequation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lifeequation.entity.Consultation;
import com.lifeequation.entity.Doctor;
import com.lifeequation.entity.User;
import com.lifeequation.mapper.ConsultationMapper;
import com.lifeequation.mapper.DoctorMapper;
import com.lifeequation.mapper.UserMapper;
import com.lifeequation.service.ConsultationService;
import org.springframework.stereotype.Service;

@Service
public class ConsultationServiceImpl extends ServiceImpl<ConsultationMapper, Consultation> implements ConsultationService {

    private final UserMapper userMapper;
    private final DoctorMapper doctorMapper;

    public ConsultationServiceImpl(UserMapper userMapper, DoctorMapper doctorMapper) {
        this.userMapper = userMapper;
        this.doctorMapper = doctorMapper;
    }

    @Override
    public Page<Consultation> getPublicPage(int page, int size, String keyword) {
        LambdaQueryWrapper<Consultation> w = new LambdaQueryWrapper<>();
        w.eq(Consultation::getIsPublic, 1);
        if (keyword != null && !keyword.isBlank()) {
            w.like(Consultation::getQuestion, keyword);
        }
        w.orderByDesc(Consultation::getCreateTime);
        Page<Consultation> result = this.page(new Page<>(page, size), w);
        populateNames(result.getRecords());
        return result;
    }

    @Override
    public Page<Consultation> getUserPage(int page, int size, Long userId) {
        LambdaQueryWrapper<Consultation> w = new LambdaQueryWrapper<>();
        w.eq(Consultation::getUserId, userId).orderByDesc(Consultation::getCreateTime);
        Page<Consultation> result = this.page(new Page<>(page, size), w);
        populateNames(result.getRecords());
        return result;
    }

    @Override
    public Consultation getDetail(Long id, Long currentUserId) {
        Consultation c = this.getById(id);
        // 不存在和无权查看返回同一提示，避免通过报错探测私密问答是否存在
        if (c == null) {
            throw new RuntimeException("问答不存在");
        }
        boolean isPublic = Integer.valueOf(1).equals(c.getIsPublic());
        boolean isOwner = currentUserId != null && currentUserId.equals(c.getUserId());
        if (!isPublic && !isOwner) {
            throw new RuntimeException("问答不存在");
        }
        populateNames(java.util.List.of(c));
        return c;
    }

    @Override
    public void createQuestion(Consultation c) {
        c.setStatus("PENDING");
        this.save(c);
    }

    @Override
    public void deleteQuestion(Long id, Long userId) {
        Consultation c = this.getById(id);
        if (c == null) {
            throw new RuntimeException("问答不存在");
        }
        if (!c.getUserId().equals(userId)) {
            throw new RuntimeException("只能删除自己的提问");
        }
        this.removeById(id);
    }

    // ==================== Admin methods ====================

    @Override
    public Page<Consultation> getConsultationPageAdmin(int page, int size, String keyword, String status) {
        LambdaQueryWrapper<Consultation> w = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) {
            w.eq(Consultation::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            w.like(Consultation::getQuestion, keyword);
        }
        w.orderByDesc(Consultation::getCreateTime);
        Page<Consultation> result = this.page(new Page<>(page, size), w);
        populateNames(result.getRecords());
        return result;
    }

    @Override
    public void deleteConsultationAdmin(Long id) {
        if (this.getById(id) == null) throw new RuntimeException("咨询不存在");
        this.removeById(id);
    }

    private void populateNames(java.util.List<Consultation> list) {
        for (Consultation c : list) {
            if (c.getUserId() != null) {
                User u = userMapper.selectById(c.getUserId());
                if (u != null) c.setUserName(u.getRealName() != null ? u.getRealName() : u.getUsername());
            }
            if (c.getDoctorId() != null) {
                Doctor d = doctorMapper.selectById(c.getDoctorId());
                if (d != null) c.setDoctorName(d.getName());
            }
        }
    }
}
