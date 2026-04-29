package com.lily.parcelhubcore.parcel.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.Parcel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@ActiveProfiles("test")
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ParcelRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private ParcelRepository parcelRepository;

    @Test
    void findByStationCodeAndWaybillCode_shouldReturnParcel_whenExists() {
        // given
        Parcel parcel = new Parcel();
        parcel.setStationCode("STATION1");
        parcel.setWaybillCode("WB1");
        parcel.setPickupCode("PICK1");
        parcel.setShelfCode("SHELF1");
        parcel.setRecipientName("Recipient1");
        parcel.setRecipientMobile("1234567890");
        parcel.setStatus(0);
        parcel.setNotifyStatus(0);
        parcel.setLatestInboundTime(Instant.now());
        parcel.setLatestOutboundTime(Instant.now().plusSeconds(3600));
        parcelRepository.saveAndFlush(parcel);

        // when
        Optional<Parcel> result = parcelRepository.findByStationCodeAndWaybillCode("STATION1", "WB1");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getStationCode()).isEqualTo("STATION1");
        assertThat(result.get().getWaybillCode()).isEqualTo("WB1");
    }

    @Test
    void findByStationCodeAndWaybillCode_shouldReturnEmpty_whenNotExists() {
        // given - no parcel

        // when
        Optional<Parcel> result = parcelRepository.findByStationCodeAndWaybillCode("STATION1", "WB1");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findByStationCodeAndShelfCode_shouldReturnList_whenParcelsExist() {
        // given
        Parcel parcel1 = new Parcel();
        parcel1.setStationCode("STATION1");
        parcel1.setWaybillCode("WB1");
        parcel1.setPickupCode("PICK1");
        parcel1.setShelfCode("SHELF1");
        parcel1.setRecipientName("Recipient1");
        parcel1.setRecipientMobile("1234567890");
        parcel1.setStatus(0);
        parcel1.setNotifyStatus(0);
        parcel1.setLatestInboundTime(Instant.now());
        parcel1.setLatestOutboundTime(Instant.now().plusSeconds(3600));
        parcelRepository.saveAndFlush(parcel1);

        Parcel parcel2 = new Parcel();
        parcel2.setStationCode("STATION1");
        parcel2.setWaybillCode("WB2");
        parcel2.setPickupCode("PICK2");
        parcel2.setShelfCode("SHELF1");
        parcel2.setRecipientName("Recipient2");
        parcel2.setRecipientMobile("0987654321");
        parcel2.setStatus(1);
        parcel2.setNotifyStatus(1);
        parcel2.setLatestInboundTime(Instant.now());
        parcel2.setLatestOutboundTime(Instant.now().plusSeconds(7200));
        parcelRepository.saveAndFlush(parcel2);

        // Parcel with different shelf
        Parcel parcel3 = new Parcel();
        parcel3.setStationCode("STATION1");
        parcel3.setWaybillCode("WB3");
        parcel3.setPickupCode("PICK3");
        parcel3.setShelfCode("SHELF2");
        parcel3.setRecipientName("Recipient3");
        parcel3.setRecipientMobile("1111111111");
        parcel3.setStatus(2);
        parcel3.setNotifyStatus(2);
        parcel3.setLatestInboundTime(Instant.now());
        parcel3.setLatestOutboundTime(Instant.now().plusSeconds(10800));
        parcelRepository.saveAndFlush(parcel3);

        // when
        List<Parcel> result = parcelRepository.findByStationCodeAndShelfCode("STATION1", "SHELF1");

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Parcel::getWaybillCode).contains("WB1", "WB2");
    }

    @Test
    void findByStationCodeAndShelfCode_shouldReturnEmptyList_whenNoParcelsMatch() {
        // given
        Parcel parcel = new Parcel();
        parcel.setStationCode("STATION1");
        parcel.setWaybillCode("WB1");
        parcel.setPickupCode("PICK1");
        parcel.setShelfCode("SHELF1");
        parcel.setRecipientName("Recipient1");
        parcel.setRecipientMobile("1234567890");
        parcel.setStatus(0);
        parcel.setNotifyStatus(0);
        parcel.setLatestInboundTime(Instant.now());
        parcel.setLatestOutboundTime(Instant.now().plusSeconds(3600));
        parcelRepository.saveAndFlush(parcel);

        // when
        List<Parcel> result = parcelRepository.findByStationCodeAndShelfCode("STATION1", "SHELF2");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void existsByStationCodeAndPickupCode_shouldReturnTrue_whenExists() {
        // given
        Parcel parcel = new Parcel();
        parcel.setStationCode("STATION1");
        parcel.setWaybillCode("WB1");
        parcel.setPickupCode("PICK1");
        parcel.setShelfCode("SHELF1");
        parcel.setRecipientName("Recipient1");
        parcel.setRecipientMobile("1234567890");
        parcel.setStatus(0);
        parcel.setNotifyStatus(0);
        parcel.setLatestInboundTime(Instant.now());
        parcel.setLatestOutboundTime(Instant.now().plusSeconds(3600));
        parcelRepository.saveAndFlush(parcel);

        // when
        boolean result = parcelRepository.existsByStationCodeAndPickupCode("STATION1", "PICK1");

        // then
        assertThat(result).isTrue();
    }

    @Test
    void existsByStationCodeAndPickupCode_shouldReturnFalse_whenNotExists() {
        // given
        Parcel parcel = new Parcel();
        parcel.setStationCode("STATION1");
        parcel.setWaybillCode("WB1");
        parcel.setPickupCode("PICK1");
        parcel.setShelfCode("SHELF1");
        parcel.setRecipientName("Recipient1");
        parcel.setRecipientMobile("1234567890");
        parcel.setStatus(0);
        parcel.setNotifyStatus(0);
        parcel.setLatestInboundTime(Instant.now());
        parcel.setLatestOutboundTime(Instant.now().plusSeconds(3600));
        parcelRepository.saveAndFlush(parcel);

        // when
        boolean result = parcelRepository.existsByStationCodeAndPickupCode("STATION1", "PICK2");

        // then
        assertThat(result).isFalse();
    }

    @Test
    void save_shouldThrowException_whenStationCodeAndWaybillCodeAlreadyExist() {
        // given
        Parcel parcel1 = new Parcel();
        parcel1.setStationCode("STATION1");
        parcel1.setWaybillCode("WB1");
        parcel1.setPickupCode("PICK1");
        parcel1.setShelfCode("SHELF1");
        parcel1.setRecipientName("Recipient1");
        parcel1.setRecipientMobile("1234567890");
        parcel1.setStatus(0);
        parcel1.setNotifyStatus(0);
        parcel1.setLatestInboundTime(Instant.now());
        parcel1.setLatestOutboundTime(Instant.now().plusSeconds(3600));
        parcelRepository.saveAndFlush(parcel1);

        Parcel parcel2 = new Parcel();
        parcel2.setStationCode("STATION1");  // same station
        parcel2.setWaybillCode("WB1");  // same waybill
        parcel2.setPickupCode("PICK2");
        parcel2.setShelfCode("SHELF2");
        parcel2.setRecipientName("Recipient2");
        parcel2.setRecipientMobile("0987654321");
        parcel2.setStatus(1);
        parcel2.setNotifyStatus(1);
        parcel2.setLatestInboundTime(Instant.now());
        parcel2.setLatestOutboundTime(Instant.now().plusSeconds(7200));

        // when & then
        assertThatThrownBy(() -> parcelRepository.saveAndFlush(parcel2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}