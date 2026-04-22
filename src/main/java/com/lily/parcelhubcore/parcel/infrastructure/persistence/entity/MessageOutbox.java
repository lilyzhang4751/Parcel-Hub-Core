package com.lily.parcelhubcore.parcel.infrastructure.persistence.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "message_outbox")
@DynamicInsert
@DynamicUpdate
public class MessageOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id")
    private String eventId;

    @Column(name = "topic")
    private String topic;

    @Column(name = "event_key")
    private String eventKey;

    @Lob
    @Column(name = "payload")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Column(name = "processing_at")
    private Instant processingAt;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    public void markProcessing() {
        this.status = Status.PROCESSING;
        this.retryCount++;
    }

    public void markSent(Instant publishedAt) {
        this.status = Status.SENT;
        this.publishedAt = publishedAt;
        this.lastError = null;
    }

    public void markFailed(String error, Instant nextRetryAt) {
        this.status = Status.FAILED;
        this.lastError = error;
        this.nextRetryAt = nextRetryAt;
    }

    public enum Status {
        NEW, PROCESSING, SENT, FAILED
    }
}
