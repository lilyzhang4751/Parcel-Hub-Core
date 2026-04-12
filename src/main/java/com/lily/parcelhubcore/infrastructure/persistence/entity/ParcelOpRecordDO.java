package com.lily.parcelhubcore.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@Entity
@Table(name = "parcel_op_record")
public class ParcelOpRecordDO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "station_code")
    private String stationCode;

    @Column(name = "waybill_code")
    private String waybillCode;

    @Column(name = "op_time")
    private Instant opTime;

    @Column(name = "op_type")
    private Integer opType;

    @Column(name = "operater_id")
    private Long operaterId;

    @Column(name = "operator_name")
    private String operatorName;

    @Column(name = "device_type")
    private Integer deviceType;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
