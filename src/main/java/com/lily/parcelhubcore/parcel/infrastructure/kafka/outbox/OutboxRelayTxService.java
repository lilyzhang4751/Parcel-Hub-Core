package com.lily.parcelhubcore.parcel.infrastructure.kafka.outbox;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.MessageOutbox;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.repository.MessageOutboxRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
public class OutboxRelayTxService {

    @Value("${app.outbox.batch-size}")
    private int batchSize;

    @Value("${app.outbox.max-retry}")
    private int maxRetry;

    @Value("${app.outbox.retry-base-seconds}")
    private int retryBaseSeconds;

    @Resource
    private MessageOutboxRepository messageOutboxRepository;

    @Transactional
    public List<MessageOutbox> claimBatch() {
        var rows = messageOutboxRepository.findReadyForPublish(
                List.of(
                        MessageOutbox.Status.NEW.name(),
                        MessageOutbox.Status.FAILED.name()
                ),
                Instant.now(),
                maxRetry,
                batchSize
        );

        if (CollectionUtils.isEmpty(rows)) {
            return new ArrayList<>();
        }

        // 更新成处理中，避免别的实例也查出来处理
        for (MessageOutbox row : rows) {
            var currentRetry = row.getRetryCount() == null ? 0 : row.getRetryCount();
            row.setStatus(MessageOutbox.Status.PROCESSING);
            row.setRetryCount(currentRetry + 1);
            row.setProcessingAt(Instant.now());
        }

        // 显式落库
        return messageOutboxRepository.saveAllAndFlush(rows);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSent(Long id) {
        var row = messageOutboxRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("outbox not found: " + id));

        row.setStatus(MessageOutbox.Status.SENT);
        row.setPublishedAt(Instant.now());
        row.setLastError(null);

        messageOutboxRepository.saveAndFlush(row);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long id, Exception ex) {
        var row = messageOutboxRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("outbox not found: " + id));

        var retryCount = row.getRetryCount() == null ? 0 : row.getRetryCount();
        var delaySeconds = (long) retryBaseSeconds * Math.max(1, retryCount);

        row.setStatus(MessageOutbox.Status.FAILED);
        row.setLastError(truncateError(ex));
        row.setNextRetryAt(Instant.now().plusSeconds(delaySeconds));

        messageOutboxRepository.saveAndFlush(row);
    }

    @Transactional
    public int recoverTimedOutProcessing(int timeoutSeconds, int limit) {
        Instant now = Instant.now();
        Instant deadline = now.minusSeconds(timeoutSeconds);

        return messageOutboxRepository.recoverTimedOutProcessing(
                MessageOutbox.Status.PROCESSING.name(),
                MessageOutbox.Status.FAILED.name(),
                deadline,
                now,
                "processing timeout, auto recovered",
                limit
        );
    }

    private String truncateError(Exception ex) {
        if (ex == null || ex.getMessage() == null) {
            return "unknown error";
        }
        String msg = ex.getMessage();
        return msg.length() <= 1000 ? msg : msg.substring(0, 1000);
    }
}
