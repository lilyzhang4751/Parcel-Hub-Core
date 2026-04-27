package com.lily.parcelhubcore.user.api.request;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StationRegisterRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 8137573301303791399L;

    @NotBlank(message = "站点名称不能为空")
    private String name;

    @NotBlank(message = "负责人不能为空")
    private String principal;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "联系电话不能为空")
    private String contactMobile;

    @NotNull(message = "经度不能为空")
    private BigDecimal longitude;

    @NotNull(message = "纬度不能为空")
    private BigDecimal latitude;

    private LocalTime businessStartTime;

    private LocalTime businessEndTime;
}
