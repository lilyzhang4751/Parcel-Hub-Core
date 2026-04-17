package com.lily.parcelhubcore.parcel.api.request;

import java.io.Serial;
import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PrepareInRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 6960634266531481436L;

    @NotBlank(message = "运单号不能为空")
    @Size(max = 20, message = "运单号长度不能超过20")
    private String waybillCode;

    @NotBlank(message = "货架号不能为空")
    @Pattern(regexp = "^\\d{1,2}-\\d{2}$", message = "货架号格式错误")
    private String shelfCode;

    @Size(max = 20, message = "姓名长度不能超过20")
    private String recipientName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号非法")
    private String recipientMobile;
}
