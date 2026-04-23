package com.lily.parcelhubcore.user.shared.enums;

import com.lily.parcelhubcore.shared.exception.CommonErrorCode;
import lombok.Getter;

@Getter
public enum ErrorCode implements CommonErrorCode {

    USER_NOT_EXIST("USER_NOT_EXIST", "用户名或密码错误"),
    STATION_NOT_EXIST("STATION_NOT_EXIST", "站点不存在或非运行中"),

    ;

    private final String code;

    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

}
