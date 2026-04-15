package com.lily.parcelhubcore.shared.handler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        // 设置 HTTP 状态码为 401（未认证）
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 构建统一 JSON 错误响应
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("code", 401);
        errorBody.put("message", "认证失败，请重新登录");
        errorBody.put("timestamp", System.currentTimeMillis());

        // 写入响应体
        objectMapper.writeValue(response.getWriter(), errorBody);
    }
}
