package com.lily.parcelhubcore.shared.handler;

import static com.lily.parcelhubcore.shared.exception.ErrorCode.AUTHENTICATION_FAILED;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.lily.parcelhubcore.shared.response.BaseResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        // 设置 HTTP 状态码为 401（未认证）
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // 失败响应
        BaseResponse<Void> baseResponse = BaseResponse.fail(AUTHENTICATION_FAILED.getCode(), authException.getMessage());

        // 写入响应体
        response.getWriter().write(objectMapper.writeValueAsString(baseResponse));

    }
}
