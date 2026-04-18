package com.lily.parcelhubcore.parcel.api.controller;

import com.lily.parcelhubcore.parcel.api.request.PageQueryRequest;
import com.lily.parcelhubcore.parcel.api.response.ParcelInfoDTO;
import com.lily.parcelhubcore.parcel.application.query.ParcelPageQuery;
import com.lily.parcelhubcore.parcel.application.service.ParcelQueryService;
import com.lily.parcelhubcore.shared.response.BaseResponse;
import com.lily.parcelhubcore.shared.response.PageResponse;
import com.lily.parcelhubcore.shared.util.CurrentUserUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@PreAuthorize("hasAnyAuthority('MANAGER','STAFF')")
@RequestMapping("/parcels")
public class ParcelQueryController {

    @Resource
    private ParcelQueryService parcelQueryService;

    @GetMapping("/{waybillCode}")
    public BaseResponse<ParcelInfoDTO> querySingleParcel(@PathVariable @NotBlank(message = "运单号不能为空") String waybillCode) {
        var parcel = parcelQueryService.querySingleParcel(CurrentUserUtil.getStationCode(), waybillCode);
        return BaseResponse.success(parcel);
    }

    @GetMapping
    public BaseResponse<PageResponse<ParcelInfoDTO>> pageQuery(@ModelAttribute @Valid PageQueryRequest request) {
        var query = new ParcelPageQuery();
        BeanUtils.copyProperties(request, query);
        query.setStationCode(CurrentUserUtil.getStationCode());
        var pageInfo = parcelQueryService.pageQuery(query);
        return BaseResponse.success(pageInfo);
    }
}
