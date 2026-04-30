package com.lily.parcelhubcore.user.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.lily.parcelhubcore.shared.authentication.dto.LoginUser;
import com.lily.parcelhubcore.shared.cache.CacheService;
import com.lily.parcelhubcore.shared.constants.KeyConstants;
import com.lily.parcelhubcore.shared.enums.StationStatusEnum;
import com.lily.parcelhubcore.shared.enums.UserStatusEnum;
import com.lily.parcelhubcore.shared.exception.BusinessException;
import com.lily.parcelhubcore.shared.util.JwtUtils;
import com.lily.parcelhubcore.user.application.command.UserRegisterCommand;
import com.lily.parcelhubcore.user.infrastructure.persistence.entity.UserInfoDO;
import com.lily.parcelhubcore.user.infrastructure.persistence.repository.StationInfoRepository;
import com.lily.parcelhubcore.user.infrastructure.persistence.repository.UserInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

// todo
@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserInfoRepository userInfoRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CacheService cacheService;

    @Mock
    private StationInfoRepository stationInfoRepository;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private LoginServiceImpl loginService;

    private UserRegisterCommand registerCommand;
    private UserInfoDO userInfoDO;
    private LoginUser loginUser;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        registerCommand = new UserRegisterCommand();
        registerCommand.setUsername("testuser");
        registerCommand.setPassword("password123");
        registerCommand.setStationCode("ST0000001001");
        registerCommand.setRole("STAFF");

        userInfoDO = new UserInfoDO();
        userInfoDO.setId(1L);
        userInfoDO.setCode("U0000001001");
        userInfoDO.setUsername("testuser");
        userInfoDO.setPassword("encodedPassword");
        userInfoDO.setStationCode("ST0000001001");
        userInfoDO.setRole("STAFF");
        userInfoDO.setStatus(UserStatusEnum.WORKING.getCode());

        loginUser = new LoginUser(userInfoDO, List.of("STAFF"));

        authentication = new UsernamePasswordAuthenticationToken(loginUser, null);
    }

    @Test
    void register_shouldSaveUser_whenValidCommand() {
        // given
        when(userInfoRepository.existsByUsername("testuser")).thenReturn(false);
        when(stationInfoRepository.existsByCodeAndStatus("ST0000001001", StationStatusEnum.OPERATION.getCode())).thenReturn(true);
        when(userInfoRepository.save(any(UserInfoDO.class))).thenReturn(userInfoDO);

        // when
        loginService.register(registerCommand);

        // then
        verify(userInfoRepository).existsByUsername("testuser");
        verify(stationInfoRepository).existsByCodeAndStatus("ST0000001001", StationStatusEnum.OPERATION.getCode());
        verify(userInfoRepository, times(2)).save(any(UserInfoDO.class));
    }

    @Test
    void register_shouldThrowException_whenUsernameDuplicate() {
        // given
        when(userInfoRepository.existsByUsername("testuser")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> loginService.register(registerCommand))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "USERNAME_DUPLICATE");

        verify(userInfoRepository).existsByUsername("testuser");
        verify(stationInfoRepository, never()).existsByCodeAndStatus(anyString(), any(Integer.class));
        verify(userInfoRepository, never()).save(any(UserInfoDO.class));
    }

    @Test
    void register_shouldThrowException_whenStationNotExist() {
        // given
        when(userInfoRepository.existsByUsername("testuser")).thenReturn(false);
        when(stationInfoRepository.existsByCodeAndStatus("ST0000001001", StationStatusEnum.OPERATION.getCode())).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> loginService.register(registerCommand))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "STATION_NOT_EXIST");

        verify(userInfoRepository).existsByUsername("testuser");
        verify(stationInfoRepository).existsByCodeAndStatus("ST0000001001", StationStatusEnum.OPERATION.getCode());
        verify(userInfoRepository, never()).save(any(UserInfoDO.class));
    }

    @Test
    void register_shouldThrowException_whenSaveFails() {
        // given
        when(userInfoRepository.existsByUsername("testuser")).thenReturn(false);
        when(stationInfoRepository.existsByCodeAndStatus("ST0000001001", StationStatusEnum.OPERATION.getCode())).thenReturn(true);
        when(userInfoRepository.save(any(UserInfoDO.class))).thenThrow(new RuntimeException("Database error"));

        // when & then
        assertThatThrownBy(() -> loginService.register(registerCommand))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");

        verify(userInfoRepository).existsByUsername("testuser");
        verify(stationInfoRepository).existsByCodeAndStatus("ST0000001001", StationStatusEnum.OPERATION.getCode());
        verify(userInfoRepository).save(any(UserInfoDO.class));
    }

    @Test
    void login_shouldReturnToken_whenAuthenticationSuccess() {
        // given
        String expectedToken = "jwt.token.here";
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtUtils.generateToken("U0000001001")).thenReturn(expectedToken);
        doNothing().when(cacheService).set(eq(KeyConstants.getLoginRedisKey("U0000001001")), any(LoginUser.class));

        // when
        String token = loginService.login("testuser", "password123");

        // then
        assertThat(token).isEqualTo(expectedToken);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(cacheService).set(eq(KeyConstants.getLoginRedisKey("U0000001001")), any(LoginUser.class));
        verify(jwtUtils).generateToken("U0000001001");
    }

    @Test
    void login_shouldThrowException_whenAuthenticationFails() {
        // given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // when & then
        assertThatThrownBy(() -> loginService.login("testuser", "wrongpassword"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Bad credentials");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(cacheService, never()).set(anyString(), any());
        verify(jwtUtils, never()).generateToken(anyString());
    }

    @Test
    void login_shouldThrowException_whenLoginUserIsNull() {
        // given
        Authentication authWithoutPrincipal = new UsernamePasswordAuthenticationToken(null, null);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authWithoutPrincipal);

        // when & then
        assertThatThrownBy(() -> loginService.login("testuser", "password123"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "AUTHENTICATION_FAILED");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(cacheService, never()).set(anyString(), any());
        verify(jwtUtils, never()).generateToken(anyString());
    }

    @Test
    void login_shouldThrowException_whenCacheServiceFails() {
        // given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        doThrow(new RuntimeException("Redis error")).when(cacheService).set(eq(KeyConstants.getLoginRedisKey("U0000001001")), any(LoginUser.class));

        // when & then
        assertThatThrownBy(() -> loginService.login("testuser", "password123"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Redis error");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(cacheService).set(eq(KeyConstants.getLoginRedisKey("U0000001001")), any(LoginUser.class));
        verify(jwtUtils, never()).generateToken(anyString());
    }

    @Test
    void logout_shouldDeleteCache_whenAuthenticationExists() {
        // given
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(cacheService.delete(KeyConstants.getLoginRedisKey("U0000001001"))).thenReturn(true);

        // when
        loginService.logout();

        // then
        verify(cacheService).delete(KeyConstants.getLoginRedisKey("U0000001001"));
    }

    @Test
    void logout_shouldThrowException_whenAuthenticationIsNull() {
        // given
        SecurityContextHolder.getContext().setAuthentication(null);

        // when & then
        assertThatThrownBy(() -> loginService.logout())
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "AUTHENTICATION_FAILED");

        verify(cacheService, never()).delete(anyString());
    }

    @Test
    void logout_shouldThrowException_whenPrincipalIsNull() {
        // given
        Authentication authWithoutPrincipal = new UsernamePasswordAuthenticationToken(null, null);
        SecurityContextHolder.getContext().setAuthentication(authWithoutPrincipal);

        // when & then
        assertThatThrownBy(() -> loginService.logout())
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "AUTHENTICATION_FAILED");

        verify(cacheService, never()).delete(anyString());
    }

    @Test
    void logout_shouldThrowException_whenCacheDeleteFails() {
        // given
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(cacheService.delete(KeyConstants.getLoginRedisKey("U0000001001"))).thenThrow(new RuntimeException("Redis error"));

        // when & then
        assertThatThrownBy(() -> loginService.logout())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Redis error");

        verify(cacheService).delete(KeyConstants.getLoginRedisKey("U0000001001"));
    }
}