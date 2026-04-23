package com.lily.parcelhubcore.parcel.domain.dto;

import java.time.Instant;

public interface DomainEvent {

    String eventId();

    Instant createdAt();

    String type();
}
