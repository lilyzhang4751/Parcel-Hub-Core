package com.lily.parcelhubcore.user.api.controller;

import static com.lily.parcelhubcore.shared.exception.ErrorCode.AUTHENTICATION_FAILED;
import static com.lily.parcelhubcore.user.common.ErrorCode.USERNAME_DUPLICATE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lily.parcelhubcore.shared.exception.BusinessException;
import com.lily.parcelhubcore.shared.filter.JwtAuthenticationTokenFilter;
import com.lily.parcelhubcore.user.api.request.LoginRequest;
import com.lily.parcelhubcore.user.api.request.UserRegisterRequest;
import com.lily.parcelhubcore.user.application.service.LoginService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = LoginController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationTokenFilter.class))
// 让 MockMvc 请求不经过 Spring Security 的过滤器链；不把 application context 里的 filters 注册到 MockMvc
@AutoConfigureMockMvc(addFilters = false)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoginService loginService;

    @Test
    void register_shouldReturnSuccess_whenValidRequest() throws Exception {
        // given
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setStationCode("ST0000001001");
        request.setRole("STAFF");

        doNothing().when(loginService).register(any());

        // when & then
        mockMvc.perform(post("/user/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.code").value("SUCCESS")).andExpect(jsonPath("$.data.result").value(true));

        verify(loginService).register(any());
    }

    @Test
    void register_shouldReturnBadRequest_whenUsernameIsBlank() throws Exception {
        // given
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername("");
        request.setPassword("password123");
        request.setStationCode("ST0000001001");
        request.setRole("STAFF");

        // when & then
        mockMvc.perform(post("/user/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isOk()).andExpect(jsonPath("$.code").value("PARAM_INVALID"));

        verify(loginService, org.mockito.Mockito.never()).register(any());
    }

    @Test
    void register_shouldReturnBadRequest_whenPasswordIsBlank() throws Exception {
        // given
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername("testuser");
        request.setPassword("");
        request.setStationCode("ST0000001001");
        request.setRole("STAFF");

        // when & then
        mockMvc.perform(post("/user/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isOk()).andExpect(jsonPath("$.code").value("PARAM_INVALID"));

        verify(loginService, org.mockito.Mockito.never()).register(any());
    }

    @Test
    void register_shouldReturnInternalServerError_whenServiceThrowsException() throws Exception {
        // given
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setStationCode("ST0000001001");
        request.setRole("STAFF");

        doThrow(new BusinessException(USERNAME_DUPLICATE)).when(loginService).register(any());

        // when & then
        mockMvc.perform(post("/user/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isOk()).andExpect(jsonPath("$.code").value("USERNAME_DUPLICATE"));

        verify(loginService).register(any());
    }

    @Test
    void login_shouldReturnSuccess_whenValidRequest() throws Exception {
        // given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        String expectedToken = "jwt.token.here";
        when(loginService.login("testuser", "password123")).thenReturn(expectedToken);

        // when & then
        mockMvc.perform(post("/user/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.code").value("SUCCESS")).andExpect(jsonPath("$.data.token").value(expectedToken));

        verify(loginService).login("testuser", "password123");
    }

    @Test
    void login_shouldReturnBadRequest_whenUsernameIsBlank() throws Exception {
        // given
        LoginRequest request = new LoginRequest();
        request.setUsername("");
        request.setPassword("password123");

        // when & then
        mockMvc.perform(post("/user/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isOk()).andExpect(jsonPath("$.code").value("PARAM_INVALID"));

        verify(loginService, org.mockito.Mockito.never()).login(anyString(), anyString());
    }

    @Test
    void login_shouldReturnBadRequest_whenPasswordIsBlank() throws Exception {
        // given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("");

        // when & then
        mockMvc.perform(post("/user/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isOk()).andExpect(jsonPath("$.code").value("PARAM_INVALID"));

        verify(loginService, org.mockito.Mockito.never()).login(anyString(), anyString());
    }

    @Test
    void login_shouldReturnInternalServerError_whenServiceThrowsException() throws Exception {
        // given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        when(loginService.login("testuser", "password123")).thenThrow(new BusinessException(AUTHENTICATION_FAILED));

        // when & then
        mockMvc.perform(post("/user/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isOk()).andExpect(jsonPath("$.code").value("AUTHENTICATION_FAILED"));

        verify(loginService).login("testuser", "password123");
    }

    @Test
    void logout_shouldReturnSuccess_whenServiceExecutesNormally() throws Exception {
        // given
        doNothing().when(loginService).logout();

        // when & then
        mockMvc.perform(get("/user/logout")).andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.code").value("SUCCESS")).andExpect(jsonPath("$.data.result").value(true));

        verify(loginService).logout();
    }

    @Test
    void logout_shouldReturnInternalServerError_whenServiceThrowsException() throws Exception {
        // given
        doThrow(new BusinessException(AUTHENTICATION_FAILED)).when(loginService).logout();

        // when & then
        mockMvc.perform(get("/user/logout")).andExpect(status().isOk()).andExpect(jsonPath("$.code").value("AUTHENTICATION_FAILED"));

        verify(loginService).logout();
    }
}