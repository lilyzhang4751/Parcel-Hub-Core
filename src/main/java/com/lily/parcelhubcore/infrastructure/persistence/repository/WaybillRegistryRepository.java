package com.lily.parcelhubcore.infrastructure.persistence.repository;

import com.lily.parcelhubcore.infrastructure.persistence.entity.WaybillRegistryDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WaybillRegistryRepository extends JpaRepository<WaybillRegistryDO, String>, JpaSpecificationExecutor<WaybillRegistryDO> {

    boolean existsByWaybillCode(String waybillCode);

    List<WaybillRegistryDO> findByStationCode(String stationCode);

    List<WaybillRegistryDO> findByStationCodeAndStatus(String stationCode, Integer status);
}
