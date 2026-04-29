package com.lily.parcelhubcore.parcel.application.service.impl;

import static com.lily.parcelhubcore.parcel.common.enums.ErrorCode.PARCEL_NOT_EXIST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import com.lily.parcelhubcore.parcel.application.command.ParcelInBoundCommand;
import com.lily.parcelhubcore.parcel.application.command.PrepareInCommand;
import com.lily.parcelhubcore.parcel.application.dto.PrepareInDTO;
import com.lily.parcelhubcore.parcel.common.constants.KeyConstants;
import com.lily.parcelhubcore.parcel.common.enums.ErrorCode;
import com.lily.parcelhubcore.parcel.common.util.CommonUtil;
import com.lily.parcelhubcore.parcel.domain.dto.ParcelPackDTO;
import com.lily.parcelhubcore.parcel.domain.service.PackageBuilder;
import com.lily.parcelhubcore.parcel.domain.service.ParcelDomainService;
import com.lily.parcelhubcore.parcel.domain.service.PickupCodeService;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.Parcel;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.WaybillRegistry;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.repository.WaybillRegistryRepository;
import com.lily.parcelhubcore.shared.cache.CacheService;
import com.lily.parcelhubcore.shared.enums.OperateTypeEnum;
import com.lily.parcelhubcore.shared.enums.WaybillRegistryStatusEnum;
import com.lily.parcelhubcore.shared.exception.BusinessException;
import com.lily.parcelhubcore.shared.lock.Lock;
import com.lily.parcelhubcore.shared.util.CurrentUserUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ParcelOpServiceTest {

    @InjectMocks
    private ParcelOpServiceImpl parcelOpService;

    @Mock
    private Lock lock;

    @Mock
    private ParcelDomainService parcelDomainService;

    @Mock
    private CacheService cacheService;

    @Mock
    private PickupCodeService pickupCodeService;

    @Mock
    private PackageBuilder packageBuilder;

    @Mock
    private WaybillRegistryRepository waybillRegistryRepository;

    @Mock
    private ParcelPackDTO packDTO;

    @Mock
    private WaybillRegistry waybillRegistry;

    @Mock
    private Parcel parcel;

    @Test
    void prepareIn_shouldReturnDTOWithCachedPickupCode_whenPickupCodeCachedAndMatchesShelf() {
        // given
        PrepareInCommand bo = new PrepareInCommand();
        bo.setWaybillCode("WB123");
        bo.setShelfCode("SHELF1");
        String stationCode = "STATION1";
        String cacheKey = KeyConstants.getWaybillPickupCacheKey(stationCode, bo.getWaybillCode());
        String cachedPickupCode = "PICK123";

        try (MockedStatic<CurrentUserUtil> mockedCurrentUserUtil = mockStatic(CurrentUserUtil.class);
             MockedStatic<CommonUtil> mockedCommonUtil = mockStatic(CommonUtil.class)) {

            // fetch stationCode
            mockedCurrentUserUtil.when(CurrentUserUtil::getStationCode).thenReturn(stationCode);
            // lock succuess
            when(lock.tryLock(anyString(), anyLong(), eq(TimeUnit.MILLISECONDS))).thenReturn(true);
            // return pickupCode from cache
            when(cacheService.get(cacheKey)).thenReturn(cachedPickupCode);
            // pickupCode matches shelfCode
            mockedCommonUtil.when(() -> CommonUtil.pickupCodeMachShelfCode(cachedPickupCode, bo.getShelfCode())).thenReturn(true);

            // when
            PrepareInDTO result = parcelOpService.prepareIn(bo);

            // then
            assertNotNull(result);
            assertEquals(cachedPickupCode, result.getPickupCode());
            verify(lock).tryLock(anyString(), anyLong(), eq(TimeUnit.MILLISECONDS));
            verify(lock).unlock(anyString());
            verify(parcelDomainService).waybillInBoundVerify(bo.getWaybillCode());
            verify(cacheService).get(cacheKey);
            verifyNoMoreInteractions(pickupCodeService, cacheService);
        }
    }

    @Test
    void prepareIn_shouldReturnDTOWithNewPickupCode_whenNoCacheOrNotMatching() {
        // given
        PrepareInCommand bo = new PrepareInCommand();
        bo.setWaybillCode("WB123");
        bo.setShelfCode("SHELF1");
        String stationCode = "STATION1";
        String cacheKey = KeyConstants.getWaybillPickupCacheKey(stationCode, bo.getWaybillCode());
        String newPickupCode = "PICK456";

        try (MockedStatic<CurrentUserUtil> mockedCurrentUserUtil = mockStatic(CurrentUserUtil.class)) {
            mockedCurrentUserUtil.when(CurrentUserUtil::getStationCode).thenReturn(stationCode);
            when(lock.tryLock(anyString(), anyLong(), eq(TimeUnit.MILLISECONDS))).thenReturn(true);
            when(cacheService.get(cacheKey)).thenReturn(null);
            when(pickupCodeService.genarate(stationCode, bo.getShelfCode())).thenReturn(newPickupCode);

            // when
            PrepareInDTO result = parcelOpService.prepareIn(bo);

            // then
            assertNotNull(result);
            assertEquals(newPickupCode, result.getPickupCode());
            verify(lock).tryLock(anyString(), anyLong(), eq(TimeUnit.MILLISECONDS));
            verify(lock).unlock(anyString());
            verify(parcelDomainService).waybillInBoundVerify(bo.getWaybillCode());
            verify(cacheService).get(cacheKey);
            verify(pickupCodeService).genarate(stationCode, bo.getShelfCode());
            verify(cacheService).set(cacheKey, newPickupCode, 2);
        }
    }

    @Test
    void prepareIn_shouldThrowBusinessException_whenLockFails() {
        // given
        PrepareInCommand bo = new PrepareInCommand();
        bo.setWaybillCode("WB123");
        bo.setShelfCode("SHELF1");
        String stationCode = "STATION1";
        try (MockedStatic<CurrentUserUtil> mockedCurrentUserUtil = mockStatic(CurrentUserUtil.class)) {
            mockedCurrentUserUtil.when(CurrentUserUtil::getStationCode).thenReturn(stationCode);
            when(lock.tryLock(anyString(), anyLong(), eq(TimeUnit.MILLISECONDS))).thenReturn(false);
            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> parcelOpService.prepareIn(bo));
            assertEquals(ErrorCode.CURRENT_EXCEPTION, exception.getCommonErrorCode());
            verify(lock).tryLock(anyString(), anyLong(), eq(TimeUnit.MILLISECONDS));
            verify(lock).unlock(anyString());
            verifyNoInteractions(parcelDomainService, cacheService, pickupCodeService);
        }
    }

    @Test
    void prepareIn_shouldThrowBusinessException_whenWaybillInBoundVerifyThrows() {
        // given
        PrepareInCommand bo = new PrepareInCommand();
        bo.setWaybillCode("WB123");
        bo.setShelfCode("SHELF1");
        String stationCode = "STATION1";

        try (MockedStatic<CurrentUserUtil> mockedCurrentUserUtil = mockStatic(CurrentUserUtil.class)) {
            mockedCurrentUserUtil.when(CurrentUserUtil::getStationCode).thenReturn(stationCode);
            when(lock.tryLock(anyString(), anyLong(), eq(TimeUnit.MILLISECONDS))).thenReturn(true);
            doThrow(new BusinessException(ErrorCode.PARCEL_ALREADY_EXIST)).when(parcelDomainService).waybillInBoundVerify(bo.getWaybillCode());

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> parcelOpService.prepareIn(bo));
            assertEquals(ErrorCode.PARCEL_ALREADY_EXIST, exception.getCommonErrorCode());
            verify(lock).tryLock(anyString(), anyLong(), eq(TimeUnit.MILLISECONDS));
            verify(lock).unlock(anyString());
            verify(parcelDomainService).waybillInBoundVerify(bo.getWaybillCode());
            verifyNoMoreInteractions(cacheService, pickupCodeService);
        }
    }

    @Test
    void inbound_shouldCompleteSuccessfully_whenAllConditionsMet() {
        // given
        ParcelInBoundCommand command = new ParcelInBoundCommand();
        command.setWaybillCode("WB123");
        command.setPickupCode("PICK123");
        String stationCode = "STATION1";

        try (MockedStatic<CurrentUserUtil> mockedCurrentUserUtil = mockStatic(CurrentUserUtil.class)) {
            mockedCurrentUserUtil.when(CurrentUserUtil::getStationCode).thenReturn(stationCode);
            when(lock.tryLock(anyString(), anyLong(), eq(TimeUnit.MILLISECONDS))).thenReturn(true);
            when(packageBuilder.buildInParcelPackDTO(command)).thenReturn(packDTO);

            // when
            parcelOpService.inbound(command);

            // then
            verify(lock).tryLock(anyString(), anyLong(), eq(TimeUnit.MILLISECONDS));
            verify(lock).unlock(anyString());
            verify(parcelDomainService).waybillInBoundVerify(command.getWaybillCode());
            verify(pickupCodeService).pickupCodeExistVerify(stationCode, command.getPickupCode());
            verify(packageBuilder).buildInParcelPackDTO(command);
            verify(parcelDomainService).updateDBAndSendMsg(packDTO);
            verify(cacheService).delete(KeyConstants.getWaybillPickupCacheKey(stationCode, command.getWaybillCode()));
        }
    }

    @Test
    void inbound_shouldThrowBusinessException_whenLockFails() {
        // given
        ParcelInBoundCommand command = new ParcelInBoundCommand();
        command.setWaybillCode("WB123");
        String stationCode = "STATION1";

        try (MockedStatic<CurrentUserUtil> mockedCurrentUserUtil = mockStatic(CurrentUserUtil.class)) {
            mockedCurrentUserUtil.when(CurrentUserUtil::getStationCode).thenReturn(stationCode);
            when(lock.tryLock(anyString(), anyLong(), eq(TimeUnit.MILLISECONDS))).thenReturn(false);

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> parcelOpService.inbound(command));
            assertEquals(ErrorCode.CURRENT_EXCEPTION, exception.getCommonErrorCode());
            verify(lock).tryLock(anyString(), anyLong(), eq(TimeUnit.MILLISECONDS));
            verify(lock).unlock(anyString());
            verifyNoInteractions(parcelDomainService, pickupCodeService, packageBuilder, cacheService);
        }
    }

    @Test
    void inbound_shouldThrowBusinessException_whenWaybillInBoundVerifyThrows() {
        // given
        ParcelInBoundCommand command = new ParcelInBoundCommand();
        command.setWaybillCode("WB123");
        String stationCode = "STATION1";

        try (MockedStatic<CurrentUserUtil> mockedCurrentUserUtil = mockStatic(CurrentUserUtil.class)) {
            mockedCurrentUserUtil.when(CurrentUserUtil::getStationCode).thenReturn(stationCode);
            when(lock.tryLock(anyString(), anyLong(), eq(TimeUnit.MILLISECONDS))).thenReturn(true);
            doThrow(new BusinessException(ErrorCode.PARCEL_ALREADY_EXIST)).when(parcelDomainService).waybillInBoundVerify(command.getWaybillCode());

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> parcelOpService.inbound(command));
            assertEquals(ErrorCode.PARCEL_ALREADY_EXIST, exception.getCommonErrorCode());
            verify(lock).tryLock(anyString(), anyLong(), eq(TimeUnit.MILLISECONDS));
            verify(lock).unlock(anyString());
            verify(parcelDomainService).waybillInBoundVerify(command.getWaybillCode());
            verifyNoMoreInteractions(pickupCodeService, packageBuilder, cacheService);
        }
    }

    @Test
    void inbound_shouldThrowBusinessException_whenPickupCodeExistVerifyThrows() {
        // given
        ParcelInBoundCommand command = new ParcelInBoundCommand();
        command.setWaybillCode("WB123");
        command.setPickupCode("PICK123");
        String stationCode = "STATION1";

        try (MockedStatic<CurrentUserUtil> mockedCurrentUserUtil = mockStatic(CurrentUserUtil.class)) {
            mockedCurrentUserUtil.when(CurrentUserUtil::getStationCode).thenReturn(stationCode);
            when(lock.tryLock(anyString(), anyLong(), eq(TimeUnit.MILLISECONDS))).thenReturn(true);
            doThrow(new BusinessException(ErrorCode.PICKUP_OCCUPIED)).when(pickupCodeService).pickupCodeExistVerify(stationCode, command.getPickupCode());

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> parcelOpService.inbound(command));
            assertEquals(ErrorCode.PICKUP_OCCUPIED, exception.getCommonErrorCode());
            verify(lock).tryLock(anyString(), anyLong(), eq(TimeUnit.MILLISECONDS));
            verify(lock).unlock(anyString());
            verify(parcelDomainService).waybillInBoundVerify(command.getWaybillCode());
            verify(pickupCodeService).pickupCodeExistVerify(stationCode, command.getPickupCode());
            verifyNoMoreInteractions(packageBuilder, parcelDomainService, cacheService);
        }
    }

    @Test
    void outBoundOrReturn_shouldCompleteSuccessfully_whenAllConditionsMet() {
        // given
        String waybillCode = "WB123";
        OperateTypeEnum operateType = OperateTypeEnum.OUT;
        String stationCode = "STATION1";

        try (MockedStatic<CurrentUserUtil> mockedCurrentUserUtil = mockStatic(CurrentUserUtil.class)) {
            mockedCurrentUserUtil.when(CurrentUserUtil::getStationCode).thenReturn(stationCode);
            when(lock.tryLock(anyString(), anyLong(), eq(TimeUnit.MILLISECONDS))).thenReturn(true);
            when(waybillRegistryRepository.findByWaybillCodeAndStatus(waybillCode, WaybillRegistryStatusEnum.OCCUPIED.getCode())).thenReturn(this.waybillRegistry);
            when(this.waybillRegistry.getStationCode()).thenReturn(stationCode);
            when(parcelDomainService.getInboundParcelDO(stationCode, waybillCode)).thenReturn(this.parcel);
            when(packageBuilder.buildParcelPackByType(this.waybillRegistry, this.parcel, operateType)).thenReturn(this.packDTO);

            // when
            parcelOpService.outBoundOrReturn(waybillCode, operateType);

            // then
            verify(lock).tryLock(anyString(), anyLong(), eq(TimeUnit.MILLISECONDS));
            verify(lock).unlock(anyString());
            verify(waybillRegistryRepository).findByWaybillCodeAndStatus(waybillCode, WaybillRegistryStatusEnum.OCCUPIED.getCode());
            verify(parcelDomainService).getInboundParcelDO(stationCode, waybillCode);
            verify(packageBuilder).buildParcelPackByType(this.waybillRegistry, this.parcel, operateType);
            verify(parcelDomainService).updateDBAndSendMsg(this.packDTO);
        }
    }

    @Test
    void outBoundOrReturn_shouldThrowBusinessException_whenWaybillRegistryNotFound() {
        // given
        String waybillCode = "WB123";
        OperateTypeEnum operateType = OperateTypeEnum.OUT;
        String stationCode = "STATION1";

        try (MockedStatic<CurrentUserUtil> mockedCurrentUserUtil = mockStatic(CurrentUserUtil.class)) {
            mockedCurrentUserUtil.when(CurrentUserUtil::getStationCode).thenReturn(stationCode);
            when(lock.tryLock(anyString(), anyLong(), eq(TimeUnit.MILLISECONDS))).thenReturn(true);
            when(waybillRegistryRepository.findByWaybillCodeAndStatus(waybillCode, WaybillRegistryStatusEnum.OCCUPIED.getCode())).thenReturn(null);

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> parcelOpService.outBoundOrReturn(waybillCode, operateType));
            assertEquals(PARCEL_NOT_EXIST, exception.getCommonErrorCode());
            verify(lock).tryLock(anyString(), anyLong(), eq(TimeUnit.MILLISECONDS));
            verify(lock).unlock(anyString());
            verify(waybillRegistryRepository).findByWaybillCodeAndStatus(waybillCode, WaybillRegistryStatusEnum.OCCUPIED.getCode());
            verifyNoMoreInteractions(parcelDomainService, packageBuilder);
        }
    }

    @Test
    void outBoundOrReturn_shouldThrowBusinessException_whenStationCodeMismatch() {
        // given
        String waybillCode = "WB123";
        OperateTypeEnum operateType = OperateTypeEnum.OUT;
        String stationCode = "STATION1";

        try (MockedStatic<CurrentUserUtil> mockedCurrentUserUtil = mockStatic(CurrentUserUtil.class)) {
            mockedCurrentUserUtil.when(CurrentUserUtil::getStationCode).thenReturn(stationCode);
            when(lock.tryLock(anyString(), anyLong(), eq(TimeUnit.MILLISECONDS))).thenReturn(true);
            when(waybillRegistryRepository.findByWaybillCodeAndStatus(waybillCode, WaybillRegistryStatusEnum.OCCUPIED.getCode())).thenReturn(this.waybillRegistry);
            when(this.waybillRegistry.getStationCode()).thenReturn("DIFFERENT_STATION");

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> parcelOpService.outBoundOrReturn(waybillCode, operateType));
            assertEquals(PARCEL_NOT_EXIST, exception.getCommonErrorCode());
            verify(lock).tryLock(anyString(), anyLong(), eq(TimeUnit.MILLISECONDS));
            verify(lock).unlock(anyString());
            verify(waybillRegistryRepository).findByWaybillCodeAndStatus(waybillCode, WaybillRegistryStatusEnum.OCCUPIED.getCode());
            verifyNoMoreInteractions(parcelDomainService, packageBuilder);
        }
    }

    @Test
    void transfer_shouldCompleteSuccessfully_whenAllConditionsMet() {
        // given
        String waybillCode = "WB123";
        String shelfCode = "SHELF2";
        String stationCode = "STATION1";
        String newPickupCode = "PICK789";

        try (MockedStatic<CurrentUserUtil> mockedCurrentUserUtil = mockStatic(CurrentUserUtil.class)) {
            mockedCurrentUserUtil.when(CurrentUserUtil::getStationCode).thenReturn(stationCode);
            when(lock.tryLock(anyString(), anyLong(), eq(TimeUnit.MILLISECONDS))).thenReturn(true);
            when(parcelDomainService.getInboundParcelDO(stationCode, waybillCode)).thenReturn(this.parcel);
            when(pickupCodeService.genarate(stationCode, shelfCode)).thenReturn(newPickupCode);
            when(packageBuilder.buildTransferParcelPackDTO(this.parcel, shelfCode, newPickupCode)).thenReturn(this.packDTO);

            // when
            parcelOpService.transfer(waybillCode, shelfCode);

            // then
            verify(lock).tryLock(anyString(), anyLong(), eq(TimeUnit.MILLISECONDS));
            verify(lock).unlock(anyString());
            verify(parcelDomainService).getInboundParcelDO(stationCode, waybillCode);
            verify(pickupCodeService).genarate(stationCode, shelfCode);
            verify(packageBuilder).buildTransferParcelPackDTO(this.parcel, shelfCode, newPickupCode);
            verify(parcelDomainService).updateDBAndSendMsg(this.packDTO);
        }
    }

    @Test
    void transfer_shouldThrowBusinessException_whenLockFails() {
        // given
        String waybillCode = "WB123";
        String shelfCode = "SHELF2";
        String stationCode = "STATION1";

        try (MockedStatic<CurrentUserUtil> mockedCurrentUserUtil = mockStatic(CurrentUserUtil.class)) {
            mockedCurrentUserUtil.when(CurrentUserUtil::getStationCode).thenReturn(stationCode);
            when(lock.tryLock(anyString(), anyLong(), eq(TimeUnit.MILLISECONDS))).thenReturn(false);

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> parcelOpService.transfer(waybillCode, shelfCode));
            assertEquals(ErrorCode.CURRENT_EXCEPTION, exception.getCommonErrorCode());
            verify(lock).tryLock(anyString(), anyLong(), eq(TimeUnit.MILLISECONDS));
            verify(lock).unlock(anyString());
            verifyNoInteractions(parcelDomainService, pickupCodeService, packageBuilder);
        }
    }

    @Test
    void transfer_shouldThrowBusinessException_whenGetInboundParcelDOReturnsNull() {
        // given
        String waybillCode = "WB123";
        String shelfCode = "SHELF2";
        String stationCode = "STATION1";

        try (MockedStatic<CurrentUserUtil> mockedCurrentUserUtil = mockStatic(CurrentUserUtil.class)) {
            mockedCurrentUserUtil.when(CurrentUserUtil::getStationCode).thenReturn(stationCode);
            when(lock.tryLock(anyString(), anyLong(), eq(TimeUnit.MILLISECONDS))).thenReturn(true);
            when(parcelDomainService.getInboundParcelDO(stationCode, waybillCode)).thenThrow(new BusinessException(PARCEL_NOT_EXIST));

            // Re-run the test
            BusinessException exception = assertThrows(BusinessException.class, () -> parcelOpService.transfer(waybillCode, shelfCode));
            assertEquals(PARCEL_NOT_EXIST, exception.getCommonErrorCode());
            verify(lock).tryLock(anyString(), anyLong(), eq(TimeUnit.MILLISECONDS));
            verify(lock).unlock(anyString());
            verify(parcelDomainService).getInboundParcelDO(stationCode, waybillCode);
            verifyNoMoreInteractions(pickupCodeService, packageBuilder);
        }
    }

}

