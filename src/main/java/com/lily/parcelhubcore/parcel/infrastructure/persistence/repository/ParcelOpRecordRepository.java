package com.lily.parcelhubcore.parcel.infrastructure.persistence.repository;

import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.ParcelOpRecordDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParcelOpRecordRepository extends JpaRepository<ParcelOpRecordDO, Long>, JpaSpecificationExecutor<ParcelOpRecordDO> {

    List<ParcelOpRecordDO> findByStationCodeAndWaybillCodeOrderByOpTimeDescIdDesc(
            String stationCode,
            String waybillCode
    );
}
