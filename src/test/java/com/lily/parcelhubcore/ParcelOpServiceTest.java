package com.lily.parcelhubcore;

import com.lily.parcelhubcore.parcel.application.command.PrepareInCommand;
import com.lily.parcelhubcore.parcel.application.service.ParcelOpService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ParcelOpServiceTest {

    @Resource
    private ParcelOpService parcelOpService;

    @Test
    public void prepareInTest(){
        var bo = new PrepareInCommand();
        bo.setWaybillCode("JD123456");
        parcelOpService.prepareIn(bo);

        bo.setWaybillCode("JD00123456");
        parcelOpService.prepareIn(bo);
    }
}
