package com.lily.parcelhubcore.user.api.controller;

import com.lily.parcelhubcore.shared.response.BaseResponse;
import com.lily.parcelhubcore.user.api.request.LoginRequest;
import com.lily.parcelhubcore.user.api.request.UserRegisterRequest;
import com.lily.parcelhubcore.user.api.response.LoginResponse;
import com.lily.parcelhubcore.shared.response.OpResultResponse;
import com.lily.parcelhubcore.user.application.command.UserRegisterCommand;
import com.lily.parcelhubcore.user.application.service.LoginService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "用户管理")
public class LoginController {

    @Resource
    private LoginService loginService;

    @PostMapping("/user/register")
    public BaseResponse<OpResultResponse> register(@RequestBody UserRegisterRequest request) {
        var command = new UserRegisterCommand();
        BeanUtils.copyProperties(request, command);
        loginService.register(command);
        return BaseResponse.success(OpResultResponse.builder().result(true).build());
    }

    @PostMapping("/user/login")
    public BaseResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        var token = loginService.login(request.getUsername(), request.getPassword());
        var response = new LoginResponse();
        response.setToken(token);
        return BaseResponse.success(response);
    }

    @GetMapping("/user/logout")
    public BaseResponse<OpResultResponse> logout() {
        loginService.logout();
        return BaseResponse.success(OpResultResponse.builder().result(true).build());
    }

}
