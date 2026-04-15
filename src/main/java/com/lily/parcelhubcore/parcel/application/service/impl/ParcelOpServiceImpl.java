package com.lily.parcelhubcore.parcel.application.service.impl;

import static com.lily.parcelhubcore.parcel.shared.common.Constants.LOCK_TIME;
import static com.lily.parcelhubcore.parcel.shared.common.Constants.PICKUP_CACHE_HOURS;

import java.util.concurrent.TimeUnit;

import com.lily.parcelhubcore.parcel.application.command.ParcelInBoundCommand;
import com.lily.parcelhubcore.parcel.application.command.PrepareInCommand;
import com.lily.parcelhubcore.parcel.application.dto.prepareInDTO;
import com.lily.parcelhubcore.parcel.application.service.ParcelOpService;
import com.lily.parcelhubcore.parcel.domain.enums.ErrorCode;
import com.lily.parcelhubcore.shared.cache.CacheService;
import com.lily.parcelhubcore.parcel.domain.service.PackageBuilder;
import com.lily.parcelhubcore.parcel.domain.service.ParcelBaseService;
import com.lily.parcelhubcore.parcel.domain.service.PickupCodeService;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.repository.ParcelRepository;
import com.lily.parcelhubcore.parcel.shared.common.KeyConstants;
import com.lily.parcelhubcore.parcel.shared.util.CommonUtil;
import com.lily.parcelhubcore.shared.exception.BusinessException;
import com.lily.parcelhubcore.shared.lock.Lock;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class ParcelOpServiceImpl implements ParcelOpService {

    @Resource
    private Lock lock;

    @Resource
    private ParcelBaseService parcelBaseService;

    @Resource
    private CacheService cacheService;

    @Resource
    private PickupCodeService pickupCodeService;

    @Resource
    private ParcelRepository parcelRepository;

    @Resource
    private PackageBuilder packageBuilder;

    @Override
    public prepareInDTO prepareIn(PrepareInCommand bo) {
        // todo 参数格式校验
        var dto = new prepareInDTO();
        dto.setWaybillCode(bo.getWaybillCode());
        dto.setRecipientMobile(bo.getRecipientMobile());
        dto.setRecipientName(bo.getRecipientName());
        dto.setShelfCode(bo.getShelfCode());

        var stationCode = bo.getStationCode();
        var waybillCode = bo.getWaybillCode();

        // add lock
        var lockKey = KeyConstants.getWaybillCodeLock(waybillCode);
        try {
            if (!lock.tryLock(lockKey, LOCK_TIME, TimeUnit.MILLISECONDS)) {
                throw new BusinessException(ErrorCode.CURRENT_EXCEPTION);
            }

            // 查询是否已在任何站点入库
            parcelBaseService.waybillInBoundVerify(waybillCode);

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
        var stationCode = command.getStationCode();
        var waybillCode = command.getWaybillCode();
        // add lock
        var lockKey = KeyConstants.getWaybillCodeLock(waybillCode);
        try {
            if (!lock.tryLock(lockKey, LOCK_TIME, TimeUnit.MILLISECONDS)) {
                throw new BusinessException(ErrorCode.CURRENT_EXCEPTION);
            }
            // 查询是否已在任何站点入库
            parcelBaseService.waybillInBoundVerify(waybillCode);
            // 校验取件码是否有重复；
            pickupCodeService.pickupCodeExistVerify(stationCode, command.getPickupCode());
            // 查看包裹是首次入库还是多次入库
            var oldParcel = parcelRepository.findFirstByStationCodeAndWaybillCode(stationCode, waybillCode);
            // 构建数据库操作对象和消息体
            var packDTO = packageBuilder.buildInParcelPackDTO(command, oldParcel);
            // 事务内更新数据库，发送消息
            parcelBaseService.updateDBAndSendMsg(packDTO);
            // 删除该运单号的取件码缓存
            cacheService.delete(KeyConstants.getWaybillPickupCacheKey(stationCode, waybillCode));
        } finally {
            lock.unlock(lockKey);
        }
    }

    @Override
    public void outBound() {

    }

    @Override
    public void returned() {

    }

    @Override
    public void transfer() {

    }

    @Override
    public void inventory() {

    }
}
