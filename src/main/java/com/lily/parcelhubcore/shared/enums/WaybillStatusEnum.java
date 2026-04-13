package com.lily.parcelhubcore.shared.enums;

import lombok.Getter;

@Getter
public enum WaybillStatusEnum {
    /**
     * 运单状态：10-已入库 20-已出库 30-已退回
     */
    INBOUND(10, "已入库"),
    OUTBOUND(20, "已出库"),
    RETURNED(30, "已退回");

    private final Integer code;
    private final String desc;

    WaybillStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
