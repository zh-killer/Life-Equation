package com.lifeequation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lifeequation.entity.FamilyMember;
import com.lifeequation.mapper.FamilyMemberMapper;
import com.lifeequation.service.FamilyMemberService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FamilyMemberServiceImpl extends ServiceImpl<FamilyMemberMapper, FamilyMember> implements FamilyMemberService {

    @Override
    public List<FamilyMember> getUserMembers(Long userId) {
        LambdaQueryWrapper<FamilyMember> w = new LambdaQueryWrapper<>();
        w.eq(FamilyMember::getUserId, userId).orderByDesc(FamilyMember::getIsDefault).orderByAsc(FamilyMember::getId);
        return this.list(w);
    }

    @Override
    public FamilyMember addMember(Long userId, FamilyMember member) {
        member.setUserId(userId);
        // 如果是第一个就诊人，自动设为默认
        long count = this.count(new LambdaQueryWrapper<FamilyMember>().eq(FamilyMember::getUserId, userId));
        if (count == 0) member.setIsDefault(1);
        this.save(member);
        return member;
    }

    @Override
    public FamilyMember updateMember(Long userId, Long id, FamilyMember member) {
        FamilyMember existing = this.getById(id);
        if (existing == null || !existing.getUserId().equals(userId))
            throw new RuntimeException("就诊人信息不存在");
        member.setId(id);
        member.setUserId(userId);
        this.updateById(member);
        return this.getById(id);
    }

    @Override
    public void deleteMember(Long userId, Long id) {
        FamilyMember existing = this.getById(id);
        if (existing == null || !existing.getUserId().equals(userId))
            throw new RuntimeException("就诊人信息不存在");
        this.removeById(id);
    }

    @Override
    public void setDefault(Long userId, Long id) {
        FamilyMember member = this.getById(id);
        if (member == null || !member.getUserId().equals(userId))
            throw new RuntimeException("就诊人信息不存在");
        // 取消其他默认
        LambdaQueryWrapper<FamilyMember> w = new LambdaQueryWrapper<>();
        w.eq(FamilyMember::getUserId, userId).eq(FamilyMember::getIsDefault, 1);
        FamilyMember oldDefault = this.getOne(w);
        if (oldDefault != null) {
            oldDefault.setIsDefault(0);
            this.updateById(oldDefault);
        }
        member.setIsDefault(1);
        this.updateById(member);
    }
}
