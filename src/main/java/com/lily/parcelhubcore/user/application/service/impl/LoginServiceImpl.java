package com.lily.parcelhubcore.user.application.service.impl;

import static com.lily.parcelhubcore.shared.exception.ErrorCode.AUTHENTICATION_FAILED;

import com.lily.parcelhubcore.shared.authentication.dto.LoginUser;
import com.lily.parcelhubcore.shared.cache.CacheService;
import com.lily.parcelhubcore.shared.constants.KeyConstants;
import com.lily.parcelhubcore.shared.enums.StationStatusEnum;
import com.lily.parcelhubcore.shared.enums.UserStatusEnum;
import com.lily.parcelhubcore.shared.exception.BusinessException;
import com.lily.parcelhubcore.shared.util.JwtUtils;
import com.lily.parcelhubcore.user.application.command.UserRegisterCommand;
import com.lily.parcelhubcore.user.application.service.LoginService;
import com.lily.parcelhubcore.user.application.util.CodeGenerator;
import com.lily.parcelhubcore.user.infrastructure.persistence.entity.UserInfoDO;
import com.lily.parcelhubcore.user.infrastructure.persistence.repository.StationInfoRepository;
import com.lily.parcelhubcore.user.infrastructure.persistence.repository.UserInfoRepository;
import com.lily.parcelhubcore.user.shared.enums.ErrorCode;
import jakarta.annotation.Resource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginServiceImpl implements LoginService {

    @Resource
    private UserInfoRepository userInfoRepository;

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private CacheService cacheService;

    @Resource
    private StationInfoRepository stationInfoRepository;

    @Override
    @Transactional
    public void register(UserRegisterCommand command) {
        var stationCode = command.getStationCode();
        var stationExist = stationInfoRepository.existsByCodeAndStatus(stationCode, StationStatusEnum.OPERATION.getCode());
        if (!stationExist) {
            throw new BusinessException(ErrorCode.STATION_NOT_EXIST);
        }
        var user = new UserInfoDO();
        user.setStationCode(stationCode);
        user.setUsername(command.getUsername());
        user.setRole(command.getRole());
        user.setStatus(UserStatusEnum.WORKING.getCode());
        // 密码用单向加密
        var encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(command.getPassword()));
        var id = userInfoRepository.save(user).getId();
        user.setCode(CodeGenerator.buildUserCode(id));
        userInfoRepository.save(user);
    }

    @Override
    public String login(String username, String password) {
        //  调用方法authenticationManager.authenticate 进行认证
        var authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        var authentication = authenticationManager.authenticate(authenticationToken);

        // 认证成功，完整信息存入redis，key为userCode
        var loginUser = (LoginUser) authentication.getPrincipal();
        if (loginUser == null) {
            throw new BusinessException(AUTHENTICATION_FAILED);
        }
        var userCode = loginUser.getUserCode();
        cacheService.set(KeyConstants.getLoginRedisKey(userCode), loginUser);

        // jwt用userCode生成token
        return JwtUtils.generateToken(userCode);
    }

    @Override
    public void logout() {
        // 获取SecurityContextHolder中的用户
        var authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var loginUser = (LoginUser) authentication.getPrincipal();
        if (loginUser == null) {
            throw new BusinessException(AUTHENTICATION_FAILED);
        }
        var userCode = loginUser.getUserCode();
        // 删除redis
        cacheService.delete(KeyConstants.getLoginRedisKey(userCode));
    }
}
