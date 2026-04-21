package com.lily.parcelhubcore.parcel.application.service.impl;

import java.util.UUID;

import com.lily.parcelhubcore.parcel.application.service.ParcelNotifyService;
import com.lily.parcelhubcore.parcel.domain.service.NotifyBuilder;
import com.lily.parcelhubcore.parcel.domain.service.ParcelDomainService;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.ParcelNotifyRecord;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.repository.ParcelNotifyRecordRepository;
import com.lily.parcelhubcore.shared.enums.NotifyChannelEnum;
import com.lily.parcelhubcore.shared.enums.NotifyStatusEnum;
import com.lily.parcelhubcore.shared.util.CurrentUserUtil;
import com.lily.parcelhubcore.shared.util.TimeConvertUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class ParcelNotifyServiceImpl implements ParcelNotifyService {

    @Resource
    protected ParcelDomainService parcelDomainService;

    @Resource
    protected ParcelNotifyRecordRepository parcelNotifyRecordRepository;

    @Resource
    protected NotifyBuilder notifyBuilder;

    @Override
    public void sendSms(String stationCode, String waybillCode) {
        // 查询在库包裹
        var parcel = parcelDomainService.getInboundParcelDO(stationCode, waybillCode);
        // 在库则发送短信
        var notifyRecord = new ParcelNotifyRecord();
        notifyRecord.setStationCode(stationCode);
        notifyRecord.setWaybillCode(waybillCode);
        notifyRecord.setMobile(parcel.getRecipientMobile());
        notifyRecord.setOperatorCode(CurrentUserUtil.getUserCode());
        notifyRecord.setOperatorName(CurrentUserUtil.getUsername());
        notifyRecord.setNotifyTime(TimeConvertUtils.toInstant(System.currentTimeMillis()));
        notifyRecord.setChannel(NotifyChannelEnum.SMS.getDesc());
        notifyRecord.setUniqueId(UUID.randomUUID().toString());
        notifyRecord.setStatus(NotifyStatusEnum.NOTIFYING.getCode());
        var content = notifyBuilder.buildSmsContent(stationCode,parcel.getPickupCode());
        notifyRecord.setContent(content);
        parcelNotifyRecordRepository.save(notifyRecord);
        // todo 实际发送消息给下游调三方接口发短信
    }
}
