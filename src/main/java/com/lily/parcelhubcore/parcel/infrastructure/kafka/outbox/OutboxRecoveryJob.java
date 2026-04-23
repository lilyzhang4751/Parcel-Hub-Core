package com.lily.parcelhubcore.parcel.infrastructure.kafka.outbox;

import java.util.concurrent.TimeUnit;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OutboxRecoveryJob {

    @Value("${app.outbox.timeout-s}")
    private int timeoutSeconds;

    @Value("${app.outbox.batch-size}")
    private int batchSize;

    @Resource
    private OutboxRelayTxService outboxRelayTxService;

    @Scheduled(fixedDelayString = "${app.outbox.recovery-s}", timeUnit = TimeUnit.SECONDS)
    public void recoverTimedOutProcessing() {
        var count = outboxRelayTxService.recoverTimedOutProcessing(timeoutSeconds, batchSize);
        if (count > 0) {
            log.info("[OutboxRecoveryJob][共恢复数据{}条]", count);
        }
    }
}
