package com.lily.parcelhubcore.parcel.domain.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {

    CURRENT_EXCEPTION("LOCK_FAILED", "当前包裹正在操作，请稍后重试"),
    PARCEL_EXIST("PARCEL_EXIST", "当前包裹已入库，无法操作"),
    NO_AVAILABLE_PICKUP("NO_AVAILABLE_PICKUP", "没有可用取件码，请修改货架号"),
    PICKUP_OCCUPIED("PICKUP_OCCUPIED", "取件码重复，请修改取件码后再入库"),

    ;

    private String code;

    private String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

}
