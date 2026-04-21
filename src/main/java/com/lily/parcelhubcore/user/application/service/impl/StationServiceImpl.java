package com.lily.parcelhubcore.user.application.service.impl;

import com.lily.parcelhubcore.shared.enums.StationStatusEnum;
import com.lily.parcelhubcore.user.application.command.StationRegisterCommand;
import com.lily.parcelhubcore.user.application.service.StationService;
import com.lily.parcelhubcore.user.application.util.CodeGenerator;
import com.lily.parcelhubcore.user.infrastructure.persistence.entity.StationInfoDO;
import com.lily.parcelhubcore.user.infrastructure.persistence.repository.StationInfoRepository;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StationServiceImpl implements StationService {

    @Resource
    private StationInfoRepository stationInfoRepository;

    @Override
    @Transactional
    public void register(StationRegisterCommand command) {
        var station = new StationInfoDO();
        station.setName(command.getName());
        station.setStatus(StationStatusEnum.OPERATION.getCode());
        station.setPrincipal(command.getPrincipal());
        var encoder = new BCryptPasswordEncoder();
        station.setContactMobile(encoder.encode(command.getContactMobile()));
        station.setLongitude(command.getLongitude());
        station.setLatitude(command.getLatitude());
        station.setBusinessStartTime(command.getBusinessStartTime());
        station.setBusinessEndTime(command.getBusinessEndTime());
        var id = stationInfoRepository.save(station).getId();
        station.setCode(CodeGenerator.buildStationCode(id));
        stationInfoRepository.save(station);
    }

}
