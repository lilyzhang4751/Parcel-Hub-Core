package com.lily.parcelhubcore.shared.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {

    USER_NOT_EXIST("USER_NOT_EXIST", "用户名或密码错误"),


    ;

    private String code;

    private String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

}
