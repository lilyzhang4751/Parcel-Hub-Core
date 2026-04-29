package com.lily.parcelhubcore.parcel.api.controller;

import static com.lily.parcelhubcore.parcel.common.constants.Constants.SHELF_CODE_REGEXP;

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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@PreAuthorize("hasAnyAuthority('MANAGER','STAFF')")
@RequestMapping("/parcel")
@Tag(name = "包裹操作", description = "包裹主要操作相关接口")
public class ParcelOpController {

    @Resource
    private ParcelOpService parcelOpService;

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
    public BaseResponse<OpResultResponse> outbound(@NotBlank(message = "运单号不能为空") String waybillCode) {
        parcelOpService.outBoundOrReturn(waybillCode, OperateTypeEnum.OUT);
        return BaseResponse.success(OpResultResponse.builder().result(true).build());
    }

    @PostMapping("/returned")
    public BaseResponse<OpResultResponse> returned(@NotBlank(message = "运单号不能为空") String waybillCode) {
        parcelOpService.outBoundOrReturn(waybillCode, OperateTypeEnum.RETURN);
        return BaseResponse.success(OpResultResponse.builder().result(true).build());
    }

    @PostMapping("/transfer")
    public BaseResponse<OpResultResponse> transfer(@NotBlank(message = "运单号不能为空") String waybillCode,
                                                   @NotBlank(message = "货架号不能为空")
                                                   @Pattern(regexp = SHELF_CODE_REGEXP, message = "货架号格式错误") String shelfCode) {
        parcelOpService.transfer(waybillCode, shelfCode);
        return BaseResponse.success(OpResultResponse.builder().result(true).build());
    }

}
