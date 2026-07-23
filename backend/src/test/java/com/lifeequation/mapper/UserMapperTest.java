package com.lifeequation.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifeequation.config.TestConfig;
import com.lifeequation.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Mapper 层集成测试。
 *
 * 使用 @SpringBootTest 加载完整上下文，通过 H2 内存数据库测试 MyBatis-Plus
 * Mapper 的 CRUD 操作。使用 @AutoConfigureTestDatabase 配置 H2 替代 MySQL，
 * @Sql 在每个测试方法前创建表结构。
 *
 * 对应文档 7.2 第三步：Mapper 层测试
 */
@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password="
        }
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestConfig.class)  // Import mock beans for Redis-dependent services
@Transactional  // 每个测试方法执行后自动回滚，保持测试数据隔离
@Sql(statements = {
        "DROP TABLE IF EXISTS `user`",
        "CREATE TABLE IF NOT EXISTS `user` (" +
                "  id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "  username VARCHAR(50) NOT NULL," +
                "  password VARCHAR(200) NOT NULL," +
                "  real_name VARCHAR(50)," +
                "  role VARCHAR(20)," +
                "  phone VARCHAR(20)," +
                "  email VARCHAR(100)," +
                "  avatar_url VARCHAR(255)," +
                "  gender INT," +
                "  birth_date DATE," +
                "  status INT DEFAULT 1," +
                "  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@DisplayName("用户 Mapper 集成测试")
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    // ==================== 插入测试 ====================

    @Test
    @DisplayName("测试插入用户 — 自动生成 ID")
    void testInsert() {
        User user = new User();
        user.setUsername("insertTest");
        user.setPassword("encryptedPass");
        user.setRealName("插入测试");
        user.setRole("USER");
        user.setEmail("insert@test.com");
        user.setPhone("13800138000");
        user.setGender(1);
        user.setStatus(1);

        int rows = userMapper.insert(user);

        assertEquals(1, rows);
        assertNotNull(user.getId(), "插入后应自动生成主键 ID");
        System.out.println("[MapperTest] 插入用户成功，ID: " + user.getId());
    }

    @Test
    @DisplayName("测试插入用户 — 验证自动填充时间")
    void testInsertAutoFill() {
        User user = new User();
        user.setUsername("autoFillTest");
        user.setPassword("pass");
        user.setRole("USER");
        user.setStatus(1);

        userMapper.insert(user);

        assertNotNull(user.getCreateTime(), "createTime 应被自动填充");
        assertNotNull(user.getUpdateTime(), "updateTime 应被自动填充");
        System.out.println("[MapperTest] createTime: " + user.getCreateTime());
    }

    // ==================== 查询测试 ====================

    @Test
    @DisplayName("测试根据 ID 查询用户")
    void testSelectById() {
        // Given: 插入一条数据
        User user = new User();
        user.setUsername("selectTest");
        user.setPassword("pass");
        user.setRole("USER");
        user.setEmail("select@test.com");
        user.setStatus(1);
        userMapper.insert(user);

        // When: 根据 ID 查询
        User foundUser = userMapper.selectById(user.getId());

        // Then: 验证查询结果
        assertNotNull(foundUser);
        assertEquals("selectTest", foundUser.getUsername());
        assertEquals("select@test.com", foundUser.getEmail());
        assertEquals("USER", foundUser.getRole());
    }

    @Test
    @DisplayName("测试查询不存在的用户应返回 null")
    void testSelectByIdNotFound() {
        User foundUser = userMapper.selectById(9999L);
        assertNull(foundUser, "查询不存在的ID应返回null");
    }

    @Test
    @DisplayName("测试根据条件查询用户 — selectOne (Lambda)")
    void testSelectOne() {
        User user = new User();
        user.setUsername("queryTest");
        user.setPassword("pass");
        user.setRole("USER");
        user.setEmail("query@test.com");
        user.setStatus(1);
        userMapper.insert(user);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, "query@test.com");
        User foundUser = userMapper.selectOne(wrapper);

        assertNotNull(foundUser);
        assertEquals("queryTest", foundUser.getUsername());
    }

    @Test
    @DisplayName("测试条件查询 — 无匹配时返回 null")
    void testSelectOneNoMatch() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, "nonExistent");
        User foundUser = userMapper.selectOne(wrapper);
        assertNull(foundUser);
    }

    // ==================== 更新测试 ====================

    @Test
    @DisplayName("测试更新用户信息")
    void testUpdate() {
        User user = new User();
        user.setUsername("updateTest");
        user.setPassword("oldPass");
        user.setRole("USER");
        user.setEmail("old@test.com");
        user.setStatus(1);
        userMapper.insert(user);

        user.setEmail("new@test.com");
        user.setRealName("更新后姓名");
        userMapper.updateById(user);

        User updatedUser = userMapper.selectById(user.getId());
        assertEquals("new@test.com", updatedUser.getEmail());
        assertEquals("更新后姓名", updatedUser.getRealName());
    }

    // ==================== 删除测试 ====================

    @Test
    @DisplayName("测试删除用户")
    void testDelete() {
        User user = new User();
        user.setUsername("deleteTest");
        user.setPassword("pass");
        user.setRole("USER");
        user.setEmail("delete@test.com");
        user.setStatus(1);
        userMapper.insert(user);
        Long userId = user.getId();

        int rows = userMapper.deleteById(userId);

        assertEquals(1, rows, "应删除1行记录");
        User deletedUser = userMapper.selectById(userId);
        assertNull(deletedUser, "删除后查询应返回null");
    }

    // ==================== 批量查询测试 ====================

    @Test
    @DisplayName("测试 Lambda 条件构造器 — 多条件查询")
    void testLambdaQueryMultipleConditions() {
        // 插入两条不同角色的用户
        User user1 = new User();
        user1.setUsername("roleAdmin");
        user1.setPassword("pass");
        user1.setRole("ADMIN");
        user1.setStatus(1);
        userMapper.insert(user1);

        User user2 = new User();
        user2.setUsername("roleUser");
        user2.setPassword("pass");
        user2.setRole("USER");
        user2.setStatus(1);
        userMapper.insert(user2);

        // 查询 role=USER 的记录数
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, "USER");
        Long userCount = userMapper.selectCount(wrapper);

        assertEquals(1, userCount, "应该只有1个USER角色用户");
    }
}
