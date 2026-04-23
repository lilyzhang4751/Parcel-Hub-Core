package com.lily.parcelhubcore.parcel.api.controller;

import com.lily.parcelhubcore.parcel.api.request.PageQueryRequest;
import com.lily.parcelhubcore.parcel.api.response.ParcelBaseInfoDTO;
import com.lily.parcelhubcore.parcel.api.response.ParcelDetailDTO;
import com.lily.parcelhubcore.parcel.application.query.ParcelPageQuery;
import com.lily.parcelhubcore.parcel.application.service.ParcelQueryService;
import com.lily.parcelhubcore.shared.response.BaseResponse;
import com.lily.parcelhubcore.shared.response.PageResponse;
import com.lily.parcelhubcore.shared.util.CurrentUserUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "包裹查询")
public class ParcelQueryController {

    @Resource
    private ParcelQueryService parcelQueryService;

    @GetMapping("/{waybillCode}")
    @Operation(
            summary = "根据运单号查询包裹",
            description = "传入运单号，返回包裹详细信息，包括操作记录和通知记录"
    )
    public BaseResponse<ParcelDetailDTO> querySingleParcel(@PathVariable @NotBlank(message = "运单号不能为空") String waybillCode) {
        var parcel = parcelQueryService.querySingleParcel(CurrentUserUtil.getStationCode(), waybillCode);
        return BaseResponse.success(parcel);
    }

    @GetMapping
    @Operation(
            summary = "根据条件查询包裹",
            description = "返回满足条件的包裹列表，不包含操作记录和通知记录"
    )
    public BaseResponse<PageResponse<ParcelBaseInfoDTO>> pageQuery(@ModelAttribute @Valid PageQueryRequest request) {
        var query = new ParcelPageQuery();
        BeanUtils.copyProperties(request, query);
        query.setStationCode(CurrentUserUtil.getStationCode());
        var pageInfo = parcelQueryService.pageQuery(query);
        return BaseResponse.success(pageInfo);
    }
}
