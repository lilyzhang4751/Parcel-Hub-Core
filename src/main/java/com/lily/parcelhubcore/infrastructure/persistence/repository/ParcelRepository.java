package com.lily.parcelhubcore.infrastructure.persistence.repository;

import com.lily.parcelhubcore.infrastructure.persistence.entity.ParcelDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ParcelRepository extends JpaRepository<ParcelDO, String>, JpaSpecificationExecutor<ParcelDO> {
}
