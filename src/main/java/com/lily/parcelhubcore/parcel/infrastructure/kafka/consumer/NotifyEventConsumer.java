package com.lily.parcelhubcore.parcel.infrastructure.kafka.consumer;

import com.lily.parcelhubcore.parcel.domain.dto.ParcelNotifyEvent;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.ParcelNotifyRecord;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.repository.ParcelNotifyRecordRepository;
import com.lily.parcelhubcore.shared.cache.CacheService;
import com.lily.parcelhubcore.shared.enums.NotifyChannelEnum;
import com.lily.parcelhubcore.shared.enums.NotifyStatusEnum;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import tools.jackson.databind.ObjectMapper;

public class NotifyEventConsumer {

    protected static final String EXIST = "1";

    @Resource
    protected ObjectMapper objectMapper;

    @Resource
    protected CacheService cacheService;

    @Resource
    protected ParcelNotifyRecordRepository parcelNotifyRecordRepository;

    protected boolean isAlreadyConsumed(String eventId) {
        var exist = cacheService.get(eventId);
        return EXIST.equals(exist);
    }

    protected void setAlreadyConsumed(String eventId) {
        cacheService.set(eventId, EXIST);
    }

    protected void saveParcelNotifyEvent(ParcelNotifyEvent event, NotifyChannelEnum channel) {
        //todo 去重
        var notifyRecord = new ParcelNotifyRecord();
        BeanUtils.copyProperties(event, notifyRecord);
        notifyRecord.setUniqueId(event.getEventId());
        notifyRecord.setStatus(NotifyStatusEnum.NOTIFYING.getCode());
        notifyRecord.setChannel(channel.getCode());
        parcelNotifyRecordRepository.save(notifyRecord);
    }
}
