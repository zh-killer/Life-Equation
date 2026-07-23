package com.lifeequation.service;

import com.lifeequation.common.JwtUtils;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {

    private static final Logger log = LoggerFactory.getLogger(TokenService.class);
    private static final String BLACKLIST_PREFIX = "token:blacklist:";
    private static final String USER_INVALIDATION_PREFIX = "user:invalidation:";
    private static final String LOGIN_PREFIX = "login:";
    private static final long DEFAULT_TTL_SECONDS = 86400; // 24h fallback

    private final StringRedisTemplate stringRedisTemplate;
    private final JwtUtils jwtUtils;

    public TokenService(StringRedisTemplate stringRedisTemplate, JwtUtils jwtUtils) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Extract a compact identifier from the JWT signature for use as Redis key.
     */
    private String extractTokenId(String token) {
        String[] parts = token.split("\\.");
        if (parts.length >= 3) {
            String sig = parts[2];
            return sig.length() > 20 ? sig.substring(sig.length() - 20) : sig;
        }
        return String.valueOf(token.hashCode());
    }

    /**
     * Calculate the remaining TTL for a token in seconds.
     */
    private long calculateRemainingTtl(String token) {
        try {
            Claims claims = jwtUtils.parseTokenClaimsGracefully(token);
            long remaining = (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000;
            return Math.max(remaining, 1);
        } catch (Exception e) {
            log.warn("Failed to parse token for TTL calculation: {}", e.getMessage());
            return DEFAULT_TTL_SECONDS;
        }
    }

    /**
     * Add a token to the Redis blacklist with its remaining lifetime as TTL.
     * If Redis is unavailable, logs a warning and returns silently.
     */
    public void blacklist(String token) {
        try {
            String tokenId = extractTokenId(token);
            long ttl = calculateRemainingTtl(token);
            stringRedisTemplate.opsForValue().set(BLACKLIST_PREFIX + tokenId, "1", ttl, TimeUnit.SECONDS);
            log.debug("Token blacklisted: {}", tokenId);
        } catch (Exception e) {
            log.warn("Failed to blacklist token in Redis: {}", e.getMessage());
        }
    }

    /**
     * Check whether a token has been blacklisted.
     * Returns false (fail-open) if Redis is unavailable.
     */
    public boolean isBlacklisted(String token) {
        try {
            String tokenId = extractTokenId(token);
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(BLACKLIST_PREFIX + tokenId));
        } catch (Exception e) {
            log.warn("Failed to check token blacklist in Redis: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Set a user-level invalidation timestamp.
     * Tokens issued before this timestamp will be rejected.
     */
    public void setUserInvalidation(Long userId) {
        try {
            stringRedisTemplate.opsForValue().set(
                    USER_INVALIDATION_PREFIX + userId,
                    String.valueOf(System.currentTimeMillis()),
                    DEFAULT_TTL_SECONDS,
                    TimeUnit.SECONDS
            );
            log.debug("User invalidation set: userId={}", userId);
        } catch (Exception e) {
            log.warn("Failed to set user invalidation in Redis: {}", e.getMessage());
        }
    }

    /**
     * Check whether a token was issued before the user was invalidated.
     * Returns false (fail-open) if Redis is unavailable or the check fails.
     */
    public boolean isUserTokenInvalid(Long userId, Date tokenIssuedAt) {
        if (userId == null || tokenIssuedAt == null) return false;
        try {
            String value = stringRedisTemplate.opsForValue().get(USER_INVALIDATION_PREFIX + userId);
            if (value != null) {
                long invalidationTime = Long.parseLong(value);
                return tokenIssuedAt.getTime() < invalidationTime;
            }
            return false;
        } catch (Exception e) {
            log.warn("Failed to check user invalidation in Redis: {}", e.getMessage());
            return false;
        }
    }

    // ==================== Login state management ====================

    /**
     * Record a successful login in Redis.
     * The key tracks which user has an active login session.
     * TTL matches the JWT expiration so the key auto-expires.
     */
    public void recordLogin(Long userId, String token) {
        try {
            long ttl = calculateRemainingTtl(token);
            stringRedisTemplate.opsForValue().set(LOGIN_PREFIX + userId, token, ttl, TimeUnit.SECONDS);
            log.debug("Login recorded: userId={}", userId);
        } catch (Exception e) {
            log.warn("Failed to record login in Redis: {}", e.getMessage());
        }
    }

    /**
     * Check if the user has an active login session.
     * Returns true (fail-open) if Redis is unavailable.
     */
    public boolean isLoginActive(Long userId) {
        if (userId == null) return false;
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(LOGIN_PREFIX + userId));
        } catch (Exception e) {
            log.warn("Failed to check login state in Redis: {}", e.getMessage());
            return true; // fail-open: allow access if Redis is down
        }
    }

    /**
     * Invalidate all login sessions for a user (force logout from all devices).
     * Called when admin disables a user.
     */
    public void invalidateAllSessions(Long userId) {
        try {
            stringRedisTemplate.delete(LOGIN_PREFIX + userId);
            log.debug("All sessions invalidated: userId={}", userId);
        } catch (Exception e) {
            log.warn("Failed to invalidate sessions in Redis: {}", e.getMessage());
        }
    }
}
