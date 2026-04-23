package com.lily.parcelhubcore.user.application.service;

import com.lily.parcelhubcore.user.application.command.UserRegisterCommand;

public interface LoginService {

    void register(UserRegisterCommand command);

    String login(String username, String password);

    void logout();
}
