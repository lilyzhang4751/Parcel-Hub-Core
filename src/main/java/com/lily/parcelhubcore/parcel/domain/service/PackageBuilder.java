package com.lily.parcelhubcore.parcel.domain.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.lily.parcelhubcore.parcel.application.command.ParcelInBoundCommand;
import com.lily.parcelhubcore.parcel.domain.dto.ParcelNotifyEvent;
import com.lily.parcelhubcore.parcel.domain.dto.ParcelOpSyncEvent;
import com.lily.parcelhubcore.parcel.domain.dto.ParcelPackDTO;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.Parcel;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.ParcelOpRecord;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.WaybillRegistry;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.repository.ParcelRepository;
import com.lily.parcelhubcore.shared.enums.NotifyChannelEnum;
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

    private static final String TRANSFER_DESC = "移库：货架号：%s->%s, 取件码：%s->%s";

    @Resource
    private ParcelRepository parcelRepository;

    public ParcelPackDTO buildInParcelPackDTO(ParcelInBoundCommand command) {
        var stationCode = command.getStationCode();
        var waybillCode = command.getWaybillCode();
        // 运单注册表
        var waybillRegistryDO = new WaybillRegistry();
        waybillRegistryDO.setStationCode(stationCode);
        waybillRegistryDO.setWaybillCode(waybillCode);
        waybillRegistryDO.setStatus(WaybillRegistryStatusEnum.OCCUPIED.getCode());

        var nowInstant = TimeConvertUtils.toInstant(System.currentTimeMillis());
        // 包裹操作记录
        var parcelOpRecordDO = buildOpRecord(stationCode, waybillCode, nowInstant);
        parcelOpRecordDO.setOpType(OperateTypeEnum.IN.getCode());
        parcelOpRecordDO.setDetail(OperateTypeEnum.IN.getDesc());
        var opSyncEvent = buildParcelOpSyncEvent(parcelOpRecordDO);

        // 包裹表
        var parcelDO = new Parcel();
        BeanUtils.copyProperties(command, parcelDO);
        parcelDO.setStatus(WaybillStatusEnum.INBOUND.getCode());
        parcelDO.setLatestInboundTime(nowInstant);

        // 包裹通知记录
        var notifyEvent = buildNotifyEvent(stationCode, waybillCode, nowInstant, command.getPickupCode());
        // 短信，app都推送
        notifyEvent.setChannelList(new ArrayList<>(List.of(NotifyChannelEnum.SMS.getDesc(), NotifyChannelEnum.APP.getDesc())));

        var pack = ParcelPackDTO.builder().waybillRegistry(waybillRegistryDO).parcelOpRecord(parcelOpRecordDO)
                .parcelOpSyncEvent(opSyncEvent).parcelNotifyEvent(notifyEvent).waybillCode(waybillRegistryDO.getWaybillCode()).build();

        // 查看包裹是首次入库还是多次入库
        var oldParcel = parcelRepository.findByStationCodeAndWaybillCode(stationCode, waybillCode);

        // 后续用save方法，有id时更新，无id时插入
        oldParcel.ifPresent(old -> parcelDO.setId(old.getId()));
        pack.setParcel(parcelDO);
        return pack;
    }

    public ParcelPackDTO buildParcelPackByType(WaybillRegistry waybillRegistry, Parcel parcel, OperateTypeEnum operateTypeEnum) {
        waybillRegistry.setStatus(WaybillRegistryStatusEnum.RELEASE.getCode());

        // 包裹操作记录
        var nowInstant = TimeConvertUtils.toInstant(System.currentTimeMillis());
        var parcelOpRecordDO = buildOpRecord(parcel.getStationCode(), parcel.getWaybillCode(), nowInstant);
        parcelOpRecordDO.setOpType(operateTypeEnum.getCode());
        parcelOpRecordDO.setDetail(operateTypeEnum.getDesc());
        var opSyncEvent = buildParcelOpSyncEvent(parcelOpRecordDO);
        // 包裹通知记录
        var notifyEvent = buildNotifyEvent(parcel.getStationCode(), parcel.getWaybillCode(), nowInstant, parcel.getPickupCode());
        // app推送
        notifyEvent.setChannelList(new ArrayList<>(List.of(NotifyChannelEnum.APP.getDesc())));

        if (OperateTypeEnum.OUT.equals(operateTypeEnum)) {
            parcel.setStatus(WaybillStatusEnum.OUTBOUND.getCode());
        }
        if (OperateTypeEnum.RETURN.equals(operateTypeEnum)) {
            parcel.setStatus(WaybillStatusEnum.RETURNED.getCode());
        }

        parcel.setLatestOutboundTime(nowInstant);
        return ParcelPackDTO.builder().waybillCode(parcel.getWaybillCode()).waybillRegistry(waybillRegistry).parcel(parcel).parcelOpRecord(parcelOpRecordDO).parcelOpSyncEvent(opSyncEvent).parcelNotifyEvent(notifyEvent).build();
    }

    public ParcelPackDTO buildTransferParcelPackDTO(Parcel parcel, String shelfCode, String pickupCode) {
        var oldShelfCode = parcel.getShelfCode();
        var oldPickupCode = parcel.getPickupCode();

        parcel.setShelfCode(shelfCode);
        parcel.setPickupCode(pickupCode);
        var nowInstant = TimeConvertUtils.toInstant(System.currentTimeMillis());
        parcel.setLatestOutboundTime(nowInstant);
        // 包裹操作记录
        var parcelOpRecordDO = buildOpRecord(parcel.getStationCode(), parcel.getWaybillCode(), nowInstant);
        parcelOpRecordDO.setOpType(OperateTypeEnum.TRANSFER.getCode());
        var desc = String.format(TRANSFER_DESC, oldShelfCode, shelfCode, oldPickupCode, parcel);
        parcelOpRecordDO.setDetail(desc);
        var opSyncEvent = buildParcelOpSyncEvent(parcelOpRecordDO);
        // 包裹通知记录
        var notifyEvent = buildNotifyEvent(parcel.getStationCode(), parcel.getWaybillCode(), nowInstant, pickupCode);
        // 短信，app都推送
        notifyEvent.setChannelList(new ArrayList<>(List.of(NotifyChannelEnum.SMS.getDesc(), NotifyChannelEnum.APP.getDesc())));

        return ParcelPackDTO.builder().waybillCode(parcel.getWaybillCode()).parcel(parcel).parcelOpRecord(parcelOpRecordDO).parcelOpSyncEvent(opSyncEvent).parcelNotifyEvent(notifyEvent).build();
    }

    private ParcelOpRecord buildOpRecord(String stationCode, String waybillCode, Instant nowInstant) {
        var parcelOpRecordDO = new ParcelOpRecord();
        parcelOpRecordDO.setStationCode(stationCode);
        parcelOpRecordDO.setWaybillCode(waybillCode);
        parcelOpRecordDO.setOperatorCode(CurrentUserUtil.getUserCode());
        parcelOpRecordDO.setOperatorName(CurrentUserUtil.getUsername());
        parcelOpRecordDO.setOpTime(nowInstant);
        parcelOpRecordDO.setUniqueId(UUID.randomUUID().toString());
        return parcelOpRecordDO;
    }

    private ParcelNotifyEvent buildNotifyEvent(String stationCode, String waybillCode, Instant nowInstant, String pickupCode) {
        var notifyEvent = new ParcelNotifyEvent();
        notifyEvent.setStationCode(stationCode);
        notifyEvent.setWaybillCode(waybillCode);
        notifyEvent.setPickupCode(pickupCode);
        notifyEvent.setOperatorCode(CurrentUserUtil.getUserCode());
        notifyEvent.setOperatorName(CurrentUserUtil.getUsername());
        notifyEvent.setNotifyTime(nowInstant);
        notifyEvent.setEventId(UUID.randomUUID().toString());
        return notifyEvent;
    }

    private ParcelOpSyncEvent buildParcelOpSyncEvent(ParcelOpRecord opRecord) {
        var opSyncEvent = new ParcelOpSyncEvent();
        BeanUtils.copyProperties(opRecord, opSyncEvent);
        opSyncEvent.setEventId(opRecord.getUniqueId());
        return opSyncEvent;
    }
}
