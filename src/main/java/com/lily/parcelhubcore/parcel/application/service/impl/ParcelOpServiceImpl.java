package com.lily.parcelhubcore.parcel.application.service.impl;

import static com.lily.parcelhubcore.parcel.common.constants.Constants.LOCK_TIME;
import static com.lily.parcelhubcore.parcel.common.constants.Constants.PICKUP_CACHE_HOURS;
import static com.lily.parcelhubcore.parcel.common.enums.ErrorCode.PARCEL_NOT_EXIST;
import static com.lily.parcelhubcore.parcel.common.enums.ErrorCode.PARCEL_NOT_INBOUND;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.lily.parcelhubcore.parcel.application.command.ParcelInBoundCommand;
import com.lily.parcelhubcore.parcel.application.command.PrepareInCommand;
import com.lily.parcelhubcore.parcel.application.dto.PrepareInDTO;
import com.lily.parcelhubcore.parcel.application.service.ParcelOpService;
import com.lily.parcelhubcore.parcel.domain.service.PackageBuilder;
import com.lily.parcelhubcore.parcel.domain.service.ParcelDomainService;
import com.lily.parcelhubcore.parcel.domain.service.PickupCodeService;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.Parcel;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.repository.ParcelRepository;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.repository.WaybillRegistryRepository;
import com.lily.parcelhubcore.parcel.common.constants.KeyConstants;
import com.lily.parcelhubcore.parcel.common.enums.ErrorCode;
import com.lily.parcelhubcore.parcel.common.util.CommonUtil;
import com.lily.parcelhubcore.shared.cache.CacheService;
import com.lily.parcelhubcore.shared.enums.OperateTypeEnum;
import com.lily.parcelhubcore.shared.enums.WaybillRegistryStatusEnum;
import com.lily.parcelhubcore.shared.enums.WaybillStatusEnum;
import com.lily.parcelhubcore.shared.exception.BusinessException;
import com.lily.parcelhubcore.shared.lock.Lock;
import com.lily.parcelhubcore.shared.util.CurrentUserUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class ParcelOpServiceImpl implements ParcelOpService {

    @Resource
    private Lock lock;

    @Resource
    private ParcelDomainService parcelDomainService;

    @Resource
    private CacheService cacheService;

    @Resource
    private PickupCodeService pickupCodeService;

    @Resource
    private ParcelRepository parcelRepository;

    @Resource
    private PackageBuilder packageBuilder;

    @Resource
    private WaybillRegistryRepository waybillRegistryRepository;

    @Override
    public PrepareInDTO prepareIn(PrepareInCommand bo) {
        var dto = new PrepareInDTO();
        BeanUtils.copyProperties(bo, dto);

        var stationCode = CurrentUserUtil.getStationCode();
        var waybillCode = bo.getWaybillCode();

        // add lock
        var lockKey = KeyConstants.getWaybillCodeLock(waybillCode);
        try {
            if (!lock.tryLock(lockKey, LOCK_TIME, TimeUnit.MILLISECONDS)) {
                throw new BusinessException(ErrorCode.CURRENT_EXCEPTION);
            }

            // 查询是否已在任何站点入库
            parcelDomainService.waybillInBoundVerify(waybillCode);

            // 查看是否有缓存的取件码（和货架号匹配），有的话直接返回（幂等）；
            var cacheKey = KeyConstants.getWaybillPickupCacheKey(stationCode, waybillCode);
            var pickupCode = cacheService.get(cacheKey);
            if (StringUtils.isNotBlank(pickupCode)) {
                if (CommonUtil.pickupCodeMachShelfCode(pickupCode, bo.getShelfCode())) {
                    dto.setPickupCode(pickupCode);
                    return dto;
                }
            }
            //  生成取件码
            var newPickupCode = pickupCodeService.genarate(stationCode, bo.getShelfCode());
            dto.setPickupCode(newPickupCode);
            //  设置取件码缓存
            cacheService.set(cacheKey, newPickupCode, PICKUP_CACHE_HOURS);
            // 返回所有信息
            return dto;
        } finally {
            lock.unlock(lockKey);
        }
    }

    @Override
    public void inbound(ParcelInBoundCommand command) {
        var stationCode = CurrentUserUtil.getStationCode();
        var waybillCode = command.getWaybillCode();
        // add lock
        var lockKey = KeyConstants.getWaybillCodeLock(waybillCode);
        try {
            if (!lock.tryLock(lockKey, LOCK_TIME, TimeUnit.MILLISECONDS)) {
                throw new BusinessException(ErrorCode.CURRENT_EXCEPTION);
            }
            // 查询是否已在任何站点入库
            parcelDomainService.waybillInBoundVerify(waybillCode);
            // 校验取件码是否有重复；
            pickupCodeService.pickupCodeExistVerify(stationCode, command.getPickupCode());
            // 构建数据库操作对象和消息体
            var packDTO = packageBuilder.buildInParcelPackDTO(command);
            // 事务内更新数据库，发送消息
            parcelDomainService.updateDBAndSendMsg(packDTO);
            // 删除该运单号的取件码缓存
            cacheService.delete(KeyConstants.getWaybillPickupCacheKey(stationCode, waybillCode));
        } finally {
            lock.unlock(lockKey);
        }
    }

    @Override
    public void outBoundOrReturn(String waybillCode, OperateTypeEnum operateTypeEnum) {
        // add lock
        var lockKey = KeyConstants.getWaybillCodeLock(waybillCode);
        try {
            if (!lock.tryLock(lockKey, LOCK_TIME, TimeUnit.MILLISECONDS)) {
                throw new BusinessException(ErrorCode.CURRENT_EXCEPTION);
            }
            var stationCode = CurrentUserUtil.getStationCode();
            // 查询注册表状态是否正常
            var waybillRegistry = waybillRegistryRepository.findByWaybillCodeAndStatus(waybillCode, WaybillRegistryStatusEnum.OCCUPIED.getCode());
            if (waybillRegistry == null || !Objects.equals(waybillRegistry.getStationCode(), stationCode)) {
                throw new BusinessException(PARCEL_NOT_EXIST);
            }

            // 查询包裹是否存在
            var parcel = getParcelDO(stationCode, waybillCode);
            // 构建数据库操作对象和消息体
            var packDTO = packageBuilder.buildParcelPackByType(waybillRegistry, parcel, operateTypeEnum);
            // 事务内更新数据库，发送消息
            parcelDomainService.updateDBAndSendMsg(packDTO);
        } finally {
            lock.unlock(lockKey);
        }
    }

    @Override
    public void transfer(String waybillCode, String shelfCode) {
        // add lock
        var lockKey = KeyConstants.getWaybillCodeLock(waybillCode);
        try {
            if (!lock.tryLock(lockKey, LOCK_TIME, TimeUnit.MILLISECONDS)) {
                throw new BusinessException(ErrorCode.CURRENT_EXCEPTION);
            }
            var stationCode = CurrentUserUtil.getStationCode();
            // 查询包裹是否存在
            var parcel = getParcelDO(stationCode, waybillCode);
            // 移库重新生成取件码
            //  生成取件码
            var newPickupCode = pickupCodeService.genarate(stationCode, shelfCode);
            // 构建数据库操作对象和消息体
            var packDTO = packageBuilder.buildTransferParcelPackDTO(parcel, shelfCode, newPickupCode);
            // 事务内更新数据库，发送消息
            parcelDomainService.updateDBAndSendMsg(packDTO);
        } finally {
            lock.unlock(lockKey);
        }
    }

    private Parcel getParcelDO(String stationCode, String waybillCode) {
// 查询包裹是否存在
        var parcel = parcelRepository.findFirstByStationCodeAndWaybillCode(stationCode, waybillCode);
        if (parcel == null) {
            throw new BusinessException(PARCEL_NOT_EXIST);
        }
        // 包裹是否还在库
        if (!Objects.equals(parcel.getStatus(), WaybillStatusEnum.INBOUND.getCode())) {
            throw new BusinessException(PARCEL_NOT_INBOUND);
        }
        return parcel;
    }
}
