package com.lily.parcelhubcore.shared.enums;

import lombok.Getter;

@Getter
public enum NotifyStatusEnum implements IntCodeEnum {
    /**
     * 通知状态：0-未通知 1-通知中 2-通知成功 3-通知失败
     */
    TO_NOTIFY(0, "未通知"),
    NOTIFYING(1, "通知中"),
    SUCCESS(2, "通知成功"),
    FAILED(3, "通知失败");

    private final int code;
    private final String desc;

    NotifyStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    ;

    @Override
    public int getCode() {
        return code;
    }
}
