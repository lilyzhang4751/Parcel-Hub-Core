package com.lily.parcelhubcore.user.api.request;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalTime;

import lombok.Data;

@Data
public class StationRegisterRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 8137573301303791399L;

    private String name;

    private String principal;

    private String contactMobile;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private LocalTime businessStartTime;

    private LocalTime businessEndTime;
}
