package com.lily.parcelhubcore.parcel.api.response;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;

@Data
public class ParcelInfoDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -5567108387771247384L;

    private Long id;

    private String stationCode;

    private String waybillCode;

    private String pickupCode;

    private String shelfCode;

    private String recipientName;

    private String recipientMobile;

    private Integer status;

    private Integer notifyStatus;

    private Long latestInboundTime;

    private Long latestOutboundTime;

}
