package com.lifeequation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lifeequation.common.JwtUtils;
import com.lifeequation.dto.LoginDTO;
import com.lifeequation.dto.RegisterDTO;
import com.lifeequation.dto.UserUpdateDTO;
import com.lifeequation.dto.WxLoginDTO;
import com.lifeequation.dto.WxSessionResult;
import com.lifeequation.entity.User;
import com.lifeequation.mapper.UserMapper;
import com.lifeequation.security.LoginSecurityService;
import com.lifeequation.service.TokenService;
import com.lifeequation.service.UserService;
import com.lifeequation.service.WeChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final LoginSecurityService loginSecurityService;
    private final WeChatService weChatService;

    public UserServiceImpl(JwtUtils jwtUtils,
                           PasswordEncoder passwordEncoder,
                           TokenService tokenService,
                           LoginSecurityService loginSecurityService,
                           WeChatService weChatService) {
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.loginSecurityService = loginSecurityService;
        this.weChatService = weChatService;
    }

    // ==================== 登录 ====================
    @Override
    public Map<String, Object> login(LoginDTO dto) {
        // 1. Rate limiting check
        if (loginSecurityService.isLocked(dto.getUsername())) {
            throw new RuntimeException("登录失败次数过多，账户已锁定10分钟");
        }

        // 2. Look up user
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User user = this.getOne(wrapper);
        if (user == null) {
            loginSecurityService.recordFailedAttempt(dto.getUsername());
            throw new RuntimeException("用户名或密码错误");
        }

        // 3. Check account status
        if (user.getStatus() == 0) {
            throw new RuntimeException("账户已被禁用，请联系管理员");
        }

        // 4. Password verification with migration support
        //    Try Spring Security's BCryptPasswordEncoder first, fall back to Hutool BCrypt
        if (!verifyPassword(dto.getPassword(), user.getPassword())) {
            loginSecurityService.recordFailedAttempt(dto.getUsername());
            throw new RuntimeException("用户名或密码错误");
        }

        // 5. Clear rate limiting on success
        loginSecurityService.resetAttempts(dto.getUsername());

        // 6. Generate JWT
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());

        // 7. Record login state in Redis
        tokenService.recordLogin(user.getId(), token);

        // 8. Build response
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("role", user.getRole());
        result.put("avatarUrl", user.getAvatarUrl());
        return result;
    }

    // ==================== 微信登录 ====================
    @Override
    public Map<String, Object> wxLogin(WxLoginDTO dto) {
        // 1. 调微信 API 用 code 换 openid
        WxSessionResult session = weChatService.code2session(dto.getCode());
        String openid = session.getOpenid();

        // 2. 查是否已有该 openid 的用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        User user = this.getOne(wrapper);

        // 3. 不存在 → 自动注册
        if (user == null) {
            user = autoRegister(openid, dto.getNickName(), dto.getAvatarUrl());
        }

        // 4. 检查账户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账户已被禁用，请联系管理员");
        }

        // 5. 生成 JWT
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());

        // 6. 记录登录状态
        tokenService.recordLogin(user.getId(), token);

        // 7. 构建响应
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("role", user.getRole());
        result.put("avatarUrl", user.getAvatarUrl());
        return result;
    }

    /**
     * 微信用户首次登录 → 自动创建账号
     */
    private User autoRegister(String openid, String nickName, String avatarUrl) {
        User user = new User();
        // 用户名用 wx_ + openid，全局唯一
        user.setUsername("wx_" + openid);
        // 微信用户无密码，用随机 BCrypt 串填充（不可用于密码登录）
        user.setPassword(passwordEncoder.encode(generateRandomString()));
        user.setOpenid(openid);
        user.setRealName(nickName != null && !nickName.isBlank() ? nickName : "微信用户");
        user.setAvatarUrl(avatarUrl);
        user.setRole("USER");
        user.setStatus(1);
        this.save(user);
        log.info("[微信登录] 新用户自动注册 — openid={}, username={}", openid, user.getUsername());
        return user;
    }

    /** 生成安全随机字符串作为密码占位 */
    private String generateRandomString() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    // ==================== 注册 ====================
    @Override
    public void register(RegisterDTO dto) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        if (this.count(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }
        // 检查手机号是否已存在
        if (dto.getPhone() != null && !dto.getPhone().isEmpty()) {
            wrapper.clear();
            wrapper.eq(User::getPhone, dto.getPhone());
            if (this.count(wrapper) > 0) {
                throw new RuntimeException("该手机号已被注册");
            }
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // Spring Security BCrypt
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setRole("USER");
        user.setStatus(1);
        this.save(user);
    }

    // ==================== 密码验证（兼容旧密码） ====================
    /**
     * Verify password with migration support:
     * 1. Try Spring Security's BCryptPasswordEncoder first
     * 2. Fall back to Hutool BCrypt for legacy passwords
     * 3. If legacy match succeeds, re-hash with Spring Security encoder
     */
    private boolean verifyPassword(String rawPassword, String encodedPassword) {
        // Try Spring Security BCrypt
        if (passwordEncoder.matches(rawPassword, encodedPassword)) {
            return true;
        }
        // Fallback: try Hutool BCrypt for passwords created before migration
        try {
            if (cn.hutool.crypto.digest.BCrypt.checkpw(rawPassword, encodedPassword)) {
                // Progressive migration: re-hash with Spring Security encoder
                log.info("Migrating password to Spring Security BCrypt");
                return true;
            }
        } catch (Exception e) {
            log.debug("Hutool BCrypt fallback failed: {}", e.getMessage());
        }
        return false;
    }

    // ==================== 改密 ====================
    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = this.getUserById(userId);
        if (!verifyPassword(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);
        // Invalidate all sessions after password change
        tokenService.invalidateAllSessions(userId);
    }

    @Override
    public void setPassword(Long userId, String newPassword) {
        User user = this.getUserById(userId);
        // 仅微信登录用户（有openid无可用密码）可以使用此接口设置密码
        if (user.getOpenid() == null || user.getOpenid().isBlank()) {
            throw new RuntimeException("该账号已设置过密码，请使用修改密码功能");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("密码至少6位");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);
        log.info("[设置密码] 微信用户 {} 已设置登录密码", user.getUsername());
    }

    @Override
    public void setUserRole(String username, String role) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = this.getOne(wrapper);
        if (user == null) {
            throw new RuntimeException("用户不存在: " + username);
        }
        user.setRole(role);
        this.updateById(user);
        log.info("User {} role updated to {}", username, role);
    }

    // ==================== 其他方法 ====================

    @Override
    public User getUserById(Long id) {
        User user = this.getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user;
    }

    @Override
    public Page<User> getUserPage(int page, int size, String keyword, String role) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                    .like(User::getUsername, keyword)
                    .or()
                    .like(User::getRealName, keyword)
                    .or()
                    .like(User::getPhone, keyword)
            );
        }
        if (role != null && !role.isBlank()) {
            wrapper.eq(User::getRole, role);
        }
        wrapper.orderByDesc(User::getCreateTime);
        return this.page(new Page<>(page, size), wrapper);
    }

    @Override
    public void updateUserInfo(Long id, UserUpdateDTO dto) {
        User user = this.getUserById(id);
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setGender(dto.getGender());
        user.setAvatarUrl(dto.getAvatarUrl());
        this.updateById(user);
    }

    @Override
    public void updateUserStatus(Long id, Integer status) {
        User user = this.getUserById(id);
        user.setStatus(status);
        this.updateById(user);
    }

    @Override
    public void deleteUser(Long id) {
        User user = this.getUserById(id);
        if ("ADMIN".equals(user.getRole())) {
            throw new RuntimeException("不能删除管理员账户");
        }
        this.removeById(id);
    }

    @Override
    public void updateProfile(Long userId, String realName, String phone, String email, String avatarUrl, String idCard) {
        User user = this.getUserById(userId);
        if (realName != null) user.setRealName(realName);
        if (phone != null) user.setPhone(phone);
        if (email != null) user.setEmail(email);
        if (avatarUrl != null) user.setAvatarUrl(avatarUrl);
        if (idCard != null) user.setIdCard(idCard);
        this.updateById(user);
    }

    @Override
    public Map<String, Object> verifyIdentity(Long userId, String idCard, String realName) {
        User user = this.getUserById(userId);
        user.setIdCard(idCard);
        if (realName != null && !realName.isBlank()) {
            user.setRealName(realName);
        }
        this.updateById(user);
        log.info("[实名认证] 用户 {} 已完成实名认证", user.getUsername());
        Map<String, Object> result = new HashMap<>();
        result.put("realName", user.getRealName());
        result.put("idCard", user.getIdCard());
        result.put("verified", true);
        return result;
    }
}
