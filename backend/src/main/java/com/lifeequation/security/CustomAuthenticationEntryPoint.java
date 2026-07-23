package com.lifeequation.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Returns 401 JSON in the project's Result format instead of Spring Security's default.
 * Response: {"code":401,"message":"未登录或登录已过期","data":null}
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401);
        Map<String, Object> body = Map.of(
                "code", 401,
                "message", "未登录或登录已过期",
                "data", (Object) null
        );
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
