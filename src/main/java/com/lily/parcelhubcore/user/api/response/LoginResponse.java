package com.lily.parcelhubcore.user.api.response;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;

@Data
public class LoginResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = -5824194365333926504L;

    private String token;
}
