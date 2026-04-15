package com.lily.parcelhubcore.parcel.api.controller;

import com.lily.parcelhubcore.parcel.api.request.InboundRequest;
import com.lily.parcelhubcore.parcel.api.request.PrepareInRequest;
import com.lily.parcelhubcore.parcel.api.response.InboundResponse;
import com.lily.parcelhubcore.parcel.application.command.ParcelInBoundCommand;
import com.lily.parcelhubcore.parcel.application.command.PrepareInCommand;
import com.lily.parcelhubcore.parcel.application.dto.prepareInDTO;
import com.lily.parcelhubcore.parcel.application.service.ParcelOpService;
import com.lily.parcelhubcore.shared.response.BaseResponse;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ParcelOpController {

    @Resource
    private ParcelOpService parcelOpService;

    @GetMapping("/hello")
    @PreAuthorize("hasAnyAuthority('MANAGER','STAFF')")
    public String prepareIn() {
        return "hello,lily";
    }

    @PostMapping("/parcel/prepare/in")
    public BaseResponse<prepareInDTO> prepareIn(PrepareInRequest request) {
        var bo = new PrepareInCommand();
        BeanUtils.copyProperties(request, bo);
        var dto = parcelOpService.prepareIn(bo);
        return BaseResponse.success(dto);
    }

    @PostMapping("/parcel/inbound")
    public BaseResponse<InboundResponse> inbound(InboundRequest request) {
        var command = new ParcelInBoundCommand();
        BeanUtils.copyProperties(request, command);
        parcelOpService.inbound(command);
        var response = new InboundResponse();
        response.setResult(true);
        return BaseResponse.success(response);
    }
}
