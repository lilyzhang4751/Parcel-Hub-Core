package com.lily.parcelhubcore.shared.enums;

import lombok.Getter;

@Getter
public enum WaybillStatusEnum implements IntCodeEnum {
    /**
     * 运单状态：10-已入库 20-已出库 30-已退回
     */
    INBOUND(10, "已入库"),
    OUTBOUND(20, "已出库"),
    RETURNED(30, "已退回");

    private final int code;
    private final String desc;

    WaybillStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public int getCode() {
        return code;
    }
}
