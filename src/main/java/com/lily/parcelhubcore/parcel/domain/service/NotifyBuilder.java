package com.lily.parcelhubcore.parcel.domain.service;

import static com.lily.parcelhubcore.parcel.common.enums.ErrorCode.STATION_NOT_EXIST;

import java.util.HashMap;

import com.lily.parcelhubcore.shared.exception.BusinessException;
import com.lily.parcelhubcore.user.infrastructure.persistence.repository.StationInfoRepository;
import jakarta.annotation.Resource;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Component;

@Component
public class NotifyBuilder {

    private static final String SMS_TEMPLATE = "【lily驿站】你的包裹已到${stationName}，请凭${pickupCode}取包裹，详询${contactMobile}";

    // todo 服务间通信
    @Resource
    private StationInfoRepository stationInfoRepository;

    public String buildSmsContent(String stationCode, String pickupCode) {
        var station = stationInfoRepository.findByCode(stationCode);
        if (station == null) {
            throw new BusinessException(STATION_NOT_EXIST);
        }
        var map = new HashMap<String, String>();
        map.put("stationName", station.getName());
        // todo 怎么使得查到的数据直接解密
        map.put("contactMobile", station.getContactMobile());
        map.put("pickupCode", pickupCode);
        return StringSubstitutor.replace(SMS_TEMPLATE, map);
    }

}
