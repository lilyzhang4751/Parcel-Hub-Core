package com.lily.parcelhubcore.parcel.application.dto;

import lombok.Data;

@Data
public class PrepareInDTO {

    private String waybillCode;

    private String shelfCode;

    private String pickupCode;

    private String recipientName;

    private String recipientMobile;
}
