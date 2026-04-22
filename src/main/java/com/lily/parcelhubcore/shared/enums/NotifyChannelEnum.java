package com.lily.parcelhubcore.shared.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.Getter;

@Getter
public enum NotifyChannelEnum implements IntCodeEnum {
    /**
     * 通知渠道：SMS APP
     */
    SMS(1, "SMS"),
    APP(2, "APP");

    public static final List<NotifyChannelEnum> CHANNEL_LIST =
            Collections.unmodifiableList(Arrays.asList(values()));
    private final int code;
    private final String desc;

    NotifyChannelEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据desc获取code
     */
    public static Integer getCodeByDesc(String desc) {
        if (desc == null) {
            return null;
        }
        for (NotifyChannelEnum item : CHANNEL_LIST) {
            if (item.getDesc().equals(desc)) {
                return item.getCode();
            }
        }
        return null;
    }

    @Override
    public int getCode() {
        return code;
    }
}

