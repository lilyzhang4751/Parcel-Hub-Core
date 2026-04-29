package com.lily.parcelhubcore.parcel.api.request;

import static com.lily.parcelhubcore.parcel.common.constants.Constants.MOBILE_REGEXP;
import static com.lily.parcelhubcore.parcel.common.constants.Constants.SHELF_CODE_REGEXP;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "包裹预处理请求")
public class PrepareInRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 6960634266531481436L;

    @NotBlank(message = "运单号不能为空")
    @Size(max = 20, message = "运单号长度不能超过20")
    private String waybillCode;

    @NotBlank(message = "货架号不能为空")
    @Pattern(regexp = SHELF_CODE_REGEXP, message = "货架号格式错误")
    private String shelfCode;

    @Size(max = 20, message = "姓名长度不能超过20")
    private String recipientName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = MOBILE_REGEXP, message = "手机号非法")
    private String recipientMobile;
}
