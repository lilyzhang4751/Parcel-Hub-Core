package com.lily.parcelhubcore.parcel.domain.service;

public interface PickupCodeService {

    String genarate(String stationCode, String shelfCode);

    void pickupCodeExistVerify(String stationCode, String pickupCode);

}
