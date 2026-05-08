package com.lily.parcelhubcore.parcel.domain.service;

import static com.lily.parcelhubcore.parcel.common.enums.ErrorCode.PARCEL_NOT_EXIST;
import static com.lily.parcelhubcore.parcel.common.enums.ErrorCode.PARCEL_NOT_INBOUND;

import java.util.Objects;

import com.lily.parcelhubcore.parcel.common.enums.ErrorCode;
import com.lily.parcelhubcore.parcel.domain.dto.ParcelPackDTO;
import com.lily.parcelhubcore.parcel.infrastructure.kafka.config.KafkaTopicConfig;
import com.lily.parcelhubcore.parcel.infrastructure.kafka.producer.OutboxDomainEventPublisher;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.Parcel;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.repository.ParcelOpRecordRepository;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.repository.ParcelRepository;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.repository.WaybillRegistryRepository;
import com.lily.parcelhubcore.shared.enums.WaybillRegistryStatusEnum;
import com.lily.parcelhubcore.shared.enums.WaybillStatusEnum;
import com.lily.parcelhubcore.shared.exception.BusinessException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ParcelDomainService {

    @Resource
    private WaybillRegistryRepository waybillRegistryRepository;

    @Resource
    private ParcelRepository parcelRepository;

    @Resource
    private ParcelOpRecordRepository parcelOpRecordRepository;

    @Resource
    private OutboxDomainEventPublisher outboxDomainEventPublisher;

    public void waybillInBoundVerify(String waybillCode) {
        // 查询是否已在任何站点入库
        var exist = waybillRegistryRepository.existsByWaybillCodeAndStatus(waybillCode, WaybillRegistryStatusEnum.OCCUPIED.getCode());
        if (exist) {
            throw new BusinessException(ErrorCode.PARCEL_ALREADY_EXIST);
        }
    }

    public Parcel getInboundParcelDO(String stationCode, String waybillCode) {
        // 查询包裹是否存在
        var parcel = parcelRepository.findByStationCodeAndWaybillCode(stationCode, waybillCode);
        if (parcel.isEmpty()) {
            throw new BusinessException(PARCEL_NOT_EXIST);
        }
        // 包裹是否还在库
        if (!Objects.equals(parcel.get().getStatus(), WaybillStatusEnum.INBOUND.getCode())) {
            throw new BusinessException(PARCEL_NOT_INBOUND);
        }
        return parcel.get();
    }

    @Transactional
    public void updateDBAndSendMsg(ParcelPackDTO packDTO) {
        // save操作，有主键id的时候，更新；没有则插入
        if (packDTO.getWaybillRegistry() != null) {
            waybillRegistryRepository.save(packDTO.getWaybillRegistry());
        }
        parcelRepository.save(packDTO.getParcel());
        parcelOpRecordRepository.save(packDTO.getParcelOpRecord());
        // 发送msg
        outboxDomainEventPublisher.publish(KafkaTopicConfig.TOPIC_PARCEL_NOTIFY,
                packDTO.getWaybillCode(), packDTO.getParcelNotifyEvent());

        outboxDomainEventPublisher.publish(KafkaTopicConfig.TOPIC_PARCEL_OP_SYNC,
                packDTO.getWaybillCode(), packDTO.getParcelOpSyncEvent());
    }

}
