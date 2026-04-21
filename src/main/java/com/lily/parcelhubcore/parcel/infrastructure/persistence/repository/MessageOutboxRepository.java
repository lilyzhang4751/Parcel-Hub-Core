package com.lily.parcelhubcore.parcel.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.MessageOutbox;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageOutboxRepository extends JpaRepository<MessageOutbox, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select o
            from MessageOutbox o
            where o.status in :statuses
              and o.nextRetryAt <= :now
              and o.retryCount < :maxRetry
            order by o.id asc
            """)
    List<MessageOutbox> findReadyForPublish(
            @Param("statuses") Collection<MessageOutbox.Status> statuses,
            @Param("now") Instant now,
            @Param("maxRetry") int maxRetry,
            Pageable pageable
    );
}
