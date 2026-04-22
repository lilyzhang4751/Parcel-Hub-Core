package com.lily.parcelhubcore.parcel.api.response;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "包裹基本信息")
public class ParcelBaseInfoDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -5567108387771247384L;

    private Long id;

    private String stationCode;

    private String waybillCode;

    private String pickupCode;

    private String shelfCode;

    private String recipientName;

    private String recipientMobile;

    @Schema(description = "包裹状态：10-已入库 20-已出库 30-已退回")
    private Integer status;

    @Schema(description = "通知状态：0-未通知 1-通知中 2-通知成功 3-通知失败")
    private Integer notifyStatus;

    private Long latestInboundTime;

    private Long latestOutboundTime;

}
