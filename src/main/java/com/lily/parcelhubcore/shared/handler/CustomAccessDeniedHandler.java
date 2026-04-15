package com.lily.parcelhubcore.shared.handler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        // 设置 HTTP 状态码为 403（禁止访问）
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 构建统一 JSON 错误响应
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("code", 403);
        errorBody.put("message", "权限不足，请联系管理员");
        errorBody.put("timestamp", System.currentTimeMillis());

        // 写入响应体
        objectMapper.writeValue(response.getWriter(), errorBody);
    }
}
