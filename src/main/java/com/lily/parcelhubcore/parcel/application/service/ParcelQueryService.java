package com.lily.parcelhubcore.parcel.application.service;

import java.util.List;

import com.lily.parcelhubcore.parcel.api.response.ParcelInfoDTO;
import com.lily.parcelhubcore.parcel.application.query.ParcelQuery;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;

@Validated
public interface ParcelQueryService {

    List<ParcelInfoDTO> parcelQueryService(String stationCode, @NotBlank(message = "取件码不能为空") String pickupCode);

    List<ParcelInfoDTO> queryAllParcel(ParcelQuery request);

}
