package com.fcb.coupon.common.enums;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author YangHanBin
 * @date 2021-06-15 15:29
 */
public enum CouponDiscountType {
    CASH(0, "金额券"),
    DISCOUNT(1, "折扣券"),
    WELFARE_CARD(11, "福利卡"),
    RED_ENVELOP(12, "红包券"),
    ;
    private final Integer type;

    private final String desc;

    CouponDiscountType(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public static CouponDiscountType of(Integer type) {
        Objects.requireNonNull(type);

        return Stream.of(values())
                .filter(bean -> bean.type.equals(type))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("error CouponDiscountType: type [" + type + "] 不存在"));
    }

    public static String getNameByType(Integer type) {
        if (type != null) {
            for (CouponDiscountType couponDiscountType : CouponDiscountType.values()) {
                if (couponDiscountType.type.equals(type)) {
                    return couponDiscountType.getDesc();
                }
            }
        }
        return null;
    }
}
