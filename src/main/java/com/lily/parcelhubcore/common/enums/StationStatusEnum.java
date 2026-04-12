package com.lily.parcelhubcore.common.enums;

import lombok.Getter;

@Getter
public enum StationStatusEnum {

    /**
     * 站点状态：0-待激活，1-运营中，2-退出中，3-已退出
     */
    INACTIVE(10, "待激活"),
    OPERATION(20, "运营中"),
    EXITING(30, "退出中"),
    EXITED(40, "已退出");

    private final Integer code;
    private final String desc;

    StationStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
