package com.lily.parcelhubcore.api.controller;

import com.lily.parcelhubcore.application.service.ParcelOpService;
import com.lily.parcelhubcore.infrastructure.persistence.repository.WaybillRegistryRepository;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ParcelOpController {

    @Resource
    private ParcelOpService parcelOpService;

    @Resource
    private WaybillRegistryRepository waybillRegistryRepository;

    @GetMapping("/parcel/inbound")
    public String inbound(@RequestParam Long userId,
                          @RequestParam String orderNo) {
        parcelOpService.inbound(userId, orderNo);
        var count = waybillRegistryRepository.findById("1");
        return "count:" + count;
    }
}
