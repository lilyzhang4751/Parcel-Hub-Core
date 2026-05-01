package com.lily.parcelhubcore.parcel.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.MessageOutbox;
import com.lily.parcelhubcore.shared.util.TimeConvertUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@DataJpaTest
// 负责启动容器
@Testcontainers
@ActiveProfiles("test")
// 不要把真实 DataSource 替换成 H2
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MessageOutboxRepositoryTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private MessageOutboxRepository messageOutboxRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanUp() {
        jdbcTemplate.execute("truncate table message_outbox restart identity cascade");
    }

    @Test
    void findReadyForPublish_shouldReturnMessages_whenMatchingCriteria() {
        // given
        Instant now = Instant.now();
        MessageOutbox msg1 = new MessageOutbox();
        msg1.setEventId("event1");
        msg1.setTopic("topic1");
        msg1.setEventKey("key1");
        msg1.setPayload("payload1");
        msg1.setStatus(MessageOutbox.Status.NEW);
        msg1.setRetryCount(0);
        msg1.setNextRetryAt(now.minusSeconds(60));  // before now
        messageOutboxRepository.saveAndFlush(msg1);

        MessageOutbox msg2 = new MessageOutbox();
        msg2.setEventId("event2");
        msg2.setTopic("topic2");
        msg2.setEventKey("key2");
        msg2.setPayload("payload2");
        msg2.setStatus(MessageOutbox.Status.FAILED);
        msg2.setRetryCount(1);
        msg2.setNextRetryAt(null);  // null
        messageOutboxRepository.saveAndFlush(msg2);

        // Message that should not be returned: wrong status
        MessageOutbox msg3 = new MessageOutbox();
        msg3.setEventId("event3");
        msg3.setTopic("topic3");
        msg3.setEventKey("key3");
        msg3.setPayload("payload3");
        msg3.setStatus(MessageOutbox.Status.SENT);
        msg3.setRetryCount(0);
        msg3.setNextRetryAt(now.minusSeconds(60));
        messageOutboxRepository.saveAndFlush(msg3);

        // Message that should not be returned: retry_count too high
        MessageOutbox msg4 = new MessageOutbox();
        msg4.setEventId("event4");
        msg4.setTopic("topic4");
        msg4.setEventKey("key4");
        msg4.setPayload("payload4");
        msg4.setStatus(MessageOutbox.Status.NEW);
        msg4.setRetryCount(5);  // >= maxRetry=3
        msg4.setNextRetryAt(now.minusSeconds(60));
        messageOutboxRepository.saveAndFlush(msg4);

        // when
        List<MessageOutbox> result = messageOutboxRepository.findReadyForPublish(
                List.of("NEW", "FAILED"), now, 3, 10);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(MessageOutbox::getEventId).contains("event1", "event2");
        assertThat(result).isSortedAccordingTo((a, b) -> a.getId().compareTo(b.getId()));
    }

    @Test
    void findReadyForPublish_shouldReturnEmptyList_whenNoMatchingMessages() {
        // given - no messages

        // when
        List<MessageOutbox> result = messageOutboxRepository.findReadyForPublish(
                List.of("NEW"), Instant.now(), 3, 10);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void recoverTimedOutProcessing_shouldUpdateMessages_whenTimedOut() {
        // given
        Instant deadline = TimeConvertUtils.toInstant(System.currentTimeMillis());
        Instant now = deadline.plusSeconds(60);
        MessageOutbox msg1 = new MessageOutbox();
        msg1.setEventId("event1");
        msg1.setTopic("topic1");
        msg1.setEventKey("key1");
        msg1.setPayload("payload1");
        msg1.setStatus(MessageOutbox.Status.PROCESSING);
        msg1.setProcessingAt(deadline.minusSeconds(120));  // before deadline
        msg1.setRetryCount(0);
        messageOutboxRepository.saveAndFlush(msg1);

        MessageOutbox msg2 = new MessageOutbox();
        msg2.setEventId("event2");
        msg2.setTopic("topic2");
        msg2.setEventKey("key2");
        msg2.setPayload("payload2");
        msg2.setStatus(MessageOutbox.Status.PROCESSING);
        msg2.setProcessingAt(deadline.minusSeconds(60));  // before deadline
        msg2.setRetryCount(1);
        messageOutboxRepository.saveAndFlush(msg2);

        // Message that should not be updated: not processing
        MessageOutbox msg3 = new MessageOutbox();
        msg3.setEventId("event3");
        msg3.setTopic("topic3");
        msg3.setEventKey("key3");
        msg3.setPayload("payload3");
        msg3.setStatus(MessageOutbox.Status.NEW);
        msg3.setProcessingAt(deadline.minusSeconds(120));
        msg3.setRetryCount(0);
        messageOutboxRepository.saveAndFlush(msg3);

        // Message that should not be updated: processing_at after deadline
        MessageOutbox msg4 = new MessageOutbox();
        msg4.setEventId("event4");
        msg4.setTopic("topic4");
        msg4.setEventKey("key4");
        msg4.setPayload("payload4");
        msg4.setStatus(MessageOutbox.Status.PROCESSING);
        msg4.setProcessingAt(deadline.plusSeconds(60));  // after deadline
        msg4.setRetryCount(0);
        messageOutboxRepository.saveAndFlush(msg4);

        // when
        int updated = messageOutboxRepository.recoverTimedOutProcessing(
                "PROCESSING", "FAILED", deadline, now, "Timeout error", 10);

        // then
        assertThat(updated).isEqualTo(2);

        // Verify updated messages
        MessageOutbox updatedMsg1 = messageOutboxRepository.findById(msg1.getId()).orElseThrow();
        assertThat(updatedMsg1.getStatus()).isEqualTo(MessageOutbox.Status.FAILED);
        assertThat(updatedMsg1.getLastError()).isEqualTo("Timeout error");
        assertThat(updatedMsg1.getNextRetryAt()).isEqualTo(now);

        MessageOutbox updatedMsg2 = messageOutboxRepository.findById(msg2.getId()).orElseThrow();
        assertThat(updatedMsg2.getStatus()).isEqualTo(MessageOutbox.Status.FAILED);
        assertThat(updatedMsg2.getLastError()).isEqualTo("Timeout error");
        assertThat(updatedMsg2.getNextRetryAt()).isEqualTo(now);

        // Verify not updated
        MessageOutbox notUpdatedMsg3 = messageOutboxRepository.findById(msg3.getId()).orElseThrow();
        assertThat(notUpdatedMsg3.getStatus()).isEqualTo(MessageOutbox.Status.NEW);

        MessageOutbox notUpdatedMsg4 = messageOutboxRepository.findById(msg4.getId()).orElseThrow();
        assertThat(notUpdatedMsg4.getStatus()).isEqualTo(MessageOutbox.Status.PROCESSING);
    }

    @Test
    void recoverTimedOutProcessing_shouldReturnZero_whenNoMessagesToRecover() {
        // given - no messages

        // when
        int updated = messageOutboxRepository.recoverTimedOutProcessing(
                "PROCESSING", "FAILED", Instant.now(), Instant.now(), "Timeout error", 10);

        // then
        assertThat(updated).isEqualTo(0);
    }

    @Test
    void save_shouldThrowException_whenEventIdAlreadyExists() {
        // given
        MessageOutbox msg1 = new MessageOutbox();
        msg1.setEventId("duplicateEvent");
        msg1.setTopic("topic1");
        msg1.setEventKey("key1");
        msg1.setPayload("payload1");
        msg1.setStatus(MessageOutbox.Status.NEW);
        msg1.setRetryCount(0);
        messageOutboxRepository.saveAndFlush(msg1);

        MessageOutbox msg2 = new MessageOutbox();
        msg2.setEventId("duplicateEvent");  // same event_id
        msg2.setTopic("topic2");
        msg2.setEventKey("key2");
        msg2.setPayload("payload2");
        msg2.setStatus(MessageOutbox.Status.NEW);
        msg2.setRetryCount(0);

        // when & then
        assertThatThrownBy(() -> messageOutboxRepository.saveAndFlush(msg2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    /**
     * 测试前：清空旧数据
     */
    @Sql(
            statements = "TRUNCATE TABLE message_outbox RESTART IDENTITY CASCADE",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Test
    // 非事务执行，挂起当前事务
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void findReadyForPublish_shouldSkipLockedRows_whenConcurrentTransactions() throws Exception {
        var txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        Instant now = Instant.now();

        // given: 先提交两条 NEW 消息
        txTemplate.executeWithoutResult(status -> {
            MessageOutbox msg1 = new MessageOutbox();
            msg1.setEventId("event1");
            msg1.setTopic("topic1");
            msg1.setEventKey("key1");
            msg1.setPayload("payload1");
            msg1.setStatus(MessageOutbox.Status.NEW);
            msg1.setRetryCount(0);
            msg1.setNextRetryAt(null);
            messageOutboxRepository.save(msg1);

            MessageOutbox msg2 = new MessageOutbox();
            msg2.setEventId("event2");
            msg2.setTopic("topic2");
            msg2.setEventKey("key2");
            msg2.setPayload("payload2");
            msg2.setStatus(MessageOutbox.Status.NEW);
            msg2.setRetryCount(0);
            msg2.setNextRetryAt(null);
            messageOutboxRepository.save(msg2);

            messageOutboxRepository.flush();
        });

        CountDownLatch firstTxLocked = new CountDownLatch(1);
        CountDownLatch releaseFirstTx = new CountDownLatch(1);

        var executor = Executors.newSingleThreadExecutor();

        var firstLockedIdFuture = executor.submit(() ->
                txTemplate.execute(status -> {
                    List<MessageOutbox> firstBatch = messageOutboxRepository.findReadyForPublish(
                            List.of("NEW"), now, 10, 1
                    );

                    assertThat(firstBatch).hasSize(1);

                    Long lockedId = firstBatch.get(0).getId();

                    // 通知主线程：第一事务已经锁住一行
                    firstTxLocked.countDown();

                    try {
                        // 保持第一事务不提交，从而保持行锁
                        assertThat(releaseFirstTx.await(5, TimeUnit.SECONDS)).isTrue();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }

                    return lockedId;
                })
        );

        try {
            // 等第一事务锁住第一条记录
            assertThat(firstTxLocked.await(5, TimeUnit.SECONDS)).isTrue();

            // when: 第二个独立事务查询
            Long secondLockedId = txTemplate.execute(status -> {
                List<MessageOutbox> secondBatch = messageOutboxRepository.findReadyForPublish(
                        List.of("NEW"), now, 10, 1
                );

                assertThat(secondBatch).hasSize(1);
                return secondBatch.get(0).getId();
            });

            // 释放第一事务
            releaseFirstTx.countDown();

            Long firstLockedId = firstLockedIdFuture.get(5, TimeUnit.SECONDS);

            // then: 第二事务拿到的不是第一事务锁住的那一行
            assertThat(secondLockedId).isNotEqualTo(firstLockedId);

        } finally {
            releaseFirstTx.countDown();
            executor.shutdownNow();
        }
    }
}