package com.lily.parcelhubcore.user.application.service.impl;

import static com.lily.parcelhubcore.parcel.common.enums.ErrorCode.STATION_NOT_EXIST;

import com.lily.parcelhubcore.shared.enums.StationStatusEnum;
import com.lily.parcelhubcore.shared.exception.BusinessException;
import com.lily.parcelhubcore.user.application.command.StationRegisterCommand;
import com.lily.parcelhubcore.user.application.service.MobileCryptoService;
import com.lily.parcelhubcore.user.application.service.StationService;
import com.lily.parcelhubcore.user.application.util.CodeGenerator;
import com.lily.parcelhubcore.user.infrastructure.persistence.entity.StationInfoDO;
import com.lily.parcelhubcore.user.infrastructure.persistence.repository.StationInfoRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StationServiceImpl implements StationService {

    @Resource
    private MobileCryptoService mobileCryptoService;

    @Resource
    private StationInfoRepository stationInfoRepository;

    @Override
    @Transactional
    public void register(StationRegisterCommand command) {
        var station = new StationInfoDO();
        station.setName(command.getName());
        station.setStatus(StationStatusEnum.OPERATION.getCode());
        station.setPrincipal(command.getPrincipal());
        station.setLongitude(command.getLongitude());
        station.setLatitude(command.getLatitude());
        station.setBusinessStartTime(command.getBusinessStartTime());
        station.setBusinessEndTime(command.getBusinessEndTime());
        // mobile encode
        station.setContactMobile(mobileCryptoService.encryptMobile(command.getContactMobile()));
        var id = stationInfoRepository.save(station).getId();
        station.setCode(CodeGenerator.buildStationCode(id));
        stationInfoRepository.save(station);
    }

    @Override
    public StationInfoDO queryByStationCode(String stationCode) {
        var station = stationInfoRepository.findByCode(stationCode);
        if (station == null) {
            throw new BusinessException(STATION_NOT_EXIST);
        }
        // mobile decode
        station.setContactMobile(mobileCryptoService.decryptMobile(station.getContactMobile()));
        return station;
    }

}
