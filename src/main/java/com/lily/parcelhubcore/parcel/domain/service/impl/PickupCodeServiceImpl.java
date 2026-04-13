package com.lily.parcelhubcore.parcel.domain.service.impl;

import static com.lily.parcelhubcore.parcel.shared.common.Constants.LOCK_TIME;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.lily.parcelhubcore.parcel.domain.enums.ErrorCode;
import com.lily.parcelhubcore.parcel.domain.service.PickupCodeService;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.ParcelDO;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.repository.ParcelRepository;
import com.lily.parcelhubcore.parcel.shared.common.KeyConstants;
import com.lily.parcelhubcore.shared.exception.BusinessException;
import com.lily.parcelhubcore.shared.lock.Lock;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.BatchOptions;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RScoredSortedSetAsync;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class PickupCodeServiceImpl implements PickupCodeService {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private ParcelRepository parcelRepository;

    @Resource
    private Lock lock;

    @Override
    public String genarate(String stationCode, String shelfCode) {
        // 查询号码池缓存是否存在
        var poolKey = KeyConstants.getPickupCodePoolKey(stationCode, shelfCode);
        RScoredSortedSet<String> zset = redissonClient.getScoredSortedSet(poolKey);
        var code = zset.pollFirst();
        if (StringUtils.isNotBlank(code)) {
            return code;
        }
        // 号吗池不存在：创建号码池，然后再取
        return genaratePoolAndGet(stationCode, shelfCode, poolKey);

    }

    @Override
    public void pickupCodeExistVerify(String stationCode, String pickupCode) {
        var exist = parcelRepository.existsByStationCodeAndPickupCode(stationCode, pickupCode);
        if (exist) {
            throw new BusinessException(ErrorCode.PICKUP_OCCUPIED);
        }
    }

    private String genaratePoolAndGet(String stationCode, String shelfCode, String poolKey) {
        // 加锁，避免并发生成号码池
        var lockKey = KeyConstants.getStationShelfLock(stationCode, shelfCode);
        try {
            if (!lock.tryLock(lockKey, LOCK_TIME, TimeUnit.MILLISECONDS)) {
                throw new BusinessException(ErrorCode.CURRENT_EXCEPTION);
            }
            // 加锁后需再次检查是否为空
            RScoredSortedSet<String> zset = redissonClient.getScoredSortedSet(poolKey);
            var code = zset.pollFirst();
            if (StringUtils.isNotBlank(code)) {
                return code;
            }

            // 1 查询包裹表，获取还在库使用的取件码
            var currentList = parcelRepository.findByStationCodeAndShelfCode(stationCode, shelfCode);
            var pickupCodeList = currentList.stream().map(ParcelDO::getPickupCode).toList();
            // 2 生成1-999共1k个号码池,剔除在库取件码
            Map<String, Double> scoreMap = new HashMap<>(1000);
            for (int i = 1; i < 1000; i++) {
                scoreMap.put(shelfCode + "-" + i, (double) i);
            }
            scoreMap.entrySet().removeIf(entry -> pickupCodeList.contains(entry.getKey()));
            if (CollectionUtils.isEmpty(scoreMap)) {
                throw new BusinessException(ErrorCode.NO_AVAILABLE_PICKUP);
            }
            // 3 获取当前最小值
            var minAndRest = extractMinAndBuildRest(scoreMap);
            // 4 剩余号码放入zset，有效期7天
            var batch = redissonClient.createBatch(BatchOptions.defaults());
            RScoredSortedSetAsync<String> newZset = batch.getScoredSortedSet(poolKey);
            newZset.addAllAsync(minAndRest.rest());
            newZset.expireAsync(Duration.ofDays(7));
            batch.execute();
            return minAndRest.minMember();
        } finally {
            lock.unlock(lockKey);
        }
    }

    private <V> MinAndRest<V> extractMinAndBuildRest(Map<V, Double> scoreMap) {
        if (scoreMap == null || scoreMap.isEmpty()) {
            return null;
        }

        Map.Entry<V, Double> minEntry = null;
        for (Map.Entry<V, Double> entry : scoreMap.entrySet()) {
            if (minEntry == null || entry.getValue() < minEntry.getValue()) {
                minEntry = entry;
            }
        }

        Map<V, Double> rest = new HashMap<>(scoreMap);
        rest.remove(minEntry.getKey());

        return new MinAndRest<>(minEntry.getKey(), minEntry.getValue(), rest);
    }

    public record MinAndRest<V>(V minMember, Double minScore, Map<V, Double> rest) {
    }
}


