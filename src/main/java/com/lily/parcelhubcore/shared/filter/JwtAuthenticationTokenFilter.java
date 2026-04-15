package com.lily.parcelhubcore.shared.filter;

import java.io.IOException;
import java.util.Objects;

import com.alibaba.fastjson2.JSON;
import com.lily.parcelhubcore.shared.authentication.dto.LoginUser;
import com.lily.parcelhubcore.shared.cache.CacheService;
import com.lily.parcelhubcore.shared.constants.KeyConstants;
import com.lily.parcelhubcore.shared.util.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

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
    }
}
