package com.lily.parcelhubcore.parcel.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;

import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.ParcelOpRecord;
import com.lily.parcelhubcore.shared.enums.OperateTypeEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ParcelOpRecordRepositoryTest {

    @Autowired
    private ParcelOpRecordRepository parcelOpRecordRepository;

    @Test
    void findByStationCodeAndWaybillCode_shouldReturnList_whenRecordsExist() {
        // given
        ParcelOpRecord record1 = new ParcelOpRecord();
        record1.setStationCode("STATION1");
        record1.setWaybillCode("WB1");
        record1.setOpTime(Instant.now());
        record1.setOpType(OperateTypeEnum.IN.getCode());
        record1.setDetail("Detail1");
        record1.setOperatorCode("OP1");
        record1.setOperatorName("Operator1");
        record1.setUniqueId("UNIQUE1");
        parcelOpRecordRepository.saveAndFlush(record1);

        ParcelOpRecord record2 = new ParcelOpRecord();
        record2.setStationCode("STATION1");
        record2.setWaybillCode("WB1");
        record2.setOpTime(Instant.now());
        record2.setOpType(OperateTypeEnum.TRANSFER.getCode());
        record2.setDetail("Detail2");
        record2.setOperatorCode("OP2");
        record2.setOperatorName("Operator2");
        record2.setUniqueId("UNIQUE2");
        parcelOpRecordRepository.saveAndFlush(record2);

        // Record with different waybillCode
        ParcelOpRecord record3 = new ParcelOpRecord();
        record3.setStationCode("STATION1");
        record3.setWaybillCode("WB2");
        record3.setOpTime(Instant.now());
        record3.setOpType(OperateTypeEnum.OUT.getCode());
        record3.setDetail("Detail3");
        record3.setOperatorCode("OP3");
        record3.setOperatorName("Operator3");
        record3.setUniqueId("UNIQUE3");
        parcelOpRecordRepository.saveAndFlush(record3);

        // when
        List<ParcelOpRecord> result = parcelOpRecordRepository.findByStationCodeAndWaybillCode("STATION1", "WB1");

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(ParcelOpRecord::getStationCode).containsOnly("STATION1");
        assertThat(result).extracting(ParcelOpRecord::getWaybillCode).containsOnly("WB1");
    }

    @Test
    void findByStationCodeAndWaybillCode_shouldReturnEmptyList_whenNoRecordsMatch() {
        // given
        ParcelOpRecord record = new ParcelOpRecord();
        record.setStationCode("STATION1");
        record.setWaybillCode("WB1");
        record.setOpTime(Instant.now());
        record.setOpType(1);
        record.setDetail("Detail");
        record.setOperatorCode("OP");
        record.setOperatorName("Operator");
        record.setUniqueId("UNIQUE");
        parcelOpRecordRepository.saveAndFlush(record);

        // when
        List<ParcelOpRecord> result = parcelOpRecordRepository.findByStationCodeAndWaybillCode("STATION2", "WB1");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findByStationCodeAndWaybillCode_shouldReturnEmptyList_whenNoRecordsExist() {
        // given - no records

        // when
        List<ParcelOpRecord> result = parcelOpRecordRepository.findByStationCodeAndWaybillCode("STATION1", "WB1");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void save_shouldThrowException_whenUniqueIdAlreadyExists() {
        // given
        ParcelOpRecord record1 = new ParcelOpRecord();
        record1.setStationCode("STATION1");
        record1.setWaybillCode("WB1");
        record1.setOpTime(Instant.now());
        record1.setOpType(1);
        record1.setDetail("Detail1");
        record1.setOperatorCode("OP1");
        record1.setOperatorName("Operator1");
        record1.setUniqueId("UNIQUE1");
        parcelOpRecordRepository.saveAndFlush(record1);

        ParcelOpRecord record2 = new ParcelOpRecord();
        record2.setStationCode("STATION2");
        record2.setWaybillCode("WB2");
        record2.setOpTime(Instant.now());
        record2.setOpType(2);
        record2.setDetail("Detail2");
        record2.setOperatorCode("OP2");
        record2.setOperatorName("Operator2");
        record2.setUniqueId("UNIQUE1");  // same unique_id

        // when & then
        assertThatThrownBy(() -> parcelOpRecordRepository.saveAndFlush(record2))
            .isInstanceOf(DataIntegrityViolationException.class);
    }
}