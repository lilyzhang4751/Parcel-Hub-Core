package com.lily.parcelhubcore.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.time.ZonedDateTime;

@Data
@Entity
@Table(name = "waybill_registry", comment = "运单注册表")
public class WaybillRegistryDO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "waybill_code")
    private String waybillCode;

    @Column(name = "station_code")
    private String stationCode;

    @Column(name = "status")
    private Integer status;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
