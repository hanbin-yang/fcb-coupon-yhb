package com.fcb.coupon.common.enums;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author mashiqiong
 * @date 2021-6-17 19:47
 */
public enum CouponTypeEnum {
    COUPON_TYPE_VIRTUAL(0,"电子券"),
    COUPON_TYPE_REAL(1,"实体券"),
    COUPON_TYPE_REDENVELOPE(2,"红包券"),
    COUPON_TYPE_THIRD(3,"第三方券码"),
    ;
    private Integer type;
    private String typeStr;

    CouponTypeEnum(Integer type, String typeStr) {
        this.type = type;
        this.typeStr = typeStr;
    }

    public Integer getType() {
        return type;
    }

    public String getTypeStr() {
        return typeStr;
    }


    public static String getNameByType(Integer type) {
        if (type != null) {
            for (CouponTypeEnum couponTypeEnum : CouponTypeEnum.values()) {
                if (couponTypeEnum.type.equals(type)) {
                    return couponTypeEnum.getTypeStr();
                }
            }
        }
        return null;
    }

    public static CouponTypeEnum of(Integer type) {
        Objects.requireNonNull(type);
        return Stream.of(values())
                .filter(bean -> bean.type.equals(type))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("CouponTypeEnum: [type=" + type + "]不存在！"))
                ;
    }
}
