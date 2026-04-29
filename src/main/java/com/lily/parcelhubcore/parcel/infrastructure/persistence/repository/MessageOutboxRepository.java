package com.lily.parcelhubcore.parcel.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.List;

import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.MessageOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageOutboxRepository extends JpaRepository<MessageOutbox, Long> {

    @Query(value = """
            select *
            from message_outbox o
            where o.status in (:statuses)
              and (o.next_retry_at <= :now or o.next_retry_at is null)
              and o.retry_count < :maxRetry
            order by o.id asc
            limit :limit
            for update skip locked
            """, nativeQuery = true)
    List<MessageOutbox> findReadyForPublish(
            @Param("statuses") List<String> statuses,
            @Param("now") Instant now,
            @Param("maxRetry") int maxRetry,
            @Param("limit") int limit
    );

    /*
    flushAutomatically = true	执行 UPDATE 前，先把当前未 flush 的变更刷到数据库
    clearAutomatically = true	执行 UPDATE 后，清空一级缓存，避免后续查到旧实体
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE message_outbox
            SET status = :failedStatus,
                last_error = :lastError,
                next_retry_at = :now
            WHERE id IN (
                SELECT id
                FROM message_outbox
                WHERE status = :processingStatus
                  AND processing_at IS NOT NULL
                  AND processing_at < :deadline
                ORDER BY processing_at
                LIMIT :limit
                FOR UPDATE SKIP LOCKED
            )
            """, nativeQuery = true)
    int recoverTimedOutProcessing(@Param("processingStatus") String processingStatus,
                                  @Param("failedStatus") String failedStatus,
                                  @Param("deadline") Instant deadline,
                                  @Param("now") Instant now,
                                  @Param("lastError") String lastError,
                                  @Param("limit") int limit);

}
