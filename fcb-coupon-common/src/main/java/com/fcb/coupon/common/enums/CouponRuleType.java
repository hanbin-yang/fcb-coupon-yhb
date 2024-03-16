package com.fcb.coupon.common.enums;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author YangHanBin
 * @date 2021-06-15 15:46
 */
public enum CouponRuleType {
    VALID_PERIOD_FIXED(1, "券固定有效期规则"),
    VALID_PERIOD_DAYS(2, "券有效期规则"),
    DISCOUNT(4, "券折扣"),
    AMOUNT(5, "券金额"),
    IF_CAN_GIVEN(11, "是否可赠送"),
    IF_CAN_TRANSFER(12, "是否可转让"),
    ;
    private final Integer type;

    private final String desc;

    CouponRuleType(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public static CouponRuleType of(Integer type) {
        Objects.requireNonNull(type);

        return Stream.of(values())
                .filter(bean -> bean.type.equals(type))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("error CouponRuleType: type [" + type + "] 不存在"));
    }
}
