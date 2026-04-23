package com.lily.parcelhubcore;

import com.lily.parcelhubcore.parcel.infrastructure.persistence.repository.WaybillRegistryRepository;
import com.lily.parcelhubcore.user.application.service.LoginService;
import com.lily.parcelhubcore.user.infrastructure.persistence.repository.UserInfoRepository;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ParcelHubCoreApplicationTests {

    @Resource
    private WaybillRegistryRepository waybillRegistryRepository;

    @Resource
    private UserInfoRepository userInfoRepository;

    @Resource
    private LoginService loginService;

    @Test
    void contextLoads() {
        System.out.println("test");
    }

}
