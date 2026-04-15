package com.lily.parcelhubcore.parcel.infrastructure.persistence.repository;

import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.WaybillRegistryDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface WaybillRegistryRepository extends JpaRepository<WaybillRegistryDO, String>, JpaSpecificationExecutor<WaybillRegistryDO> {

    boolean existsByWaybillCodeAndStatus(String waybillCode, int status);

    WaybillRegistryDO findByWaybillCodeAndStatus(String stationCode, int status);
}
