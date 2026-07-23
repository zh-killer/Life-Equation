package com.lifeequation.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Returns 403 JSON in the project's Result format instead of Spring Security's default.
 * Response: {"code":403,"message":"权限不足，仅管理员可访问","data":null}
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(403);
        Map<String, Object> body = Map.of(
                "code", 403,
                "message", "权限不足，仅管理员可访问",
                "data", (Object) null
        );
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
