package com.lily.parcelhubcore.station.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "station_info")
public class StationInfoDO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "status")
    private Integer status;

    @Column(name = "principal")
    private String principal;

    @Column(name = "contact_mobile")
    private String contactMobile;

    @Column(name = "deposit")
    private BigDecimal deposit;

    @Column(name = "address")
    private String address;

    @Column(name = "active_time")
    private Instant activeTime;

    @Column(name = "exit_time")
    private Instant exitTime;

    @Column(name = "longitude")
    private BigDecimal longitude;

    @Column(name = "latitude")
    private BigDecimal latitude;

    @Column(name = "business_start_time")
    private LocalTime businessStartTime;

    @Column(name = "business_end_time")
    private LocalTime businessEndTime;

    @Column(name = "remarks")
    private String remarks;

    @CreatedDate
    @Column(name = "created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
}
