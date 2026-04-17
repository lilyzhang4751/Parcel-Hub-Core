package com.lily.parcelhubcore.shared.filter;

import java.io.IOException;
import java.util.Objects;

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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Resource
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Resource
    private CacheService cacheService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 获取token
        String token = request.getHeader("token");
        if (!StringUtils.hasText(token)) {
            // 放行，后续做拦截
            filterChain.doFilter(request, response);
            return;
        }
        try {
            // 解析token
            var claims = JwtUtils.parseToken(token);
            var userCode = claims.getSubject();
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
        }
    }
}
