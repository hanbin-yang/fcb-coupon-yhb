package com.fcb.coupon.common.enums;

import java.util.Objects;
import java.util.Optional;
/**
 * @author mashiqiong
 * @date 2021/08/19 20:23:00
 */
public enum BusinessTypeEnum {
    /**
     * 验证码类型枚举
     */
	COUPON_GIVE(0, "优惠券转赠"),
    COUPON_TRANSFER(1, "优惠券转让"),
    ;
    private Integer type;
    private String content;

    BusinessTypeEnum(Integer type, String content) {
        this.type = type;
        this.content = content;
    }

    public static Optional<BusinessTypeEnum> valueOf(Integer value) {
        if (value == null) {
            return Optional.empty();
        }
        for (BusinessTypeEnum captchasTypeEnum : BusinessTypeEnum.values()) {
            if (Objects.equals(captchasTypeEnum.type, value)) {
                return Optional.of(captchasTypeEnum);
            }
        }

        return Optional.empty();
    }

    public Integer getType() {
        return type;
    }
}
