package com.lily.parcelhubcore.parcel.infrastructure.kafka.consumer;

import com.alibaba.fastjson2.JSON;
import com.lily.parcelhubcore.parcel.domain.dto.ParcelNotifyEvent;
import com.lily.parcelhubcore.parcel.domain.service.NotifyBuilder;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.ParcelNotifyRecord;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.repository.ParcelNotifyRecordRepository;
import com.lily.parcelhubcore.shared.cache.CacheService;
import com.lily.parcelhubcore.shared.enums.NotifyChannelEnum;
import com.lily.parcelhubcore.shared.enums.NotifyStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import tools.jackson.databind.ObjectMapper;

@Slf4j
public class NotifyEventConsumer {

    protected static final String EXIST = "1";

    @Resource
    protected ObjectMapper objectMapper;

    @Resource
    protected CacheService cacheService;

    @Resource
    protected ParcelNotifyRecordRepository parcelNotifyRecordRepository;

    @Resource
    protected NotifyBuilder notifyBuilder;

    protected boolean isAlreadyConsumed(String eventId) {
        var exist = cacheService.get(eventId);
        return EXIST.equals(exist);
    }

    protected void setAlreadyConsumed(String eventId) {
        cacheService.set(eventId, EXIST);
    }

    protected boolean notNeedHandle(ParcelNotifyEvent event, NotifyChannelEnum channelEnum) {
        var channelList = event.getChannelList();
        if (CollectionUtils.isEmpty(channelList)) {
            log.warn("[NotifyEventConsumer][推送渠道为空不处理][event={}]", JSON.toJSON(event));
            return true;
        }
        return !channelList.contains(channelEnum.getDesc());
    }

    protected void saveParcelNotifyEvent(ParcelNotifyEvent event, NotifyChannelEnum channel) {
        var notifyRecord = new ParcelNotifyRecord();
        BeanUtils.copyProperties(event, notifyRecord);
        notifyRecord.setUniqueId(event.getEventId());
        notifyRecord.setStatus(NotifyStatusEnum.NOTIFYING.getCode());
        notifyRecord.setChannel(channel.getDesc());
        var content = notifyBuilder.buildSmsContent(event.getStationCode(), event.getPickupCode());
        notifyRecord.setContent(content);
        parcelNotifyRecordRepository.save(notifyRecord);
    }
}
