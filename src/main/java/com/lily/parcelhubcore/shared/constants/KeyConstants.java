package com.lily.parcelhubcore.shared.constants;

public class KeyConstants {

    private static final String LOGIN_REDIS_KEY = "LOGIN_KEY:%s";

    public static String getLoginRedisKey(String userCode) {
        return String.format(LOGIN_REDIS_KEY, userCode);
    }
}
