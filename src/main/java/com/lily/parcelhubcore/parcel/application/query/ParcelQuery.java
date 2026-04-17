package com.lily.parcelhubcore.parcel.application.query;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Validated
@Data
public class ParcelQuery {

    @NotBlank
    private String stationCode;

    private String waybillCode;

    private String shelfCode;

    private String pickupCode;

    private String mobile;
}
