package com.lily.parcelhubcore.shared.enums;

import lombok.Getter;

@Getter
public enum UserRole {

    ADMIN(0, "管理员"),
    OPERATOR(1, "员工");

    private final Integer code;
    private final String desc;

    UserRole(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
