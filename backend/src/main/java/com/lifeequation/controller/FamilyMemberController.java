package com.lifeequation.controller;

import com.lifeequation.common.Result;
import com.lifeequation.entity.FamilyMember;
import com.lifeequation.security.SecurityUtils;
import com.lifeequation.service.FamilyMemberService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/family-members")
public class FamilyMemberController {

    private final FamilyMemberService familyMemberService;

    public FamilyMemberController(FamilyMemberService familyMemberService) {
        this.familyMemberService = familyMemberService;
    }

    @GetMapping
    public Result<?> list() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<FamilyMember> members = familyMemberService.getUserMembers(userId);
        return Result.ok(members);
    }

    @PostMapping
    public Result<?> add(@RequestBody FamilyMember member) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.ok(familyMemberService.addMember(userId, member));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody FamilyMember member) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.ok(familyMemberService.updateMember(userId, id, member));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        familyMemberService.deleteMember(userId, id);
        return Result.ok("删除成功");
    }

    @PutMapping("/{id}/default")
    public Result<?> setDefault(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        familyMemberService.setDefault(userId, id);
        return Result.ok("设置成功");
    }
}
