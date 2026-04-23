package com.lily.parcelhubcore.parcel.api.response;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ParcelOpRecordDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -2929619141404851318L;

    private String stationCode;

    private String waybillCode;

    private Long opTime;

    @Schema(description = "操作类型 100-入库 200-出库 300-退回 400-移库")
    private Integer opType;

    private String detail;

    private String operatorCode;

    private String operatorName;

    private String uniqueId;
}
