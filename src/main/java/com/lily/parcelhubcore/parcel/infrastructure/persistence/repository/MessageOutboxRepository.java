package com.lily.parcelhubcore.parcel.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.MessageOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageOutboxRepository extends JpaRepository<MessageOutbox, Long> {

    // @Lock(PESSIMISTIC_WRITE)：当查出这些行时，立刻对这些行加悲观写锁，直到事务结束
    @Query(value = """
            select *
            from message_outbox o
            where o.status in (:statuses)
              and (o.next_retry_at <= :now or o.next_retry_at is null)
              and o.retry_count < :maxRetry
            order by o.id asc
            for update skip locked
            limit :limit
            """, nativeQuery = true)
    List<MessageOutbox> findReadyForPublish(
            @Param("statuses") List<String> statuses,
            @Param("now") Instant now,
            @Param("maxRetry") int maxRetry,
            @Param("limit") int limit
    );
}
