package com.lily.parcelhubcore.parcel.common.enums;

import com.lily.parcelhubcore.shared.exception.CommonErrorCode;
import lombok.Getter;

@Getter
public enum ErrorCode implements CommonErrorCode {

    USER_NOT_EXIST("USER_NOT_EXIST", "用户名或密码错误"),
    STATION_NOT_EXIST("STATION_NOT_EXIST", "站点不存在"),

    CURRENT_EXCEPTION("LOCK_FAILED", "当前包裹正在操作，请稍后重试"),
    PARCEL_ALREADY_EXIST("PARCEL_ALREADY_EXIST", "当前包裹已入库，无法操作"),
    PARCEL_NOT_EXIST("PARCEL_NOT_EXIST", "包裹不存在"),
    PARCEL_NOT_INBOUND("PARCEL_NOT_INBOUND", "包裹不在库，无法操作"),
    NO_AVAILABLE_PICKUP("NO_AVAILABLE_PICKUP", "没有可用取件码，请修改货架号"),
    PICKUP_OCCUPIED("PICKUP_OCCUPIED", "取件码重复，请修改取件码后再入库");

    private final String code;

    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

}
