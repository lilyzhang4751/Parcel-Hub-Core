package com.lily.parcelhubcore.shared.filter;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import com.lily.parcelhubcore.shared.authentication.dto.LoginUser;
import com.lily.parcelhubcore.shared.cache.CacheService;
import com.lily.parcelhubcore.shared.constants.KeyConstants;
import com.lily.parcelhubcore.shared.handler.CustomAuthenticationEntryPoint;
import com.lily.parcelhubcore.shared.util.JwtUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private static final String UNKNOWN = "unknown";

    private static final String HEADER_REQUEST_ID = "X-Request-Id";
    private static final String HEADER_FORWARDED_FOR = "X-Forwarded-For";
    private static final String HEADER_REAL_IP = "X-Real-IP";
    private static final String HEADER_TOKEN = "token";

    private static final String MDC_REQUEST_ID = "requestId";
    private static final String MDC_USER_ID = "userId";
    private static final String MDC_CLIENT_IP = "clientIp";

    @Resource
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Resource
    private CacheService cacheService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var start = System.currentTimeMillis();
        // 放入MDC方便后续日志关联
        String requestId = resolveOrGenerateRequestId(request);
        String clientIp = resolveClientIp(request);
        MDC.put(MDC_REQUEST_ID, requestId);
        MDC.put(MDC_CLIENT_IP, clientIp);
        // 也把 requestId 回写给响应头，便于前后端排查
        response.setHeader(HEADER_REQUEST_ID, requestId);

        var token = request.getHeader(HEADER_TOKEN);
        try {
            if (!StringUtils.hasText(token)) {
                // 放行，后续做拦截
                filterChain.doFilter(request, response);
                return;
            }
            // 解析token
            var claims = JwtUtils.parseToken(token);
            var userCode = claims.getSubject();
            MDC.put(MDC_USER_ID, userCode);
            // 从redis获取用户信息
            var cacheKey = KeyConstants.getLoginRedisKey(userCode);
            var loginUser = cacheService.get(cacheKey, LoginUser.class);
            if (Objects.nonNull(loginUser)) {
                var authenticationToken = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
                // 存入SecurityContextHolder
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
            // 放行
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException ex) {
            SecurityContextHolder.clearContext();
            /*
             JWT 过滤器放在 UsernamePasswordAuthenticationFilter前，是在 ExceptionTranslationFilter前面；
             那么这个过滤器里直接 throw new AuthenticationException(...)，异常会直接往容器外冒，
             ExceptionTranslationFilter 根本接不到，所以不会“自动”调 AuthenticationEntryPoint。
             所以只能自己手动调用AuthenticationEntryPoint.commence(...)
             */
            customAuthenticationEntryPoint.commence(
                    request,
                    response,
                    new CredentialsExpiredException("JWT expired", ex)
            );
        } catch (JwtException | SecurityException ex) {
            SecurityContextHolder.clearContext();
            customAuthenticationEntryPoint.commence(
                    request,
                    response,
                    new BadCredentialsException("JWT invalid", ex)
            );
        } finally {
            var method = request.getMethod();
            var uri = request.getRequestURI();
            var query = request.getQueryString();
            var status = response.getStatus();
            var cost = System.currentTimeMillis() - start;

            log.info("[api access][method={}][uri={}][query={}][status={}][costMs={}]",
                    method, uri, query, status, cost);

            MDC.remove(MDC_REQUEST_ID);
            MDC.remove(MDC_USER_ID);
            MDC.remove(MDC_CLIENT_IP);
        }
    }

    private String resolveOrGenerateRequestId(HttpServletRequest request) {
        var requestId = request.getHeader(HEADER_REQUEST_ID);
        if (StringUtils.hasText(requestId)) {
            return requestId;
        }
        // todo
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String resolveClientIp(HttpServletRequest request) {
        var forwardedFor = request.getHeader(HEADER_FORWARDED_FOR);
        if (StringUtils.hasText(forwardedFor) && !UNKNOWN.equalsIgnoreCase(forwardedFor)) {
            int commaIndex = forwardedFor.indexOf(',');
            return commaIndex > 0 ? forwardedFor.substring(0, commaIndex).trim() : forwardedFor.trim();
        }

        var realIp = request.getHeader(HEADER_REAL_IP);
        if (StringUtils.hasText(realIp) && !UNKNOWN.equalsIgnoreCase(realIp)) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }
}
