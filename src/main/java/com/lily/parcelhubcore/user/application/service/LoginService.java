package com.lily.parcelhubcore.user.application.service;

public interface LoginService {

    String login(String userName, String password);

    void logout();
}
