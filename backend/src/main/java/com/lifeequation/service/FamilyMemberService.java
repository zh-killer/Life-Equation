package com.lifeequation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lifeequation.entity.FamilyMember;
import java.util.List;

public interface FamilyMemberService extends IService<FamilyMember> {
    List<FamilyMember> getUserMembers(Long userId);
    FamilyMember addMember(Long userId, FamilyMember member);
    FamilyMember updateMember(Long userId, Long id, FamilyMember member);
    void deleteMember(Long userId, Long id);
    void setDefault(Long userId, Long id);
}
