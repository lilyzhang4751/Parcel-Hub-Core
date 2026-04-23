package com.lily.parcelhubcore.user.infrastructure.persistence.repository;

import com.lily.parcelhubcore.user.infrastructure.persistence.entity.StationInfoDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface StationInfoRepository extends JpaRepository<StationInfoDO, Long>, JpaSpecificationExecutor<StationInfoDO> {

    StationInfoDO findByCode(String code);

    boolean existsByCodeAndStatus(String code, Integer status);
}
