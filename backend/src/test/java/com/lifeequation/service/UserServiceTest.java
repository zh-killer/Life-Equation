package com.lifeequation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.lifeequation.common.JwtUtils;
import com.lifeequation.common.Result;
import com.lifeequation.dto.LoginDTO;
import com.lifeequation.dto.RegisterDTO;
import com.lifeequation.dto.UserUpdateDTO;
import com.lifeequation.entity.User;
import com.lifeequation.mapper.UserMapper;
import com.lifeequation.security.LoginSecurityService;
import com.lifeequation.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UserService 单元测试。
 * 使用 Mockito 模拟 UserMapper、JwtUtils、PasswordEncoder 等外部依赖，
 * 实现对业务逻辑的隔离测试。
 *
 * 对应文档 7.2 第二步：Service 层单元测试 (UserServiceTest)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务单元测试")
class UserServiceTest {

    /**
     * @Mock：为 UserMapper 接口创建一个模拟对象。
     * 该对象的所有方法默认返回 null / 0 / false / 空集合。
     */
    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @Mock
    private LoginSecurityService loginSecurityService;

    /**
     * @InjectMocks：创建 UserServiceImpl 的实例，
     * 并将 @Mock 标注的模拟对象自动注入。
     */
    @InjectMocks
    private UserServiceImpl userService;

    private RegisterDTO registerDTO;
    private LoginDTO loginDTO;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 由于 UserServiceImpl 继承 ServiceImpl，baseMapper 通过 @Autowired
        // 字段注入。Mockito @InjectMocks 可能无法注入父类字段，此处手动设置。
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);

        // 准备注册 DTO
        registerDTO = new RegisterDTO();
        registerDTO.setUsername("testUser");
        registerDTO.setPassword("plainPassword");
        registerDTO.setRealName("测试用户");
        registerDTO.setPhone("13800138000");
        registerDTO.setEmail("test@example.com");

        // 准备登录 DTO
        loginDTO = new LoginDTO();
        loginDTO.setUsername("testUser");
        loginDTO.setPassword("plainPassword");

        // 准备测试用户实体
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setPassword("encodedPassword");
        testUser.setRealName("测试用户");
        testUser.setRole("USER");
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
        testUser.setStatus(1);
    }

    // ==================== register 注册测试 ====================

    @Test
    @DisplayName("注册 — 用户名已存在，应抛出异常")
    void register_WhenUsernameExists_ShouldThrowException() {
        // 1. 打桩 (Stubbing)：使用 when().thenReturn() 预设模拟对象的行为
        //    count() 方法返回 1，表示用户名已存在
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // 2. 执行被测方法并验证抛出异常
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.register(registerDTO),
                "用户名已存在时应抛出 RuntimeException"
        );
        assertEquals("用户名已存在", exception.getMessage());

        // 3. 验证密码编码器未被调用（因为在此之前就抛异常了）
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("注册 — 手机号已被注册，应抛出异常")
    void register_WhenPhoneExists_ShouldThrowException() {
        // 用户名不存在 (count=0)，但手机号已存在 (第二次count=1)
        when(userMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(0L)   // 第一次调用是检查用户名
                .thenReturn(1L);  // 第二次调用是检查手机号

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.register(registerDTO)
        );
        assertEquals("该手机号已被注册", exception.getMessage());
    }

    @Test
    @DisplayName("注册 — 新用户注册成功")
    void register_WhenNewUser_ShouldSucceed() {
        // 用户名不存在，手机号不存在
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L, 0L);
        // 密码加密
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        // 插入用户成功
        when(userMapper.insert(any(User.class))).thenReturn(1);

        // 执行
        assertDoesNotThrow(() -> userService.register(registerDTO));

        // 验证：selectCount 被调用2次（检查用户名 + 检查手机号）
        verify(userMapper, times(2)).selectCount(any(LambdaQueryWrapper.class));
        // 验证：密码编码器被调用1次
        verify(passwordEncoder, times(1)).encode("plainPassword");
        // 验证：插入操作被调用1次
        verify(userMapper, times(1)).insert(any(User.class));
    }

    @Test
    @DisplayName("注册 — 手机号为空时跳过手机号检查")
    void register_WhenPhoneIsNull_ShouldSkipPhoneCheck() {
        registerDTO.setPhone(null);

        // 用户名不存在
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userMapper.insert(any())).thenReturn(1);

        assertDoesNotThrow(() -> userService.register(registerDTO));

        // selectCount 只调用1次（仅检查用户名，跳过手机号检查）
        verify(userMapper, times(1)).selectCount(any(LambdaQueryWrapper.class));
    }

    // ==================== login 登录测试 ====================

    @Test
    @DisplayName("登录 — 账户被锁定，应抛出异常")
    void login_WhenAccountLocked_ShouldThrowException() {
        when(loginSecurityService.isLocked("testUser")).thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.login(loginDTO)
        );
        assertEquals("登录失败次数过多，账户已锁定10分钟", exception.getMessage());

        // 验证后续操作均未执行
        verify(userMapper, never()).selectOne(any(), anyBoolean());
    }

    @Test
    @DisplayName("登录 — 用户不存在，应抛出异常并记录失败")
    void login_WhenUserNotFound_ShouldThrowException() {
        when(loginSecurityService.isLocked("testUser")).thenReturn(false);
        // getOne 调用 selectOne
        when(userMapper.selectOne(any(), anyBoolean())).thenReturn(null);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.login(loginDTO)
        );
        assertEquals("用户名或密码错误", exception.getMessage());

        // 验证失败尝试被记录
        verify(loginSecurityService).recordFailedAttempt("testUser");
    }

    @Test
    @DisplayName("登录 — 账户被禁用，应抛出异常")
    void login_WhenAccountDisabled_ShouldThrowException() {
        testUser.setStatus(0); // 禁用状态
        when(loginSecurityService.isLocked("testUser")).thenReturn(false);
        when(userMapper.selectOne(any(), anyBoolean())).thenReturn(testUser);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.login(loginDTO)
        );
        assertEquals("账户已被禁用，请联系管理员", exception.getMessage());
    }

    @Test
    @DisplayName("登录 — 密码错误，应抛出异常")
    void login_WhenWrongPassword_ShouldThrowException() {
        when(loginSecurityService.isLocked("testUser")).thenReturn(false);
        when(userMapper.selectOne(any(), anyBoolean())).thenReturn(testUser);
        // 密码验证失败
        when(passwordEncoder.matches("plainPassword", "encodedPassword")).thenReturn(false);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.login(loginDTO)
        );
        assertEquals("用户名或密码错误", exception.getMessage());
        verify(loginSecurityService).recordFailedAttempt("testUser");
    }

    @Test
    @DisplayName("登录 — 成功登录，返回包含token的Map")
    void login_WhenSuccess_ShouldReturnTokenMap() {
        when(loginSecurityService.isLocked("testUser")).thenReturn(false);
        when(userMapper.selectOne(any(), anyBoolean())).thenReturn(testUser);
        when(passwordEncoder.matches("plainPassword", "encodedPassword")).thenReturn(true);
        when(jwtUtils.generateToken(1L, "testUser", "USER"))
                .thenReturn("mock-jwt-token-abc123");

        // 执行
        var result = userService.login(loginDTO);

        // 验证返回值
        assertNotNull(result);
        assertEquals("mock-jwt-token-abc123", result.get("token"));
        assertEquals(1L, result.get("userId"));
        assertEquals("testUser", result.get("username"));
        assertEquals("USER", result.get("role"));

        // 验证关键方法被调用
        verify(loginSecurityService).resetAttempts("testUser");
        verify(tokenService).recordLogin(eq(1L), eq("mock-jwt-token-abc123"));
        verify(jwtUtils).generateToken(1L, "testUser", "USER");
    }

    // ==================== getUserById 测试 ====================

    @Test
    @DisplayName("getUserById — 用户存在时返回用户")
    void getUserById_WhenUserExists_ShouldReturnUser() {
        when(userMapper.selectById(1L)).thenReturn(testUser);

        User result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
    }

    @Test
    @DisplayName("getUserById — 用户不存在时抛出异常")
    void getUserById_WhenUserNotFound_ShouldThrowException() {
        when(userMapper.selectById(999L)).thenReturn(null);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.getUserById(999L)
        );
        assertEquals("用户不存在", exception.getMessage());
    }

    // ==================== changePassword 测试 ====================

    @Test
    @DisplayName("改密 — 原密码错误应抛出异常")
    void changePassword_WhenOldPasswordWrong_ShouldThrowException() {
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(passwordEncoder.matches("wrongOldPass", "encodedPassword")).thenReturn(false);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.changePassword(1L, "wrongOldPass", "newPass")
        );
        assertEquals("原密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("改密 — 密码修改成功并失效所有会话")
    void changePassword_WhenSuccess_ShouldInvalidateSessions() {
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(passwordEncoder.matches("correctOldPass", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("newEncodedPass");
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        assertDoesNotThrow(() -> userService.changePassword(1L, "correctOldPass", "newPass"));

        verify(passwordEncoder).encode("newPass");
        verify(userMapper).updateById(any(User.class));
        verify(tokenService).invalidateAllSessions(1L);
    }

    // ==================== deleteUser 测试 ====================

    @Test
    @DisplayName("删除 — 不能删除管理员账户")
    void deleteUser_WhenAdmin_ShouldThrowException() {
        testUser.setRole("ADMIN");
        when(userMapper.selectById(1L)).thenReturn(testUser);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.deleteUser(1L)
        );
        assertEquals("不能删除管理员账户", exception.getMessage());

        // 验证删除操作未执行
        verify(userMapper, never()).deleteById(any(java.io.Serializable.class));
    }

    @Test
    @DisplayName("删除 — 普通用户删除成功")
    void deleteUser_WhenRegularUser_ShouldSucceed() {
        when(userMapper.selectById(1L)).thenReturn(testUser); // role=USER
        when(userMapper.deleteById((java.io.Serializable) 1L)).thenReturn(1);

        // ServiceImpl.removeById() 内部调用 TableInfoHelper.getTableInfo()，
        // 纯 Mockito 单测没有 MyBatis-Plus 全局配置，需要 mockStatic 提供 TableInfo
        try (var mockedTableInfo = mockStatic(TableInfoHelper.class)) {
            TableInfo tableInfo = mock(TableInfo.class);
            when(tableInfo.isWithLogicDelete()).thenReturn(false);
            mockedTableInfo.when(() -> TableInfoHelper.getTableInfo(User.class))
                    .thenReturn(tableInfo);

            assertDoesNotThrow(() -> userService.deleteUser(1L));
        }
        verify(userMapper).deleteById(eq(1L));
    }

    // ==================== updateUserStatus 测试 ====================

    @Test
    @DisplayName("updateUserStatus — 更新状态成功")
    void updateUserStatus_ShouldSucceed() {
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        userService.updateUserStatus(1L, 0);

        assertEquals(0, testUser.getStatus());
        verify(userMapper).updateById(testUser);
    }
}
