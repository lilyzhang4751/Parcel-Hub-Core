package com.lily.parcelhubcore.parcel.api.request;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;

@Data
public class PrepareInRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 6960634266531481436L;

    //todo delete
    private String stationCode;

    private String waybillCode;

    private String shelfCode;

    private String recipientName;

    private String recipientMobile;
}
