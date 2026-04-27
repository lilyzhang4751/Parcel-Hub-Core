package com.lily.parcelhubcore.user.api.request;

import java.io.Serial;
import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRegisterRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 9088929886055761694L;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "站点编码不能为空")
    private String stationCode;

    @NotBlank(message = "角色不能为空")
    private String role;
}
