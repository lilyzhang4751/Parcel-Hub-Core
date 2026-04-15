package com.lily.parcelhubcore.user.application.service;

import com.lily.parcelhubcore.user.application.command.StationRegisterCommand;

public interface StationService {

    void register(StationRegisterCommand command);

}
