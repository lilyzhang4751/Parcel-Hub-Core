package com.lily.parcelhubcore.parcel.domain.dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import lombok.Data;

@Data
public class ParcelOpSyncEvent implements DomainEvent, Serializable {
    @Serial
    private static final long serialVersionUID = 8941097435523585578L;

    private String stationCode;

    private String waybillCode;

    private Instant opTime;

    private Integer opType;

    private String operatorCode;

    private String operatorName;

    private String eventId;

    private Instant occurredAt;

    @Override
    public String eventId() {
        return eventId;
    }

    @Override
    public Instant createdAt() {
        return opTime;
    }

    @Override
    public String type() {
        return this.getClass().getSimpleName();
    }
}
