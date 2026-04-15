package com.lily.parcelhubcore.user.infrastructure.persistence.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_info")
public class UserInfoDO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "station_code")
    private String stationCode;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "identity_card")
    private String identityCard;

    @Column(name = "role")
    private String role;

    @Column(name = "status")
    private Short status;

    @Column(name = "hire_time")
    private Instant hireTime;

    @Column(name = "resign_time")
    private Instant resignTime;

    @Column(name = "password")
    private String password;

    @Column(name = "last_login_time")
    private Instant lastLoginTime;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
