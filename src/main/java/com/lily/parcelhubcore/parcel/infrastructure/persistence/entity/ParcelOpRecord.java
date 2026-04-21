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
@Table(name = "parcel_op_record")
@DynamicInsert
public class ParcelOpRecord {

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
