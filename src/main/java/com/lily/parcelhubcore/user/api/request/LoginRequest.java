package com.lily.parcelhubcore.user.api.request;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;

@Data
public class LoginRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 5748440666533183084L;

    private String username;

    private String password;
}
