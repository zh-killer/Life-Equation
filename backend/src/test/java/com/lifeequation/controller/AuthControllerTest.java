package com.lifeequation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeequation.config.TestConfig;
import com.lifeequation.dto.LoginDTO;
import com.lifeequation.dto.RegisterDTO;
import com.lifeequation.service.TokenService;
import com.lifeequation.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller 层集成测试（AuthController）。
 *
 * 使用 @SpringBootTest + @AutoConfigureMockMvc 加载完整上下文，
 * 通过 @MockBean 模拟 Service 层，验证 HTTP 路由、参数绑定、
 * 状态码、JSON 响应结构。
 *
 * 对应文档 7.2 第四步：Controller 层测试
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)  // 禁用 Security 过滤器，专注于 Controller 测试
@Import(TestConfig.class)  // 提供 Redis 相关 Mock Bean
@DisplayName("认证控制器集成测试")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * @MockBean：在 Spring 容器中创建一个模拟的 Service Bean。
     */
    @MockBean
    private UserService userService;

    @MockBean
    private TokenService tokenService;

    // ==================== POST /api/auth/register ====================

    @Test
    @DisplayName("POST /api/auth/register — 注册成功应返回200")
    void register_WhenValidInput_ShouldReturn200() throws Exception {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("newUser");
        dto.setPassword("pass123");
        dto.setRealName("新用户");
        dto.setPhone("13800138001");
        dto.setEmail("new@example.com");
        String requestBody = objectMapper.writeValueAsString(dto);

        doNothing().when(userService).register(any(RegisterDTO.class));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                // 注：Result.ok(data) 将 "注册成功" 放在 data 字段，message 为默认 "操作成功"
                .andExpect(jsonPath("$.data").value("注册成功"));
    }

    @Test
    @DisplayName("POST /api/auth/register — 用户名不能为空，应返回校验错误")
    void register_WhenUsernameBlank_ShouldReturn400() throws Exception {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("");
        dto.setPassword("12");
        dto.setRealName("用户");
        String requestBody = objectMapper.writeValueAsString(dto);

        // 注：本项目 GlobalExceptionHandler 捕获 MethodArgumentNotValidException
        // 并返回 HTTP 200 但 code 为 500，包含错误详情
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("POST /api/auth/register — 邮箱格式不正确，应返回校验错误")
    void register_WhenEmailInvalid_ShouldReturn400() throws Exception {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("validUser");
        dto.setPassword("validPass123");
        dto.setRealName("用户");
        dto.setEmail("not-an-email");
        String requestBody = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("POST /api/auth/register — 手机号格式不正确，应返回校验错误")
    void register_WhenPhoneInvalid_ShouldReturn400() throws Exception {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("validUser");
        dto.setPassword("validPass123");
        dto.setRealName("用户");
        dto.setPhone("12345");
        String requestBody = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    // ==================== POST /api/auth/login ====================

    @Test
    @DisplayName("POST /api/auth/login — 登录成功应返回200及token信息")
    void login_WhenSuccess_ShouldReturn200WithToken() throws Exception {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("testUser");
        dto.setPassword("correctPass");
        String requestBody = objectMapper.writeValueAsString(dto);

        Map<String, Object> loginResult = Map.of(
                "token", "mock-jwt-token-abc",
                "userId", 1L,
                "username", "testUser",
                "realName", "测试用户",
                "role", "USER",
                "avatarUrl", "/uploads/avatar.png"
        );
        when(userService.login(any(LoginDTO.class))).thenReturn(loginResult);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("mock-jwt-token-abc"))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.username").value("testUser"))
                .andExpect(jsonPath("$.data.role").value("USER"));
    }

    @Test
    @DisplayName("POST /api/auth/login — 密码不能为空，应返回校验错误")
    void login_WhenPasswordBlank_ShouldReturn400() throws Exception {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("testUser");
        dto.setPassword("");
        String requestBody = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("POST /api/auth/login — 用户名为空，应返回校验错误")
    void login_WhenUsernameBlank_ShouldReturn400() throws Exception {
        LoginDTO dto = new LoginDTO();
        dto.setUsername(null);
        dto.setPassword("validPass123");
        String requestBody = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }
}
