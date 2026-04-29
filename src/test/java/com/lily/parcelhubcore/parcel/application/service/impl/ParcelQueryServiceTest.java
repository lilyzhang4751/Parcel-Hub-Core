package com.lily.parcelhubcore.parcel.application.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.lily.parcelhubcore.parcel.api.response.ParcelBaseInfoDTO;
import com.lily.parcelhubcore.parcel.api.response.ParcelDetailDTO;
import com.lily.parcelhubcore.parcel.application.query.ParcelPageQuery;
import com.lily.parcelhubcore.parcel.common.enums.ErrorCode;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.Parcel;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.ParcelNotifyRecord;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.ParcelOpRecord;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.repository.ParcelNotifyRecordRepository;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.repository.ParcelOpRecordRepository;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.repository.ParcelRepository;
import com.lily.parcelhubcore.shared.exception.BusinessException;
import com.lily.parcelhubcore.shared.response.PageResponse;
import com.lily.parcelhubcore.shared.util.TimeConvertUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class ParcelQueryServiceTest {

    @InjectMocks
    private ParcelQueryServiceImpl parcelQueryService;

    @Mock
    private ParcelRepository parcelRepository;

    @Mock
    private ParcelOpRecordRepository parcelOpRecordRepository;

    @Mock
    private ParcelNotifyRecordRepository parcelNotifyRecordRepository;

    @Mock
    private Parcel parcel;

    @Mock
    private ParcelOpRecord parcelOpRecord;

    @Mock
    private ParcelNotifyRecord parcelNotifyRecord;

    @Test
    void querySingleParcel_shouldReturnDetail_whenParcelExistsWithRecords() {
        // given
        String stationCode = "STATION1";
        String waybillCode = "WB123";
        List<ParcelOpRecord> opRecords = List.of(parcelOpRecord);
        List<ParcelNotifyRecord> notifyRecords = List.of(parcelNotifyRecord);

        when(parcelRepository.findByStationCodeAndWaybillCode(stationCode, waybillCode)).thenReturn(Optional.of(parcel));
        when(parcelOpRecordRepository.findByStationCodeAndWaybillCode(stationCode, waybillCode)).thenReturn(opRecords);
        when(parcelNotifyRecordRepository.findByStationCodeAndWaybillCode(stationCode, waybillCode)).thenReturn(notifyRecords);

        try (MockedStatic<TimeConvertUtils> mockedTimeConvertUtils = mockStatic(TimeConvertUtils.class)) {
            mockedTimeConvertUtils.when(() -> TimeConvertUtils.toEpochMilli(any())).thenReturn(123456789L);

            // when
            ParcelDetailDTO result = parcelQueryService.querySingleParcel(stationCode, waybillCode);

            // then
            assertNotNull(result);
            assertNotNull(result.getBaseInfo());
            assertNotNull(result.getOpRecordList());
            assertEquals(1, result.getOpRecordList().size());
            assertNotNull(result.getNotifyRecordList());
            assertEquals(1, result.getNotifyRecordList().size());
            verify(parcelRepository).findByStationCodeAndWaybillCode(stationCode, waybillCode);
            verify(parcelOpRecordRepository).findByStationCodeAndWaybillCode(stationCode, waybillCode);
            verify(parcelNotifyRecordRepository).findByStationCodeAndWaybillCode(stationCode, waybillCode);
        }
    }

    @Test
    void querySingleParcel_shouldReturnDetail_whenParcelExistsWithoutRecords() {
        // given
        String stationCode = "STATION1";
        String waybillCode = "WB123";

        when(parcelRepository.findByStationCodeAndWaybillCode(stationCode, waybillCode)).thenReturn(Optional.of(parcel));
        when(parcelOpRecordRepository.findByStationCodeAndWaybillCode(stationCode, waybillCode)).thenReturn(List.of());
        when(parcelNotifyRecordRepository.findByStationCodeAndWaybillCode(stationCode, waybillCode)).thenReturn(List.of());

        try (MockedStatic<TimeConvertUtils> mockedTimeConvertUtils = mockStatic(TimeConvertUtils.class)) {
            mockedTimeConvertUtils.when(() -> TimeConvertUtils.toEpochMilli(any())).thenReturn(123456789L);

            // when
            ParcelDetailDTO result = parcelQueryService.querySingleParcel(stationCode, waybillCode);

            // then
            assertNotNull(result);
            assertNotNull(result.getBaseInfo());
            assertNull(result.getOpRecordList());
            assertNull(result.getNotifyRecordList());
            verify(parcelRepository).findByStationCodeAndWaybillCode(stationCode, waybillCode);
            verify(parcelOpRecordRepository).findByStationCodeAndWaybillCode(stationCode, waybillCode);
            verify(parcelNotifyRecordRepository).findByStationCodeAndWaybillCode(stationCode, waybillCode);
        }
    }

    @Test
    void querySingleParcel_shouldThrowBusinessException_whenParcelNotFound() {
        // given
        String stationCode = "STATION1";
        String waybillCode = "WB123";

        when(parcelRepository.findByStationCodeAndWaybillCode(stationCode, waybillCode)).thenReturn(Optional.empty());

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> parcelQueryService.querySingleParcel(stationCode, waybillCode));
        assertEquals(ErrorCode.PARCEL_NOT_EXIST, exception.getCommonErrorCode());
        verify(parcelRepository).findByStationCodeAndWaybillCode(stationCode, waybillCode);
        verifyNoInteractions(parcelOpRecordRepository, parcelNotifyRecordRepository);
    }

    @Test
    void pageQuery_shouldReturnPageResponse_whenQueryValid() {
        // given
        ParcelPageQuery query = new ParcelPageQuery();
        query.setStationCode("STATION1");
        query.setPageNum(1);
        query.setPageSize(10);
        List<Parcel> parcels = List.of(parcel);
        Page<Parcel> page = new PageImpl<>(parcels, PageRequest.of(0, 10), 1);

        when(parcelRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        try (MockedStatic<TimeConvertUtils> mockedTimeConvertUtils = mockStatic(TimeConvertUtils.class)) {
            mockedTimeConvertUtils.when(() -> TimeConvertUtils.toEpochMilli(any())).thenReturn(123456789L);

            // when
            PageResponse<ParcelBaseInfoDTO> result = parcelQueryService.pageQuery(query);

            // then
            assertNotNull(result);
            assertEquals(1, result.getRecordList().size());
            assertEquals(1L, result.getTotal());
            assertEquals(1, result.getTotalPage());
            assertEquals(1, result.getPageNum());
            assertEquals(10, result.getPageSize());
            verify(parcelRepository).findAll(any(Specification.class), any(Pageable.class));
        }
    }

    @Test
    void pageQuery_shouldReturnEmptyPage_whenNoParcels() {
        // given
        ParcelPageQuery query = new ParcelPageQuery();
        query.setStationCode("STATION1");
        query.setPageNum(1);
        query.setPageSize(10);
        List<Parcel> parcels = List.of();
        Page<Parcel> page = new PageImpl<>(parcels, PageRequest.of(0, 10), 0);

        when(parcelRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        // when
        PageResponse<ParcelBaseInfoDTO> result = parcelQueryService.pageQuery(query);

        // then
        assertNotNull(result);
        assertTrue(result.getRecordList().isEmpty());
        assertEquals(0L, result.getTotal());
        assertEquals(0, result.getTotalPage());
        verify(parcelRepository).findAll(any(Specification.class), any(Pageable.class));
    }

}