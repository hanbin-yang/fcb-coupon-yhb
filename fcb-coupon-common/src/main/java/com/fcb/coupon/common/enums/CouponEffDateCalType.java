package com.fcb.coupon.common.enums;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author YangHanBin
 * @date 2021-06-15 16:28
 */
public enum CouponEffDateCalType {
    FIXED(1, "固定有效期"),
    DAYS(2, "从领用开始计算"),
    ;
    private final Integer type;

    private final String desc;

    CouponEffDateCalType(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public static CouponEffDateCalType of(Integer type) {
        Objects.requireNonNull(type);

        return Stream.of(values())
                .filter(bean -> bean.type.equals(type))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("error EffDateCalType: type [" + type + "] 不存在"));
    }
}
