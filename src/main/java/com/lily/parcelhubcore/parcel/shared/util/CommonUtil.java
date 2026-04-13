package com.lily.parcelhubcore.parcel.shared.util;

import org.springframework.util.StringUtils;

public class CommonUtil {

    /**
     * 根据取件码获取货架号
     *
     * @param pickupCode 取件码
     * @return 货架号
     */
    public static String getShelfCodeFromPickupCode(String pickupCode) {
        if (pickupCode == null || pickupCode.isEmpty()) {
            return pickupCode;
        }

        int firstDashIndex = pickupCode.indexOf('-');
        if (firstDashIndex == -1) {
            return pickupCode;
        }

        int secondDashIndex = pickupCode.indexOf('-', firstDashIndex + 1);
        if (secondDashIndex == -1) {
            return pickupCode;
        }

        // 返回第二个横杠之前的所有字符（不包含第二个横杠）
        return pickupCode.substring(0, secondDashIndex);
    }

    public static boolean pickupCodeMachShelfCode(String pickupCode, String targetShelfCode) {
        if (!StringUtils.hasText(pickupCode) || !StringUtils.hasText(targetShelfCode)) {
            return false;
        }
        var realShelfCode = getShelfCodeFromPickupCode(pickupCode);
        return targetShelfCode.equals(realShelfCode);
    }

}
