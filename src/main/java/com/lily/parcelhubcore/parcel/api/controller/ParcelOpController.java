package com.lily.parcelhubcore.parcel.api.controller;

import com.lily.parcelhubcore.parcel.api.request.InboundRequest;
import com.lily.parcelhubcore.parcel.api.request.PrepareInRequest;
import com.lily.parcelhubcore.parcel.api.response.PrepareInResponse;
import com.lily.parcelhubcore.parcel.application.command.ParcelInBoundCommand;
import com.lily.parcelhubcore.parcel.application.command.PrepareInCommand;
import com.lily.parcelhubcore.parcel.application.service.ParcelOpService;
import com.lily.parcelhubcore.shared.enums.OperateTypeEnum;
import com.lily.parcelhubcore.shared.response.BaseResponse;
import com.lily.parcelhubcore.shared.response.OpResultResponse;
import com.lily.parcelhubcore.shared.util.CurrentUserUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasAnyAuthority('MANAGER','STAFF')")
@RequestMapping("/parcel")
public class ParcelOpController {

    @Resource
    private ParcelOpService parcelOpService;

    // todo 想办法融入多线程～
    @PostMapping("/prepare/in")
    public BaseResponse<PrepareInResponse> prepareIn(@RequestBody @Valid PrepareInRequest request) {
        var command = new PrepareInCommand();
        BeanUtils.copyProperties(request, command);
        command.setStationCode(CurrentUserUtil.getStationCode());
        var dto = parcelOpService.prepareIn(command);
        var response = new PrepareInResponse();
        BeanUtils.copyProperties(dto, response);
        return BaseResponse.success(response);
    }

    @PostMapping("/inbound")
    public BaseResponse<OpResultResponse> inbound(@RequestBody @Valid InboundRequest request) {
        var command = new ParcelInBoundCommand();
        BeanUtils.copyProperties(request, command);
        command.setStationCode(CurrentUserUtil.getStationCode());
        parcelOpService.inbound(command);
        return BaseResponse.success(OpResultResponse.builder().result(true).build());
    }

    @PostMapping("/outbound")
    public BaseResponse<OpResultResponse> outbound(String waybillCode) {
        parcelOpService.outBoundOrReturn(waybillCode, OperateTypeEnum.OUT);
        return BaseResponse.success(OpResultResponse.builder().result(true).build());
    }

    @PostMapping("/returned")
    public BaseResponse<OpResultResponse> returned(String waybillCode) {
        parcelOpService.outBoundOrReturn(waybillCode, OperateTypeEnum.RETURN);
        return BaseResponse.success(OpResultResponse.builder().result(true).build());
    }

    // todo ；移库的操作记录没有货架号取件码，这样看不到修改记录
    @PostMapping("/transfer")
    public BaseResponse<OpResultResponse> transfer(String waybillCode, String shelfCode) {
        parcelOpService.transfer(waybillCode, shelfCode);
        return BaseResponse.success(OpResultResponse.builder().result(true).build());
    }

}
