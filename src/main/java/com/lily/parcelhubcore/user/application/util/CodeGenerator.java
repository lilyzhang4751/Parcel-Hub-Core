package com.lily.parcelhubcore.user.application.util;

import java.util.Random;

public class CodeGenerator {

    public static String buildStationCode(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
        ;
        return "ST" + String.format("%07d", id) + String.format("%03d", new Random().nextInt(1000));
    }

    public static String buildUserCode(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
        return "U" + String.format("%07d", id) + String.format("%03d", new Random().nextInt(1000));
    }
}
