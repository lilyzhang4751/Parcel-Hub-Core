package com.lily.parcelhubcore.parcel.application.service.impl;

import static com.lily.parcelhubcore.parcel.shared.enums.ErrorCode.PARCEL_NOT_EXIST;

import java.util.ArrayList;

import com.lily.parcelhubcore.parcel.api.response.ParcelInfoDTO;
import com.lily.parcelhubcore.parcel.application.query.ParcelPageQuery;
import com.lily.parcelhubcore.parcel.application.service.ParcelQueryService;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.ParcelDO;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.repository.ParcelRepository;
import com.lily.parcelhubcore.shared.exception.BusinessException;
import com.lily.parcelhubcore.shared.response.PageResponse;
import com.lily.parcelhubcore.shared.util.TimeConvertUtils;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ParcelQueryServiceImpl implements ParcelQueryService {

    @Resource
    private ParcelRepository parcelRepository;

    @Override
    public ParcelInfoDTO querySingleParcel(String stationCode, String waybillCode) {
        var parcel = parcelRepository.findFirstByStationCodeAndWaybillCode(stationCode, waybillCode);
        if (parcel == null) {
            throw new BusinessException(PARCEL_NOT_EXIST);
        }
        return toDTO(parcel);
    }

    @Override
    public PageResponse<ParcelInfoDTO> pageQuery(ParcelPageQuery query) {
        Specification<ParcelDO> spec = (root, cq, cb) -> {
            var predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("stationCode"), query.getStationCode()));

            if (StringUtils.hasText(query.getShelfCode())) {
                predicates.add(cb.equal(root.get("shelfCode"), query.getShelfCode()));
            }

            if (StringUtils.hasText(query.getPickupCode())) {
                predicates.add(cb.equal(root.get("pickupCode"), query.getPickupCode()));
            }

            if (StringUtils.hasText(query.getMobile())) {
                predicates.add(cb.equal(root.get("recipientMobile"), query.getMobile()));
            }

            if (query.getWaybillStatus() != null) {
                predicates.add(cb.equal(root.get("status"), query.getWaybillStatus()));
            }

            if (query.getNotifyStatus() != null) {
                predicates.add(cb.equal(root.get("notifyStatus"), query.getNotifyStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
        var pageable = PageRequest.of(query.getPageNum() - 1, query.getPageSize(), Sort.by(Sort.Direction.DESC, "latestInboundTime"));
        var page = parcelRepository.findAll(spec, pageable);
        var recordList = page.getContent().stream().map(this::toDTO).toList();

        return PageResponse.<ParcelInfoDTO>builder()
                .recordList(recordList)
                .total(page.getTotalElements())
                .totalPage(page.getTotalPages())
                .pageNum(query.getPageNum())
                .pageSize(query.getPageSize()).build();
    }

    private ParcelInfoDTO toDTO(ParcelDO parcel) {
        var dto = new ParcelInfoDTO();
        BeanUtils.copyProperties(parcel, dto);
        dto.setLatestInboundTime(TimeConvertUtils.toEpochMilli(parcel.getLatestInboundTime()));
        dto.setLatestOutboundTime(TimeConvertUtils.toEpochMilli(parcel.getLatestOutboundTime()));
        return dto;
    }
}
