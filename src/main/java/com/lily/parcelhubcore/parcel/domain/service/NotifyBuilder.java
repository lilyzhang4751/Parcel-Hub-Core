package com.lily.parcelhubcore.parcel.domain.service;

import java.util.HashMap;

import com.lily.parcelhubcore.user.application.service.StationService;
import jakarta.annotation.Resource;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Component;

@Component
public class NotifyBuilder {

    private static final String SMS_TEMPLATE = "【lily驿站】你的包裹已到${stationName}，请凭${pickupCode}取包裹，详询${contactMobile}";

    // todo 改成服务间通信
    @Resource
    private StationService stationService;

    public String buildSmsContent(String stationCode, String pickupCode) {
        var station = stationService.queryByStationCode(stationCode);
        var map = new HashMap<String, String>();
        map.put("stationName", station.getName());
        map.put("contactMobile", station.getContactMobile());
        map.put("pickupCode", pickupCode);
        return StringSubstitutor.replace(SMS_TEMPLATE, map);
    }

}
