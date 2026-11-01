package com.lifeequation.controller;

import com.lifeequation.common.Result;
import com.lifeequation.dto.LoginDTO;
import com.lifeequation.dto.RegisterDTO;
import com.lifeequation.dto.WxLoginDTO;
import com.lifeequation.entity.User;
import com.lifeequation.security.SecurityUtils;
import com.lifeequation.service.TokenService;
import com.lifeequation.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final TokenService tokenService;

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    public AuthController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok(userService.login(dto));
    }

    @PostMapping("/wx-login")
    public Result<?> wxLogin(@Valid @RequestBody WxLoginDTO dto) {
        return Result.ok(userService.wxLogin(dto));
    }

    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.ok("注册成功");
    }

    @GetMapping("/info")
    public Result<?> getUserInfo() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userService.getUserById(userId);
        user.setPassword(null);
        return Result.ok(user);
    }

    @PutMapping("/update-profile")
    public Result<?> updateProfile(@RequestBody Map<String, String> params) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.updateProfile(userId,
                params.get("realName"),
                params.get("phone"),
                params.get("email"),
                params.get("avatarUrl"),
                params.get("idCard"));
        return Result.ok("资料已更新");
    }

    @PostMapping("/verify-identity")
    public Result<?> verifyIdentity(@RequestBody Map<String, String> params) {
        Long userId = SecurityUtils.getCurrentUserId();
        String idCard = params.get("idCard");
        String realName = params.get("realName");
        if (idCard == null || idCard.isBlank()) {
            return Result.fail("身份证号不能为空");
        }
        return Result.ok(userService.verifyIdentity(userId, idCard, realName));
    }

    @PutMapping("/change-password")
    public Result<?> changePassword(@RequestBody Map<String, String> params,
                                    HttpServletRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.changePassword(userId,
                params.get("oldPassword"),
                params.get("newPassword"));
        // Blacklist current token so old tokens become invalid
        String token = extractBearerToken(request);
        if (token != null) {
            tokenService.blacklist(token);
        }
        return Result.ok("密码已修改，请重新登录");
    }

    @PostMapping("/upload-avatar")
    public Result<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.fail("请选择文件");
        Long userId = SecurityUtils.getCurrentUserId();
        try {
            String dir = uploadPath + "/avatars/" + userId;
            Files.createDirectories(Paths.get(dir));
            String originalName = file.getOriginalFilename();
            String ext = ".jpg";
            if (originalName != null && originalName.contains(".")) {
                String candidate = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
                // 扩展名白名单：任意后缀会让人上传 .html/.svg 并在同源下被浏览器渲染
                if (candidate.matches("\\.(jpg|jpeg|png|gif|webp)")) {
                    ext = candidate;
                }
            }
            String fileName = UUID.randomUUID() + ext;
            Path target = Paths.get(dir, fileName);
            file.transferTo(target.toFile());
            String avatarUrl = "/uploads/avatars/" + userId + "/" + fileName;
            // 更新用户头像
            userService.updateProfile(userId, null, null, null, avatarUrl, null);
            return Result.ok(avatarUrl);
        } catch (IOException e) {
            return Result.fail("上传失败: " + e.getMessage());
        }
    }

    /**
     * 通过 base64 上传头像（绕过 wx.uploadFile 的域名校验，走 wx.request 代理）
     */
    @PostMapping("/upload-avatar-base64")
    public Result<?> uploadAvatarBase64(@RequestBody Map<String, String> params) {
        String base64Data = params.get("file");
        if (base64Data == null || base64Data.isBlank()) return Result.fail("请选择文件");
        Long userId = SecurityUtils.getCurrentUserId();
        try {
            // 去掉 data:image/...;base64, 前缀（如果有）
            if (base64Data.contains(",")) {
                base64Data = base64Data.substring(base64Data.indexOf(",") + 1);
            }
            byte[] bytes = Base64.getDecoder().decode(base64Data);
            String extParam = params.getOrDefault("ext", "jpg");
            if (!extParam.startsWith(".")) extParam = "." + extParam;
            extParam = extParam.toLowerCase();
            String ext = extParam.matches("\\.(jpg|jpeg|png|gif|webp)") ? extParam : ".jpg";
            String dir = uploadPath + "/avatars/" + userId;
            Files.createDirectories(Paths.get(dir));
            String fileName = UUID.randomUUID() + ext;
            Path target = Paths.get(dir, fileName);
            Files.write(target, bytes);
            String avatarUrl = "/uploads/avatars/" + userId + "/" + fileName;
            userService.updateProfile(userId, null, null, null, avatarUrl, null);
            return Result.ok(avatarUrl);
        } catch (IOException e) {
            return Result.fail("上传失败: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return Result.fail("图片数据解析失败");
        }
    }

    /**
     * 获取当前用户头像的 base64 数据（绕过 <image> 的 HTTP 限制）
     */
    @GetMapping("/avatar-base64")
    public Result<?> getAvatarBase64() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userService.getUserById(userId);
        String avatarUrl = user.getAvatarUrl();
        if (avatarUrl == null || avatarUrl.isBlank()) {
            return Result.ok("");
        }
        try {
            Path uploadRoot = Paths.get(uploadPath).toAbsolutePath().normalize();
            Path filePath = uploadRoot.resolve(avatarUrl.replace("/uploads/", "")).normalize();
            // 严格限定在上传根目录内，防止通过 ../ 穿越读取服务器任意文件
            if (!filePath.startsWith(uploadRoot) || !Files.exists(filePath)) {
                return Result.ok("");
            }
            byte[] bytes = Files.readAllBytes(filePath);
            String base64 = Base64.getEncoder().encodeToString(bytes);
            String ext = avatarUrl.substring(avatarUrl.lastIndexOf(".") + 1).toLowerCase();
            String mime = ext.equals("png") ? "image/png" : ext.equals("gif") ? "image/gif" : "image/jpeg";
            return Result.ok("data:" + mime + ";base64," + base64);
        } catch (IOException e) {
            return Result.fail("读取头像失败");
        }
    }

    @PutMapping("/set-password")
    public Result<?> setPassword(@RequestBody Map<String, String> params) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.setPassword(userId, params.get("newPassword"));
        return Result.ok("密码设置成功");
    }

    @PostMapping("/logout")
    public Result<?> logout(HttpServletRequest request) {
        String token = extractBearerToken(request);
        if (token != null) {
            tokenService.blacklist(token);
        }
        return Result.ok("已退出登录");
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
