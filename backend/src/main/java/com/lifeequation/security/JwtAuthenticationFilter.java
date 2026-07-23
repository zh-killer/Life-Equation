package com.lifeequation.security;

import com.lifeequation.common.JwtUtils;
import com.lifeequation.service.TokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;

/**
 * JWT authentication filter that runs before Spring Security's authentication filter.
 * Replaces the old JwtInterceptor.
 *
 * Flow:
 * 1. Extract Bearer token from Authorization header
 * 2. Parse and validate JWT
 * 3. Check Redis blacklist (logout/password change)
 * 4. Check user invalidation (admin disable)
 * 5. Check login state (active session in Redis)
 * 6. Set SecurityContext with authenticated user
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtils jwtUtils;
    private final TokenService tokenService;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, TokenService tokenService) {
        this.jwtUtils = jwtUtils;
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 1. Parse JWT (gracefully handles expired tokens)
            Claims claims = jwtUtils.parseTokenClaimsGracefully(token);

            // 2. Check expiration
            if (claims.getExpiration().before(new Date())) {
                log.debug("JWT expired");
                filterChain.doFilter(request, response);
                return;
            }

            Long userId = claims.get("userId", Long.class);
            String username = claims.getSubject();
            String role = claims.get("role", String.class);
            Date issuedAt = claims.getIssuedAt();

            // 3. Check Redis blacklist (token explicitly revoked via logout/password change)
            if (tokenService.isBlacklisted(token)) {
                log.debug("Token is blacklisted");
                filterChain.doFilter(request, response);
                return;
            }

            // 4. Check user-level invalidation (admin disabled user)
            if (tokenService.isUserTokenInvalid(userId, issuedAt)) {
                log.debug("User {} token invalidated", userId);
                filterChain.doFilter(request, response);
                return;
            }

            // 5. Check login state in Redis (positive session check)
            if (!tokenService.isLoginActive(userId)) {
                log.debug("User {} has no active login session", userId);
                filterChain.doFilter(request, response);
                return;
            }

            // 6. Build authentication and set SecurityContext
            UserDetailsImpl userDetails = new UserDetailsImpl(userId, username, role);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 7. Temporary: also set request attribute for backward compatibility
            request.setAttribute("userId", userId);
            request.setAttribute("role", role);

            log.debug("JWT authenticated: userId={}, role={}", userId, role);

        } catch (Exception e) {
            // Malformed JWT or other parsing errors — don't authenticate
            log.debug("JWT parsing failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
