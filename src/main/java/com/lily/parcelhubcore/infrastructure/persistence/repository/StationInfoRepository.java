package com.lily.parcelhubcore.infrastructure.persistence.repository;

import com.lily.parcelhubcore.infrastructure.persistence.entity.StationInfoDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface StationInfoRepository extends JpaRepository<StationInfoDO, Long>, JpaSpecificationExecutor<StationInfoDO> {

    boolean existsByCode(String code);

    // 可根据需要添加更多查询方法
}
