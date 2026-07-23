package com.lifeequation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeequation.common.Result;
import com.lifeequation.dto.UserUpdateDTO;
import com.lifeequation.entity.User;
import com.lifeequation.service.TokenService;
import com.lifeequation.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;
    private final TokenService tokenService;

    public UserController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @GetMapping
    public Result<?> getUserPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {
        Page<User> result = userService.getUserPage(page, size, keyword, role);
        result.getRecords().forEach(u -> u.setPassword(null));
        return Result.ok(result);
    }

    @GetMapping("/{id}")
    public Result<?> getUserDetail(@PathVariable Long id) {
        User user = userService.getUserById(id);
        user.setPassword(null);
        return Result.ok(user);
    }

    @PutMapping("/{id}")
    public Result<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) {
        userService.updateUserInfo(id, dto);
        return Result.ok("更新成功");
    }

    @PutMapping("/{id}/status")
    public Result<?> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateUserStatus(id, status);
        // When disabling a user, invalidate all existing tokens
        if (status == 0) {
            tokenService.setUserInvalidation(id);
            tokenService.invalidateAllSessions(id);
        }
        return Result.ok(status == 1 ? "已启用" : "已禁用");
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.ok("删除成功");
    }

    @PutMapping("/{id}/role")
    public Result<?> updateUserRole(@PathVariable Long id, @RequestParam String role) {
        User user = userService.getUserById(id);
        user.setRole(role);
        userService.updateById(user);
        return Result.ok("角色已更新");
    }
}
