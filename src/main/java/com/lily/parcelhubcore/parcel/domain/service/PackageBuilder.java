package com.lily.parcelhubcore.parcel.domain.service;

import com.lily.parcelhubcore.parcel.application.command.ParcelInBoundCommand;
import com.lily.parcelhubcore.parcel.domain.dto.InParcelPackDTO;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.ParcelDO;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.ParcelOpRecordDO;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.WaybillRegistryDO;
import com.lily.parcelhubcore.shared.enums.OperateTypeEnum;
import com.lily.parcelhubcore.shared.enums.WaybillRegistryStatusEnum;
import com.lily.parcelhubcore.shared.enums.WaybillStatusEnum;
import com.lily.parcelhubcore.shared.util.TimeConvertUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class PackageBuilder {

    public InParcelPackDTO buildInParcelPackDTO(ParcelInBoundCommand command, ParcelDO oldParcel) {
        // 运单注册表
        var waybillRegistryDO = new WaybillRegistryDO();
        waybillRegistryDO.setStationCode(command.getStationCode());
        waybillRegistryDO.setWaybillCode(command.getWaybillCode());
        waybillRegistryDO.setStatus(WaybillRegistryStatusEnum.OCCUPIED.getCode());

        // todo 包裹操作记录
        var parcelOpRecordDO = new ParcelOpRecordDO();
        parcelOpRecordDO.setStationCode(command.getStationCode());
        parcelOpRecordDO.setWaybillCode(command.getWaybillCode());
        var nowInstant = TimeConvertUtils.toInstant(System.currentTimeMillis());
        parcelOpRecordDO.setOpTime(nowInstant);
        parcelOpRecordDO.setOpType(OperateTypeEnum.IN.getCode());

        // 包裹表
        var parcelDO = new ParcelDO();
        BeanUtils.copyProperties(command, parcelDO);
        parcelDO.setStatus(WaybillStatusEnum.INBOUND.getCode());
        parcelDO.setLatestInboundTime(nowInstant);

        var pack = InParcelPackDTO.builder()
                .waybillRegistryDO(waybillRegistryDO).parcelOpRecordDO(parcelOpRecordDO).build();

        if (oldParcel == null) {
            pack.setInsertParcelDO(parcelDO);
        } else {
            // 后续用save方法，有id时更新，无id时插入
            parcelDO.setId(oldParcel.getId());
            pack.setUpdateParcelDO(parcelDO);
        }
        return pack;
    }
}
