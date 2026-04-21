package com.lily.parcelhubcore.parcel.infrastructure.kafka.outbox;

import java.util.concurrent.TimeUnit;

import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.MessageOutbox;
import jakarta.annotation.Resource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class OutboxRelayService {

    @Resource
    private OutboxRelayTxService outboxRelayTxService;

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelayString = "${app.outbox.fixed-delay-ms}")
    public void relay() {
        var batch = outboxRelayTxService.claimBatch();
        if (CollectionUtils.isEmpty(batch)) {
            return;
        }
        for (MessageOutbox item : batch) {
            publishOne(item);
        }
    }

    private void publishOne(MessageOutbox item) {
        try {
            var message = MessageBuilder.withPayload(item.getPayload())
                    .setHeader(KafkaHeaders.TOPIC, item.getTopic())
                    .setHeader(KafkaHeaders.KEY, item.getEventKey())
                    .setHeader("eventId", item.getEventId())
                    .build();

            kafkaTemplate.send(message).get(10, TimeUnit.SECONDS);

            outboxRelayTxService.markSent(item.getId());
        } catch (Exception ex) {
            outboxRelayTxService.markFailed(item.getId(), ex);
        }
    }
}
