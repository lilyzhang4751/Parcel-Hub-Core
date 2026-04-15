package com.lily.parcelhubcore.shared.enums;

import lombok.Getter;

@Getter
public enum UserRole {

    MANAGER("MANAGER", "管理员"),
    STAFF("STAFF", "员工");

    private final String role;
    private final String desc;

    UserRole(String role, String desc) {
        this.role = role;
        this.desc = desc;
    }
}
