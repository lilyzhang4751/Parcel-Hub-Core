package com.lily.parcelhubcore.parcel.application.service;

import com.lily.parcelhubcore.parcel.api.response.ParcelBaseInfoDTO;
import com.lily.parcelhubcore.parcel.api.response.ParcelDetailDTO;
import com.lily.parcelhubcore.parcel.application.query.ParcelPageQuery;
import com.lily.parcelhubcore.shared.response.PageResponse;
import org.springframework.validation.annotation.Validated;

@Validated
public interface ParcelQueryService {

    ParcelDetailDTO querySingleParcel(String stationCode, String waybillCode);

    PageResponse<ParcelBaseInfoDTO> pageQuery(ParcelPageQuery query);

}
