package com.lily.parcelhubcore.parcel.api.response;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ParcelNotifyRecordDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 9178744728067879561L;

    private String stationCode;

    private String waybillCode;

    private String mobile;

    private Long notifyTime;

    @Schema(description = "通知渠道：SMS APP")
    private String channel;

    @Schema(description = "通知状态：0-未通知 1-通知中 2-通知成功 3-通知失败")
    private Integer status;

    private String content;

    private String operatorCode;

    private String operatorName;

    private String uniqueId;
}
