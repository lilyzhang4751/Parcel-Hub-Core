package com.lily.parcelhubcore.parcel.application.command;

import lombok.Data;

@Data
public class PrepareInCommand {

    private String stationCode;

    private String waybillCode;

    private String shelfCode;

    private String recipientName;

    private String recipientMobile;
}
