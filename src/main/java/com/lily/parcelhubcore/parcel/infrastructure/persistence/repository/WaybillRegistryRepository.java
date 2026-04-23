package com.lily.parcelhubcore.parcel.infrastructure.persistence.repository;

import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.WaybillRegistry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface WaybillRegistryRepository extends JpaRepository<WaybillRegistry, String>, JpaSpecificationExecutor<WaybillRegistry> {

    boolean existsByWaybillCodeAndStatus(String waybillCode, int status);

    WaybillRegistry findByWaybillCodeAndStatus(String stationCode, int status);
}
