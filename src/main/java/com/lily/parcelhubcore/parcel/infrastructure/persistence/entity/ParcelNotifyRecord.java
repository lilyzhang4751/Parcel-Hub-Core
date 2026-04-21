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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "parcel_notify_record")
@DynamicInsert
public class ParcelNotifyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "station_code")
    private String stationCode;

    @Column(name = "waybill_code")
    private String waybillCode;

    @Column(name = "notify_time")
    private Instant notifyTime;

    @Column(name = "channel")
    private Integer channel;

    @Column(name = "status")
    private Integer status;

    @Column(name = "content")
    private String content;

    @Column(name = "operator_code")
    private String operatorCode;

    @Column(name = "operator_name")
    private String operatorName;

    @Column(name = "unique_id")
    private String uniqueId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;
}
