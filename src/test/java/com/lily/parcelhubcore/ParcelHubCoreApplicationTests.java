package com.lily.parcelhubcore;

import com.lily.parcelhubcore.parcel.infrastructure.persistence.repository.WaybillRegistryRepository;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ParcelHubCoreApplicationTests {

    @Resource
    private WaybillRegistryRepository waybillRegistryRepository;

    @Test
    void contextLoads() {
        var waybill = waybillRegistryRepository.findById("1");
        System.out.println(waybill);
    }

}
