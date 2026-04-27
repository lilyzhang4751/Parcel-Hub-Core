package com.lily.parcelhubcore.shared.exception;

import lombok.Getter;

@Getter
public enum ErrorCode implements CommonErrorCode {

    PARAM_INVALID("PARAM_INVALID", "请求参数错误"),
    INTERNAL_ERROR("INTERNAL_ERROR", "系统内部错误"),
    AUTHENTICATION_FAILED("AUTHENTICATION_FAILED", "认证失败，请重新登录"),
    ACCESS_DENIED("403", "权限不足，请联系管理员"),
    ;

    private final String code;

    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

}
