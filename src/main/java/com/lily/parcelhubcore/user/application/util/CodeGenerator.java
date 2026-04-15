package com.lily.parcelhubcore.user.application.util;

public class CodeGenerator {

    public static String fromId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
        return "ST" + String.format("%07d", id);
    }
}
