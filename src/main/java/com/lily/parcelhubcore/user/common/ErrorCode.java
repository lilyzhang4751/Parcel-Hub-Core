package com.lily.parcelhubcore.user.common;

import com.lily.parcelhubcore.shared.exception.CommonErrorCode;
import lombok.Getter;

@Getter
public enum ErrorCode implements CommonErrorCode {

    MOBILE_DUPLICATE("MOBILE_DUPLICATE", "手机号重复"),
    USERNAME_DUPLICATE("USERNAME_DUPLICATE", "用户名重复"),

    ;

    private final String code;

    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

}
