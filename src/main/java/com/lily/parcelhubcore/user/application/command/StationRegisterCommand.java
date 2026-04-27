package com.lily.parcelhubcore.user.application.command;

import java.math.BigDecimal;
import java.time.LocalTime;

import lombok.Data;

@Data
public class StationRegisterCommand {

    private String name;

    private String principal;

    private String password;

    private String contactMobile;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private LocalTime businessStartTime;

    private LocalTime businessEndTime;
}
