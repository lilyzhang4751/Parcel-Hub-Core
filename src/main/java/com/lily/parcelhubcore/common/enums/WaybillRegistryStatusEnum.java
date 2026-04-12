package com.lily.parcelhubcore.common.enums;

import lombok.Getter;

@Getter
public enum WaybillRegistryStatusEnum {

    OCCUPIED(0, "占用中"),
    RELEASE(1, "已释放"),
    ERROR(2, "异常");

    private final Integer code;
    private final String desc;

    WaybillRegistryStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     *
     * @param code code
     * @return 枚举值
     */
    public static WaybillRegistryStatusEnum getStatusByCode(Integer code) {
        for (WaybillRegistryStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

}
