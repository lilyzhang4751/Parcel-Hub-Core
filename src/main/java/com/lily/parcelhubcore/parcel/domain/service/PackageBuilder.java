package com.lily.parcelhubcore.parcel.domain.service;

import java.time.Instant;

import com.lily.parcelhubcore.parcel.application.command.ParcelInBoundCommand;
import com.lily.parcelhubcore.parcel.domain.dto.ParcelPackDTO;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.ParcelDO;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.ParcelOpRecordDO;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.WaybillRegistryDO;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.repository.ParcelRepository;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.repository.WaybillRegistryRepository;
import com.lily.parcelhubcore.shared.enums.OperateTypeEnum;
import com.lily.parcelhubcore.shared.enums.WaybillRegistryStatusEnum;
import com.lily.parcelhubcore.shared.enums.WaybillStatusEnum;
import com.lily.parcelhubcore.shared.util.CurrentUserUtil;
import com.lily.parcelhubcore.shared.util.TimeConvertUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class PackageBuilder {

    @Resource
    private WaybillRegistryRepository waybillRegistryRepository;

    @Resource
    private ParcelRepository parcelRepository;

    public ParcelPackDTO buildInParcelPackDTO(ParcelInBoundCommand command) {
        var stationCode = command.getStationCode();
        var waybillCode = command.getWaybillCode();
        // 运单注册表
        var waybillRegistryDO = new WaybillRegistryDO();
        waybillRegistryDO.setStationCode(stationCode);
        waybillRegistryDO.setWaybillCode(waybillCode);
        waybillRegistryDO.setStatus(WaybillRegistryStatusEnum.OCCUPIED.getCode());

        var nowInstant = TimeConvertUtils.toInstant(System.currentTimeMillis());
        // 包裹操作记录
        var parcelOpRecordDO = buildOpRecord(stationCode, waybillCode, nowInstant);
        parcelOpRecordDO.setOpType(OperateTypeEnum.IN.getCode());

        // 包裹表
        var parcelDO = new ParcelDO();
        BeanUtils.copyProperties(command, parcelDO);
        parcelDO.setStatus(WaybillStatusEnum.INBOUND.getCode());
        parcelDO.setLatestInboundTime(nowInstant);

        var pack = ParcelPackDTO.builder().waybillRegistryDO(waybillRegistryDO).parcelOpRecordDO(parcelOpRecordDO).build();

        // 查看包裹是首次入库还是多次入库
        var oldParcel = parcelRepository.findFirstByStationCodeAndWaybillCode(stationCode, waybillCode);

        if (oldParcel != null) {
            // 后续用save方法，有id时更新，无id时插入
            parcelDO.setId(oldParcel.getId());
        }
        pack.setParcelDO(parcelDO);
        return pack;
    }

    public ParcelPackDTO buildParcelPackByType(WaybillRegistryDO waybillRegistry, ParcelDO parcel, OperateTypeEnum operateTypeEnum) {
        waybillRegistry.setStatus(WaybillRegistryStatusEnum.RELEASE.getCode());

        // 包裹操作记录
        var nowInstant = TimeConvertUtils.toInstant(System.currentTimeMillis());
        var parcelOpRecordDO = buildOpRecord(parcel.getStationCode(), parcel.getWaybillCode(), nowInstant);
        parcelOpRecordDO.setOpType(operateTypeEnum.getCode());

        if (OperateTypeEnum.OUT.equals(operateTypeEnum)) {
            parcel.setStatus(WaybillStatusEnum.OUTBOUND.getCode());
        }
        if (OperateTypeEnum.RETURN.equals(operateTypeEnum)) {
            parcel.setStatus(WaybillStatusEnum.RETURNED.getCode());
        }

        parcel.setLatestOutboundTime(nowInstant);
        return ParcelPackDTO.builder().waybillRegistryDO(waybillRegistry).parcelDO(parcel).parcelOpRecordDO(parcelOpRecordDO).build();
    }

    public ParcelPackDTO buildTransferParcelPackDTO(ParcelDO parcel, String shelfCode, String pickupCode) {
        parcel.setShelfCode(shelfCode);
        parcel.setPickupCode(pickupCode);
        // 包裹操作记录
        var nowInstant = TimeConvertUtils.toInstant(System.currentTimeMillis());
        var parcelOpRecordDO = buildOpRecord(parcel.getStationCode(), parcel.getWaybillCode(), nowInstant);
        parcelOpRecordDO.setOpType(OperateTypeEnum.TRANSFER.getCode());

        parcel.setLatestOutboundTime(nowInstant);
        return ParcelPackDTO.builder().parcelDO(parcel).parcelOpRecordDO(parcelOpRecordDO).build();
    }

    private ParcelOpRecordDO buildOpRecord(String stationCode, String waybillCode, Instant nowInstant) {
        var parcelOpRecordDO = new ParcelOpRecordDO();
        parcelOpRecordDO.setStationCode(stationCode);
        parcelOpRecordDO.setWaybillCode(waybillCode);
        parcelOpRecordDO.setOperatorCode(CurrentUserUtil.getUserCode());
        parcelOpRecordDO.setOperatorName(CurrentUserUtil.getUsername());
        parcelOpRecordDO.setOpTime(nowInstant);
        return parcelOpRecordDO;
    }
}
