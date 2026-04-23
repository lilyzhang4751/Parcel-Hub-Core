package com.lily.parcelhubcore.user.application.service;

import com.lily.parcelhubcore.user.application.command.StationRegisterCommand;
import com.lily.parcelhubcore.user.infrastructure.persistence.entity.StationInfoDO;

public interface StationService {

    void register(StationRegisterCommand command);

    StationInfoDO queryByStationCode(String stationCode);

}
