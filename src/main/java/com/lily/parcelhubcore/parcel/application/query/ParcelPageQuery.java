package com.lily.parcelhubcore.parcel.application.query;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Validated
@Data
public class ParcelPageQuery {

    @NotBlank
    private String stationCode;

    private String shelfCode;

    private String pickupCode;

    private String mobile;

    private Integer waybillStatus;

    private Integer notifyStatus;

    @NotNull
    private Integer pageNum;

    @NotNull
    private Integer pageSize;


}
