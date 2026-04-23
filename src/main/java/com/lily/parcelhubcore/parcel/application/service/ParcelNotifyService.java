package com.lily.parcelhubcore.parcel.application.service;

import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;

public interface ParcelNotifyService {

    void sendSms(String stationCode, String waybillCode);
}
