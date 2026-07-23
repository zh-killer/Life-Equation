package com.lifeequation.config;

import com.lifeequation.service.TokenService;
import com.lifeequation.security.LoginSecurityService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 测试专用配置 — 为 Redis 相关依赖提供 Mock Bean。
 *
 * 由于测试环境通常不运行 Redis，此配置替换真实 Redis 相关 Bean 为 Mock，
 * 使测试能正常加载 Spring 上下文而不依赖 Redis 基础设施。
 */
@TestConfiguration
public class TestConfig {

    /**
     * Mock StringRedisTemplate — 所有方法默认返回 null / false / 空集合。
     */
    @Bean
    @Primary
    public StringRedisTemplate stringRedisTemplate() {
        return Mockito.mock(StringRedisTemplate.class);
    }

    /**
     * Mock TokenService — 所有方法默认不做任何事。
     */
    @Bean
    @Primary
    public TokenService tokenService() {
        return Mockito.mock(TokenService.class);
    }

    /**
     * Mock LoginSecurityService — 所有方法默认返回 false。
     */
    @Bean
    @Primary
    public LoginSecurityService loginSecurityService() {
        return Mockito.mock(LoginSecurityService.class);
    }
}
