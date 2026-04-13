package com.lily.parcelhubcore.parcel.infrastructure.persistence.repository;

import java.util.List;

import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.WaybillRegistryDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface WaybillRegistryRepository extends JpaRepository<WaybillRegistryDO, String>, JpaSpecificationExecutor<WaybillRegistryDO> {

    //@Query(value = "select count(*) > 0 from waybill_registry where waybill_code = ? and status = 0", nativeQuery = true)
    boolean existsByWaybillCodeAndStatus(String waybillCode, int status);

    List<WaybillRegistryDO> findByStationCode(String stationCode);

    List<WaybillRegistryDO> findByStationCodeAndStatus(String stationCode, Integer status);
}
