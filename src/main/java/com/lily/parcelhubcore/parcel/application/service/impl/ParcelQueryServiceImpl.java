package com.lily.parcelhubcore.parcel.application.service.impl;

import static com.lily.parcelhubcore.parcel.common.enums.ErrorCode.PARCEL_NOT_EXIST;

import java.util.ArrayList;

import com.lily.parcelhubcore.parcel.api.response.ParcelBaseInfoDTO;
import com.lily.parcelhubcore.parcel.api.response.ParcelDetailDTO;
import com.lily.parcelhubcore.parcel.api.response.ParcelNotifyRecordDTO;
import com.lily.parcelhubcore.parcel.api.response.ParcelOpRecordDTO;
import com.lily.parcelhubcore.parcel.application.query.ParcelPageQuery;
import com.lily.parcelhubcore.parcel.application.service.ParcelQueryService;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.Parcel;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.ParcelNotifyRecord;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.entity.ParcelOpRecord;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.repository.ParcelNotifyRecordRepository;
import com.lily.parcelhubcore.parcel.infrastructure.persistence.repository.ParcelOpRecordRepository;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class ParcelQueryServiceImpl implements ParcelQueryService {

    @Resource
    private ParcelRepository parcelRepository;

    @Resource
    private ParcelOpRecordRepository parcelOpRecordRepository;

    @Resource
    private ParcelNotifyRecordRepository parcelNotifyRecordRepository;

    @Override
    public ParcelDetailDTO querySingleParcel(String stationCode, String waybillCode) {
        var parcel = parcelRepository.findByStationCodeAndWaybillCode(stationCode, waybillCode);
        if (parcel.isEmpty()) {
            throw new BusinessException(PARCEL_NOT_EXIST);
        }
        var detail = ParcelDetailDTO.builder().baseInfo(toParcelDTO(parcel.get())).build();
        var opRecordList = parcelOpRecordRepository.findByStationCodeAndWaybillCode(stationCode, waybillCode);
        if (!CollectionUtils.isEmpty(opRecordList)) {
            detail.setOpRecordList(opRecordList.stream().map(this::toParcelOpRecordDTO).toList());
        }
        var notifyRecordList = parcelNotifyRecordRepository.findByStationCodeAndWaybillCode(stationCode, waybillCode);
        if (!CollectionUtils.isEmpty(notifyRecordList)) {
            detail.setNotifyRecordList(notifyRecordList.stream().map(this::toParcelNotifyRecordDTO).toList());
        }
        return detail;
    }

    @Override
    public PageResponse<ParcelBaseInfoDTO> pageQuery(ParcelPageQuery query) {
        Specification<Parcel> spec = (root, cq, cb) -> {
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
        var recordList = page.getContent().stream().map(this::toParcelDTO).toList();

        return PageResponse.<ParcelBaseInfoDTO>builder().recordList(recordList).total(page.getTotalElements()).totalPage(page.getTotalPages()).pageNum(query.getPageNum()).pageSize(query.getPageSize()).build();
    }

    private ParcelBaseInfoDTO toParcelDTO(Parcel parcel) {
        var dto = new ParcelBaseInfoDTO();
        BeanUtils.copyProperties(parcel, dto);
        dto.setLatestInboundTime(TimeConvertUtils.toEpochMilli(parcel.getLatestInboundTime()));
        dto.setLatestOutboundTime(TimeConvertUtils.toEpochMilli(parcel.getLatestOutboundTime()));
        return dto;
    }

    private ParcelOpRecordDTO toParcelOpRecordDTO(ParcelOpRecord opRecord) {
        var dto = new ParcelOpRecordDTO();
        BeanUtils.copyProperties(opRecord, dto);
        dto.setOpTime(TimeConvertUtils.toEpochMilli(opRecord.getOpTime()));
        return dto;
    }

    private ParcelNotifyRecordDTO toParcelNotifyRecordDTO(ParcelNotifyRecord notifyRecord) {
        var dto = new ParcelNotifyRecordDTO();
        BeanUtils.copyProperties(notifyRecord, dto);
        dto.setNotifyTime(TimeConvertUtils.toEpochMilli(notifyRecord.getNotifyTime()));
        return dto;
    }

}
