package com.lily.parcelhubcore.parcel.domain.service;

import com.lily.parcelhubcore.parcel.domain.dto.InParcelPackDTO;
import com.lily.parcelhubcore.parcel.domain.enums.ErrorCode;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.repository.ParcelOpRecordRepository;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.repository.ParcelRepository;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.repository.WaybillRegistryRepository;
import com.lily.parcelhubcore.shared.enums.WaybillRegistryStatusEnum;
import com.lily.parcelhubcore.shared.exception.BusinessException;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

@Component
public class ParcelBaseService {

    @Resource
    private WaybillRegistryRepository waybillRegistryRepository;

    @Resource
    private ParcelRepository parcelRepository;

    @Resource
    private ParcelOpRecordRepository parcelOpRecordRepository;

    public void waybillInBoundVerify(String waybillCode) {
        // 查询是否已在任何站点入库
        var exist = waybillRegistryRepository.existsByWaybillCodeAndStatus(waybillCode, WaybillRegistryStatusEnum.OCCUPIED.getCode());
        if (exist) {
            throw new BusinessException(ErrorCode.PARCEL_EXIST);
        }
    }

    @Transactional
    public void updateDBAndSendMsg(InParcelPackDTO packDTO) {
        waybillRegistryRepository.save(packDTO.getWaybillRegistryDO());
       // parcelOpRecordRepository.save(packDTO.getParcelOpRecordDO());
        if (packDTO.getInsertParcelDO() != null) {
            parcelRepository.save(packDTO.getInsertParcelDO());
        }
        if (packDTO.getUpdateParcelDO() != null) {
            // 有id插入
            parcelRepository.save(packDTO.getUpdateParcelDO());
        }
        // todo 发送msg
    }

}
