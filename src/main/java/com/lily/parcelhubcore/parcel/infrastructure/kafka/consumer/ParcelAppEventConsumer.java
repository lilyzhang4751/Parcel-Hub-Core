package com.lily.parcelhubcore.parcel.infrastructure.kafka.consumer;

import com.alibaba.fastjson2.JSON;
import com.lily.parcelhubcore.parcel.domain.dto.ParcelNotifyEvent;
import com.lily.parcelhubcore.parcel.infrastructure.kafka.config.KafkaTopicConfig;
import com.lily.parcelhubcore.shared.enums.NotifyChannelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ParcelAppEventConsumer extends NotifyEventConsumer {

    @KafkaListener(
            topics = KafkaTopicConfig.TOPIC_PARCEL_NOTIFY,
            groupId = "${app.consumer.group.parcel-app}",
            containerFactory = "parcelKafkaListenerContainerFactory"
    )
    public void onMessage(
            @Payload String payload,
            @Header("eventId") String eventId,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic
    ) throws Exception {
        // 判断是否已经消费消息
        if (isAlreadyConsumed(eventId)) {
            return;
        }
        var event = objectMapper.readValue(payload, ParcelNotifyEvent.class);
        log.info("[ParcelAppEventConsumer][onMessage][消息开始处理][event={}]", JSON.toJSON(event));
        if (notNeedHandle(event, NotifyChannelEnum.APP)) {
            return;
        }
        saveParcelNotifyEvent(event, NotifyChannelEnum.APP);
        // todo 实际发送短信或app推送，需要三方接口支持，此处省略

        // 处理成功设置缓存
        setAlreadyConsumed(eventId);
        log.info("[ParcelAppEventConsumer][onMessage][APP推送成功][event={}]", JSON.toJSON(event));
    }
}
