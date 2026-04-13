package com.lily.parcelhubcore.user.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import com.lily.parcelhubcore.user.infrastructure.persistence.entity.UserDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserDO, Long>, JpaSpecificationExecutor<UserDO> {

    Optional<UserDO> findByCode(String code);

    Optional<UserDO> findByMobile(String mobile);

    List<UserDO> findByStationCode(String stationCode);

    List<UserDO> findByStatus(Short status);

    boolean existsByCode(String code);

    boolean existsByMobile(String mobile);
}