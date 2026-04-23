package com.lily.parcelhubcore.user.infrastructure.persistence.repository;

import com.lily.parcelhubcore.user.infrastructure.persistence.entity.UserInfoDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfoDO, Long>, JpaSpecificationExecutor<UserInfoDO> {

    UserInfoDO findByCode(String code);

    UserInfoDO findByUsername(String username);
}