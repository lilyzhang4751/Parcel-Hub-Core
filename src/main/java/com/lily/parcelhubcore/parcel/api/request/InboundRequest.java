package com.lily.parcelhubcore.parcel.api.request;

import static com.lily.parcelhubcore.parcel.common.constants.Constants.MOBILE_REGEXP;
import static com.lily.parcelhubcore.parcel.common.constants.Constants.PICKUP_CODE_REGEXP;
import static com.lily.parcelhubcore.parcel.common.constants.Constants.SHELF_CODE_REGEXP;

import java.io.Serial;
import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InboundRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 4198926573248529916L;

    @NotBlank(message = "运单号不能为空")
    @Size(max = 20, message = "运单号长度不能超过20")
    private String waybillCode;

    @NotBlank(message = "货架号不能为空")
    @Pattern(regexp = SHELF_CODE_REGEXP, message = "货架号格式错误")
    private String shelfCode;

    @NotBlank(message = "取件码不能为空")
    @Pattern(regexp = PICKUP_CODE_REGEXP, message = "取件码格式错误")
    private String pickupCode;

    @Size(max = 20, message = "姓名长度不能超过20")
    private String recipientName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = MOBILE_REGEXP, message = "手机号非法")
    private String recipientMobile;
}
