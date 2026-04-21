package com.lily.parcelhubcore.parcel.infrastructure.kafka.outbox;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.MessageOutbox;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
public class OutboxRelayService {

    @Resource
    private OutboxRelayTxService outboxRelayTxService;

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    @Resource
    private Executor outboxRelayExecutor;

    @Scheduled(fixedDelayString = "${app.outbox.fixed-delay-ms}")
    public void relay() {
        var batch = outboxRelayTxService.claimBatch();
        if (CollectionUtils.isEmpty(batch)) {
            return;
        }

        // todo 理解
        var groupedByKey = batch.stream()
                .collect(Collectors.groupingBy(item -> String.valueOf(item.getEventKey())));

        var futures = groupedByKey.values().stream()
                .map(group -> CompletableFuture.runAsync(() -> processGroup(group), outboxRelayExecutor)
                        .exceptionally(ex -> {
                            var eventKey = group.isEmpty() ? null : group.get(0).getEventKey();
                            log.error("outbox publish group failed, eventKey={}", eventKey, ex);
                            return null;
                        }))
                .toList();

        // 等待 batch 全部完成，再结束这次调度
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private void processGroup(List<MessageOutbox> group) {
        group.stream()
                .sorted(Comparator.comparing(
                        MessageOutbox::getCreatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .forEach(this::publishOne);
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
