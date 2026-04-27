package com.lily.parcelhubcore.user.api.controller;

import com.lily.parcelhubcore.shared.response.BaseResponse;
import com.lily.parcelhubcore.user.api.request.StationRegisterRequest;
import com.lily.parcelhubcore.user.api.response.StationRegisterResponse;
import com.lily.parcelhubcore.user.application.command.StationRegisterCommand;
import com.lily.parcelhubcore.user.application.service.StationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "站点管理")
public class StationController {

    @Resource
    private StationService stationService;

    @PostMapping("/station/register")
    public BaseResponse<StationRegisterResponse> register(@RequestBody @Valid StationRegisterRequest request) {
        var command = new StationRegisterCommand();
        BeanUtils.copyProperties(request, command);
        // return station code
        var stationCode = stationService.register(command);
        return BaseResponse.success(StationRegisterResponse.builder().stationCode(stationCode).build());
    }
}
