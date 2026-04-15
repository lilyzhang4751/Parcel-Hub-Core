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
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasAnyAuthority('MANAGER','STAFF')")
public class ParcelOpController {

    @Resource
    private ParcelOpService parcelOpService;

    @GetMapping("/hello")
    public String prepareIn() {
        return "hello,lily";
    }

    @PostMapping("/parcel/prepare/in")
    public BaseResponse<PrepareInResponse> prepareIn(@RequestBody PrepareInRequest request) {
        var bo = new PrepareInCommand();
        BeanUtils.copyProperties(request, bo);
        var dto = parcelOpService.prepareIn(bo);
        var response = new PrepareInResponse();
        BeanUtils.copyProperties(dto, response);
        return BaseResponse.success(response);
    }

    @PostMapping("/parcel/inbound")
    public BaseResponse<OpResultResponse> inbound(@RequestBody InboundRequest request) {
        var command = new ParcelInBoundCommand();
        BeanUtils.copyProperties(request, command);
        parcelOpService.inbound(command);
        return BaseResponse.success(OpResultResponse.builder().result(true).build());
    }

    @PostMapping("/parcel/outbound")
    public BaseResponse<OpResultResponse> outbound(String waybillCode) {
        parcelOpService.outBoundOrReturn(waybillCode, OperateTypeEnum.OUT);
        return BaseResponse.success(OpResultResponse.builder().result(true).build());
    }

    @PostMapping("/parcel/returned")
    public BaseResponse<OpResultResponse> returned(String waybillCode) {
        parcelOpService.outBoundOrReturn(waybillCode, OperateTypeEnum.RETURN);
        return BaseResponse.success(OpResultResponse.builder().result(true).build());
    }

    // todo 接口通了，但是只要是异常，都变成了认证失败；移库的操作记录没有货架号取件码，这样看不到修改记录
    @PostMapping("/parcel/transfer")
    public BaseResponse<OpResultResponse> transfer(String waybillCode, String shelfCode) {
        parcelOpService.transfer(waybillCode, shelfCode);
        return BaseResponse.success(OpResultResponse.builder().result(true).build());
    }

}
