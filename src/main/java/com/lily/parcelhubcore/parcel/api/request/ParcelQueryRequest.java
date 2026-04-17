package com.lily.parcelhubcore.parcel.api.request;

import java.io.Serial;
import java.io.Serializable;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ParcelQueryRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -337620606624422202L;

    @Size(max = 20, message = "运单号长度不能超过20")
    private String waybillCode;

    @Pattern(regexp = "^\\d{1,2}-\\d{2}$", message = "货架号格式错误")
    private String shelfCode;

    @Pattern(regexp = "^\\d{1,2}-\\d{1,2}-\\d{1,3}$", message = "取件码格式错误")
    private String pickupCode;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号非法")
    private String mobile;
}
