package com.lily.parcelhubcore.shared.enums;

import lombok.Getter;

@Getter
public enum OperateTypeEnum {
    /**
     * 操作类型 100-入库 200-出库 300-退回 400-移库 500-盘库
     */
    IN(100, "入库"),
    OUT(200, "出库"),
    RETURN(300, "退回"),
    TRANSFER(400, "移库"),
    INVENTORY(500, "盘库");

    private final Integer code;
    private final String desc;

    OperateTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
