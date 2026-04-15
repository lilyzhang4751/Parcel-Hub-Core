package com.lily.parcelhubcore.user.application.service.impl;

import com.lily.parcelhubcore.shared.authentication.dto.LoginUser;
import com.lily.parcelhubcore.shared.cache.CacheService;
import com.lily.parcelhubcore.shared.constants.KeyConstants;
import com.lily.parcelhubcore.shared.util.JwtUtils;
import com.lily.parcelhubcore.user.application.service.LoginService;
import jakarta.annotation.Resource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private CacheService cacheService;

    @Override
    public String login(String userName, String password) {
        //  调用方法authenticationManager.authenticate 进行认证
        var authenticationToken = new UsernamePasswordAuthenticationToken(userName, password);
        var authentication = authenticationManager.authenticate(authenticationToken);

        // 认证成功，完整信息存入redis，key为userCode
        var loginUser = (LoginUser) authentication.getPrincipal();
        var userCode = loginUser.getUser().getCode();
        cacheService.set(KeyConstants.getLoginRedisKey(userCode), loginUser);

        // jwt用userCode生成token
        return JwtUtils.generateToken(userCode);
    }

    @Override
    public void logout() {
        // 获取SecurityContextHolder中的用户
        var authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var loginUser = (LoginUser) authentication.getPrincipal();
        var userCode = loginUser.getUser().getCode();
        // 删除redis
        cacheService.delete(KeyConstants.getLoginRedisKey(userCode));
    }
}
