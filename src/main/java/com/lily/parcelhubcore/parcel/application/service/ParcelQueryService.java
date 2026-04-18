package com.lily.parcelhubcore.parcel.application.service;

import com.lily.parcelhubcore.parcel.api.response.ParcelInfoDTO;
import com.lily.parcelhubcore.parcel.application.query.ParcelPageQuery;
import com.lily.parcelhubcore.shared.response.PageResponse;
import org.springframework.validation.annotation.Validated;

@Validated
public interface ParcelQueryService {

    ParcelInfoDTO querySingleParcel(String stationCode, String waybillCode);

    PageResponse<ParcelInfoDTO> pageQuery(ParcelPageQuery query);

}
