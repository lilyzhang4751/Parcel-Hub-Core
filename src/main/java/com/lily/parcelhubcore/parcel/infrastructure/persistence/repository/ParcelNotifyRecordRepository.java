package com.lily.parcelhubcore.parcel.infrastructure.persistence.repository;

import java.util.List;

import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.ParcelNotifyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ParcelNotifyRecordRepository extends JpaRepository<ParcelNotifyRecord, Long>, JpaSpecificationExecutor<ParcelNotifyRecord> {

    List<ParcelNotifyRecord> findByStationCodeAndWaybillCode(String stationCode, String waybillCode);

}
