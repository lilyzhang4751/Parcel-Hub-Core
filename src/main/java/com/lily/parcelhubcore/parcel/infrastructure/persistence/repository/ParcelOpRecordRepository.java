package com.lily.parcelhubcore.parcel.infrastructure.persistence.repository;

import java.util.List;

import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.ParcelOpRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ParcelOpRecordRepository extends JpaRepository<ParcelOpRecord, Long>, JpaSpecificationExecutor<ParcelOpRecord> {

    List<ParcelOpRecord> findByStationCodeAndWaybillCode(String stationCode, String waybillCode);
}
