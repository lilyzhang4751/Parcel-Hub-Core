package com.lily.parcelhubcore.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "parcel", comment = "包裹表")
public class ParcelDO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "station_code")
    private String stationCode;

    @Column(name = "waybill_code")
    private String waybillCode;

    @Column(name = "pickup_code")
    private String pickupCode;

    @Column(name = "shelf_code")
    private String shelfCode;

    @Column(name = "recipient_mobile")
    private String recipientMobile;

    private Integer status;

    @Column(name = "notify_status")
    private Integer notifyStatus;

    @Column(name = "latest_inbound_time")
    private Instant latestInboundTime;

    @Column(name = "latest_outbound_time")
    private Instant latestOutboundTime;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
