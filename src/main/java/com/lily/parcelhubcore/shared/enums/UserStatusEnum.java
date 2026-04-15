package com.lily.parcelhubcore.shared.enums;

import lombok.Getter;

@Getter
public enum UserStatusEnum {

    /**
     * 用户状态：0在职 1离职 2休假等
     */
    WORKING(0, "在职"),
    RESIGNED(1, "离职"),
    OFF_WORK(2, "休假");

    private final int code;
    private final String desc;

    UserStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
