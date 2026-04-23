package com.lily.parcelhubcore.parcel.api.response;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;

@Data
public class PrepareInResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = -2662477062701038256L;

    private String waybillCode;

    private String shelfCode;

    private String pickupCode;

    private String recipientName;

    private String recipientMobile;
}
