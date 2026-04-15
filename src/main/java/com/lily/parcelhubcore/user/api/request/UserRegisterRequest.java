package com.lily.parcelhubcore.user.api.request;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;

@Data
public class UserRegisterRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 9088929886055761694L;

    private String username;

    private String password;

    private String stationCode;

    private String role;
}
