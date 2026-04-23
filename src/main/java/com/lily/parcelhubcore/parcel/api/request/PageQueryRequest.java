package com.lily.parcelhubcore.parcel.api.request;

import static com.lily.parcelhubcore.parcel.common.constants.Constants.MOBILE_REGEXP;
import static com.lily.parcelhubcore.parcel.common.constants.Constants.PICKUP_CODE_REGEXP;
import static com.lily.parcelhubcore.parcel.common.constants.Constants.SHELF_CODE_REGEXP;

import com.lily.parcelhubcore.shared.enums.NotifyStatusEnum;
import com.lily.parcelhubcore.shared.enums.WaybillStatusEnum;
import com.lily.parcelhubcore.shared.validate.annotation.EnumIntCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
@Schema(description = "分页查询请求")
public class PageQueryRequest {

    @Pattern(regexp = SHELF_CODE_REGEXP, message = "货架号格式错误")
    private String shelfCode;

    @Pattern(regexp = PICKUP_CODE_REGEXP, message = "取件码格式错误")
    private String pickupCode;

    @Pattern(regexp = MOBILE_REGEXP, message = "手机号非法")
    private String mobile;

    @EnumIntCode(enumClass = WaybillStatusEnum.class, message = "运单状态不合法")
    private Integer waybillStatus;

    @EnumIntCode(enumClass = NotifyStatusEnum.class, message = "通知状态不合法")
    private Integer notifyStatus;

    @NotNull(message = " 页码不能为空")
    @Positive
    private Integer pageNum;

    @NotNull(message = " pageSize不能为空")
    @Min(1)
    @Max(100)
    private Integer pageSize;

    @AssertTrue(message = "shelfCode、pickupCode、mobile、waybillStatus、notifyStatus 不能同时为空")
    public boolean isAtLeastOneConditionProvided() {
        return StringUtils.hasText(shelfCode)
                || StringUtils.hasText(pickupCode)
                || StringUtils.hasText(mobile)
                || waybillStatus != null
                || notifyStatus != null;
    }
}
