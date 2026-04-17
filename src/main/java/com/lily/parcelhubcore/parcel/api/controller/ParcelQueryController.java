package com.lily.parcelhubcore.parcel.api.controller;

import com.lily.parcelhubcore.parcel.api.request.ParcelQueryRequest;
import com.lily.parcelhubcore.parcel.api.response.ParcelQueryResultResponse;
import com.lily.parcelhubcore.parcel.application.query.ParcelQuery;
import com.lily.parcelhubcore.parcel.application.service.ParcelQueryService;
import com.lily.parcelhubcore.shared.response.BaseResponse;
import com.lily.parcelhubcore.shared.util.CurrentUserUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@PreAuthorize("hasAnyAuthority('MANAGER','STAFF')")
@RequestMapping("/parcel")
public class ParcelQueryController {

    @Resource
    private ParcelQueryService parcelQueryService;

    // todo 批量查询时的默认排序问题，是否需要加在库条件？增加单独分页查询
    @GetMapping
    public BaseResponse<ParcelQueryResultResponse> queryAllParcel(@ModelAttribute ParcelQueryRequest request) {
        var query = new ParcelQuery();
        BeanUtils.copyProperties(request, query);
        query.setStationCode(CurrentUserUtil.getStationCode());
        var parcelList = parcelQueryService.queryAllParcel(query);
        return BaseResponse.success(ParcelQueryResultResponse.builder()
                .total(parcelList.size()).parcelList(parcelList).build());
    }
}
