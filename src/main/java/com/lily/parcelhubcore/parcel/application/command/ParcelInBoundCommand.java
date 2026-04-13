package com.lily.parcelhubcore.parcel.application.command;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;

@Data
public class ParcelInBoundCommand implements Serializable {

    @Serial
    private static final long serialVersionUID = -5833078471996450362L;

    private String stationCode;

    private String waybillCode;

    private String pickupCode;

    private String shelfCode;

    private String recipientName;

    private String recipientMobile;

}
