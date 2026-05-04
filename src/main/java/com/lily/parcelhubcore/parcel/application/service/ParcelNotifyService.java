package com.lily.parcelhubcore.parcel.application.service;

public interface ParcelNotifyService {

    void sendSms(String stationCode, String waybillCode);
}
