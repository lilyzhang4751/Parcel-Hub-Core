package com.lily.parcelhubcore.parcel.infrastructure.kafka.producer;

import com.lily.parcelhubcore.parcel.domain.dto.DomainEvent;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.MessageOutbox;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.repository.MessageOutboxRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class OutboxDomainEventPublisher {

    @Resource
    private MessageOutboxRepository outboxRepository;

    @Resource
    private ObjectMapper objectMapper;

    public void publish(String topic, String key, DomainEvent event) {
        var payload = objectMapper.writeValueAsString(event);
        var outbox = new MessageOutbox();
        outbox.setTopic(topic);
        outbox.setEventKey(key);
        outbox.setEventId(event.eventId());
        outbox.setCreatedAt(event.createdAt());
        outbox.setStatus(MessageOutbox.Status.NEW);
        outbox.setPayload(payload);
        outboxRepository.save(outbox);
    }
}
