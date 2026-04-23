package com.lily.parcelhubcore.user.application.command;

import lombok.Data;

@Data
public class UserRegisterCommand {

    private String username;

    private String password;

    private String stationCode;

    private String role;
}
