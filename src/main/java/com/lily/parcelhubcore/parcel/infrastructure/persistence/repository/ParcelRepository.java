package com.lily.parcelhubcore.parcel.infrastructure.persistence.repository;

import java.util.List;

import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.ParcelDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ParcelRepository extends JpaRepository<ParcelDO, String>, JpaSpecificationExecutor<ParcelDO> {

    List<ParcelDO> findByStationCodeAndShelfCode(String stationCode, String shelfCode);

    boolean existsByStationCodeAndPickupCode(String stationCode, String pickupCode);

    List<ParcelDO> findByStationCodeAndPickupCode(String stationCode, String pickupCode);

    // todo 确实是find哪一个
    ParcelDO findFirstByStationCodeAndWaybillCode(String stationCode, String waybillCode);
}
