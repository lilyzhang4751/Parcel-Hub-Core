package com.lily.parcelhubcore.parcel.infrastructure.persistence.repository;

import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.ParcelOpRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParcelOpRecordRepository extends JpaRepository<ParcelOpRecord, Long>, JpaSpecificationExecutor<ParcelOpRecord> {

    List<ParcelOpRecord> findByStationCodeAndWaybillCodeOrderByOpTimeDescIdDesc(
            String stationCode,
            String waybillCode
    );
}
