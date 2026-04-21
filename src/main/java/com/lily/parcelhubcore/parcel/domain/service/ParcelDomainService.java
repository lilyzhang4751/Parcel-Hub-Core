package com.lily.parcelhubcore.parcel.domain.service;

import com.lily.parcelhubcore.parcel.common.enums.ErrorCode;
import com.lily.parcelhubcore.parcel.domain.dto.ParcelPackDTO;
import com.lily.parcelhubcore.parcel.infrastructure.kafka.config.KafkaTopicConfig;
import com.lily.parcelhubcore.parcel.infrastructure.kafka.producer.OutboxDomainEventPublisher;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.repository.ParcelOpRecordRepository;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.repository.ParcelRepository;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.repository.WaybillRegistryRepository;
import com.lily.parcelhubcore.shared.enums.WaybillRegistryStatusEnum;
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

    @Transactional
    public void updateDBAndSendMsg(ParcelPackDTO packDTO) {
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
