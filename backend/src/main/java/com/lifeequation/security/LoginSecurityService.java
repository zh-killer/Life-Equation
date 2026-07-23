package com.lifeequation.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis-based login rate limiting service.
 * After MAX_ATTEMPTS (5) consecutive failures, the account is locked for LOCK_DURATION (10 minutes).
 *
 * Redis keys:
 *   login:attempt:{username}  — failure counter, TTL = LOCK_DURATION
 *   login:lock:{username}     — lock flag, TTL = LOCK_DURATION
 */
@Service
public class LoginSecurityService {

    private static final Logger log = LoggerFactory.getLogger(LoginSecurityService.class);
    private static final String ATTEMPT_PREFIX = "login:attempt:";
    private static final String LOCK_PREFIX = "login:lock:";
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION = 10; // minutes

    private final StringRedisTemplate stringRedisTemplate;

    public LoginSecurityService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * Check if the account is currently locked due to too many failed attempts.
     */
    public boolean isLocked(String username) {
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(LOCK_PREFIX + username));
        } catch (Exception e) {
            log.warn("Failed to check login lock in Redis: {}", e.getMessage());
            return false; // fail-open
        }
    }

    /**
     * Record a failed login attempt. If the threshold is reached, lock the account.
     */
    public void recordFailedAttempt(String username) {
        try {
            String attemptKey = ATTEMPT_PREFIX + username;
            Long count = stringRedisTemplate.opsForValue().increment(attemptKey);
            if (count == 1) {
                stringRedisTemplate.expire(attemptKey, LOCK_DURATION, TimeUnit.MINUTES);
            }
            log.warn("User {} login failed, attempt count: {}", username, count);
            if (count >= MAX_ATTEMPTS) {
                stringRedisTemplate.opsForValue().set(
                        LOCK_PREFIX + username, "1", LOCK_DURATION, TimeUnit.MINUTES);
                log.warn("User {} locked due to {} failed attempts", username, count);
            }
        } catch (Exception e) {
            log.warn("Failed to record login attempt in Redis: {}", e.getMessage());
        }
    }

    /**
     * Clear all failure records and lock on successful login.
     */
    public void resetAttempts(String username) {
        try {
            stringRedisTemplate.delete(ATTEMPT_PREFIX + username);
            stringRedisTemplate.delete(LOCK_PREFIX + username);
        } catch (Exception e) {
            log.warn("Failed to reset login attempts in Redis: {}", e.getMessage());
        }
    }

    /**
     * Get the current failure count for a username.
     */
    public int getAttemptCount(String username) {
        try {
            String value = stringRedisTemplate.opsForValue().get(ATTEMPT_PREFIX + username);
            return value != null ? Integer.parseInt(value) : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
