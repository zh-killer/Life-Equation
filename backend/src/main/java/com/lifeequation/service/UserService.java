package com.lifeequation.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lifeequation.dto.LoginDTO;
import com.lifeequation.dto.RegisterDTO;
import com.lifeequation.dto.UserUpdateDTO;
import com.lifeequation.dto.WxLoginDTO;
import com.lifeequation.entity.User;
import java.util.Map;

public interface UserService extends IService<User> {

    Map<String, Object> login(LoginDTO dto);

    Map<String, Object> wxLogin(WxLoginDTO dto);

    void register(RegisterDTO dto);

    User getUserById(Long id);

    Page<User> getUserPage(int page, int size, String keyword, String role);

    void updateUserInfo(Long id, UserUpdateDTO dto);

    void updateUserStatus(Long id, Integer status);

    void deleteUser(Long id);
    void updateProfile(Long userId, String realName, String phone, String email, String avatarUrl, String idCard);

    Map<String, Object> verifyIdentity(Long userId, String idCard, String realName);
    void changePassword(Long userId, String oldPassword, String newPassword);

    void setPassword(Long userId, String newPassword);

    void setUserRole(String username, String role);
}
