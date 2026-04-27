package com.lily.parcelhubcore.user.application.service.impl;

import static com.lily.parcelhubcore.parcel.common.enums.ErrorCode.STATION_NOT_EXIST;
import static com.lily.parcelhubcore.user.common.ErrorCode.MOBILE_DUPLICATE;

import com.lily.parcelhubcore.shared.enums.StationStatusEnum;
import com.lily.parcelhubcore.shared.enums.UserRoleEnum;
import com.lily.parcelhubcore.shared.exception.BusinessException;
import com.lily.parcelhubcore.user.application.command.StationRegisterCommand;
import com.lily.parcelhubcore.user.application.command.UserRegisterCommand;
import com.lily.parcelhubcore.user.application.service.LoginService;
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

    @Resource
    private LoginService loginService;

    @Override
    @Transactional
    public String register(StationRegisterCommand command) {
        // 查询手机号是否重复
        var mobileHash = mobileCryptoService.hash(command.getContactMobile());
        if (stationInfoRepository.existsByMobileHash(mobileHash)) {
            throw new BusinessException(MOBILE_DUPLICATE);
        }

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
        station.setMobileHash(mobileHash);
        var id = stationInfoRepository.save(station).getId();
        var stationCode = CodeGenerator.buildStationCode(id);
        station.setCode(stationCode);
        stationInfoRepository.save(station);
        // 创建该手机号的MANAGER user
        var userRegisterCommand = new UserRegisterCommand();
        userRegisterCommand.setUsername(command.getPrincipal());
        userRegisterCommand.setPassword(command.getPassword());
        userRegisterCommand.setStationCode(stationCode);
        userRegisterCommand.setRole(UserRoleEnum.MANAGER.getRole());
        loginService.register(userRegisterCommand);
        return stationCode;
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
