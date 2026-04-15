package com.lily.parcelhubcore.user.api.controller;

import com.lily.parcelhubcore.shared.response.BaseResponse;
import com.lily.parcelhubcore.user.api.request.LoginRequest;
import com.lily.parcelhubcore.user.api.response.LoginResponse;
import com.lily.parcelhubcore.user.application.service.LoginService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    @Resource
    private LoginService loginService;

    @PostMapping("/user/login")
    public BaseResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        var token = loginService.login(request.getUserName(), request.getPassword());
        var response = new LoginResponse();
        response.setToken(token);
        return BaseResponse.success(response);
    }

    @GetMapping("/user/logout")
    public BaseResponse<LoginResponse> logout() {
        loginService.logout();
        return BaseResponse.success();
    }

}
