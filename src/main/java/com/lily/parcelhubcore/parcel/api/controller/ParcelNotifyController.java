package com.lily.parcelhubcore.parcel.api.controller;

import com.lily.parcelhubcore.parcel.application.service.ParcelNotifyService;
import com.lily.parcelhubcore.shared.response.BaseResponse;
import com.lily.parcelhubcore.shared.response.OpResultResponse;
import com.lily.parcelhubcore.shared.util.CurrentUserUtil;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@PreAuthorize("hasAnyAuthority('MANAGER')")
@RequestMapping("/notify")
public class ParcelNotifyController {

    @Resource
    private ParcelNotifyService parcelNotifyService;

    @GetMapping("/sms/{waybillCode}")
    public BaseResponse<OpResultResponse> sendSms(@PathVariable @NotBlank(message = "运单号不能为空") String waybillCode) {
        parcelNotifyService.sendSms(CurrentUserUtil.getStationCode(), waybillCode);
        return BaseResponse.success(OpResultResponse.builder().result(true).build());
    }
}
