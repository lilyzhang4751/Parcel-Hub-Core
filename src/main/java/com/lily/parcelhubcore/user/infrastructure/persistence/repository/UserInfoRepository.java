package com.lily.parcelhubcore.user.infrastructure.persistence.repository;

import java.util.List;

import com.lily.parcelhubcore.user.infrastructure.persistence.entity.UserInfoDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfoDO, Long>, JpaSpecificationExecutor<UserInfoDO> {

    UserInfoDO findByCode(String code);

    UserInfoDO findByUserName(String userName);

    List<UserInfoDO> findByStationCode(String stationCode);

    List<UserInfoDO> findByStatus(Short status);

    boolean existsByCode(String code);

    boolean existsByMobile(String mobile);
}