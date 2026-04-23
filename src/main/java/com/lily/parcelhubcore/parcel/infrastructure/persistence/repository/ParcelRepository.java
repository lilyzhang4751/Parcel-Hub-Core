package com.lily.parcelhubcore.parcel.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.Parcel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ParcelRepository extends JpaRepository<Parcel, String>, JpaSpecificationExecutor<Parcel> {

    List<Parcel> findByStationCodeAndShelfCode(String stationCode, String shelfCode);

    boolean existsByStationCodeAndPickupCode(String stationCode, String pickupCode);

    Optional<Parcel> findByStationCodeAndWaybillCode(String stationCode, String waybillCode);
}
