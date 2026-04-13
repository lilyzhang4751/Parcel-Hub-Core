package com.lily.parcelhubcore.parcel.shared.common;

public class KeyConstants {

    private static final String WAYBILL_CODE_LOCK = "waybillCode:%s";

    private static final String STATION_SHELF_LOCK = "station:%s:shelf:%s";

    private static final String WAYBILL_PICKUP_CACHE_KEY = "cache:station:%s:waybill:%s:piuckup";

    private static final String PICKUP_CODE_POOL_KEY = "pickup:pool:station:%s:shelf:%s";

    public static String getWaybillCodeLock(String waybillCode) {
        return String.format(WAYBILL_CODE_LOCK, waybillCode);
    }

    public static String getStationShelfLock(String stationCode, String shelfCode) {
        return String.format(STATION_SHELF_LOCK, stationCode, shelfCode);
    }

    /**
     * 获取运单号取件码缓存key
     *
     * @param waybillCode 运单号
     * @return 缓存key
     */
    public static String getWaybillPickupCacheKey(String stationCode, String waybillCode) {
        return String.format(WAYBILL_PICKUP_CACHE_KEY, stationCode, waybillCode);
    }

    public static String getPickupCodePoolKey(String stationCode, String shelfCode) {
        return String.format(PICKUP_CODE_POOL_KEY, stationCode, shelfCode);
    }
}
