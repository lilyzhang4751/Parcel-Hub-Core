package com.lily.parcelhubcore.parcel.domain.dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import lombok.Data;

@Data
public class ParcelNotifyEvent implements DomainEvent, Serializable {

    @Serial
    private static final long serialVersionUID = 8941097435523585578L;

    private String stationCode;

    private String waybillCode;

    private Instant notifyTime;

    private String content;

    private String operatorCode;

    private String operatorName;

    private String pickupCode;

    private List<String> channelList;

    private String eventId;

    @Override
    public String eventId() {
        return eventId;
    }

    @Override
    public Instant createdAt() {
        return notifyTime;
    }

    @Override
    public String type() {
        return this.getClass().getSimpleName();
    }

}
