package com.lily.parcelhubcore.parcel.application.service.impl;

import static com.lily.parcelhubcore.parcel.shared.enums.ErrorCode.PARCEL_NOT_EXIST;

import java.util.ArrayList;
import java.util.List;

import com.lily.parcelhubcore.parcel.api.response.ParcelInfoDTO;
import com.lily.parcelhubcore.parcel.application.query.ParcelQuery;
import com.lily.parcelhubcore.parcel.application.service.ParcelQueryService;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.ParcelDO;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.repository.ParcelRepository;
import com.lily.parcelhubcore.shared.exception.BusinessException;
import com.lily.parcelhubcore.shared.util.TimeConvertUtils;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class ParcelQueryServiceImpl implements ParcelQueryService {

    @Resource
    private ParcelRepository parcelRepository;

    @Override
    public List<ParcelInfoDTO> parcelQueryService(String stationCode, String pickupCode) {
        var parceList = parcelRepository.findByStationCodeAndPickupCode(stationCode, pickupCode);
        if (CollectionUtils.isEmpty(parceList)) {
            throw new BusinessException(PARCEL_NOT_EXIST);
        }
        return parceList.stream().map(this::toDTO).toList();
    }

    @Override
    public List<ParcelInfoDTO> queryAllParcel(ParcelQuery query) {
        Specification<ParcelDO> spec = (root, cq, cb) -> {
            var predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("stationCode"), query.getStationCode()));

            if (StringUtils.hasText(query.getShelfCode())) {
                predicates.add(cb.equal(root.get("shelfCode"), query.getShelfCode()));
            }

            if (StringUtils.hasText(query.getPickupCode())) {
                predicates.add(cb.equal(root.get("pickupCode"), query.getPickupCode()));
            }
            if (StringUtils.hasText(query.getWaybillCode())) {
                predicates.add(cb.equal(root.get("waybillCode"), query.getWaybillCode()));
            }
            if (StringUtils.hasText(query.getMobile())) {
                predicates.add(cb.equal(root.get("recipientMobile"), query.getMobile()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return parcelRepository.findAll(spec)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private ParcelInfoDTO toDTO(ParcelDO parcel) {
        var dto = new ParcelInfoDTO();
        BeanUtils.copyProperties(parcel, dto);
        dto.setLatestInboundTime(TimeConvertUtils.toEpochMilli(parcel.getLatestInboundTime()));
        dto.setLatestOutboundTime(TimeConvertUtils.toEpochMilli(parcel.getLatestOutboundTime()));
        return dto;
    }
}
