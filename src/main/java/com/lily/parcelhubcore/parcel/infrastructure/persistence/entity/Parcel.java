package com.lily.parcelhubcore.parcel.infrastructure.persistence.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "parcel", comment = "包裹表")
@DynamicInsert
@DynamicUpdate
public class Parcel {

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

    @Column(name = "recipient_name")
    private String recipientName;

    @Column(name = "recipient_mobile")
    private String recipientMobile;

    private Integer status;

    @Column(name = "notify_status")
    private Integer notifyStatus;

    @Column(name = "latest_inbound_time")
    private Instant latestInboundTime;

    @Column(name = "latest_outbound_time")
    private Instant latestOutboundTime;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;
}
