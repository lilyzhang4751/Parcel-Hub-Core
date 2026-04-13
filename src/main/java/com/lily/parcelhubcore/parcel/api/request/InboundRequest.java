package com.lily.parcelhubcore.parcel.api.request;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;

@Data
public class InboundRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 4198926573248529916L;

    // todo delete
    private String stationCode;

    private String waybillCode;

    private String shelfCode;

    private String pickupCode;

    private String recipientName;

    private String recipientMobile;
}
