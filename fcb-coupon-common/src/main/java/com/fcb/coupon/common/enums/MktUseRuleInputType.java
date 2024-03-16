package com.fcb.coupon.common.enums;

import java.util.stream.Stream;

/**
 * @author HanBin_Yang
 * @since 2021/6/21 10:34
 */
@Deprecated
public enum MktUseRuleInputType {
    STORE(2, "店铺"),
    MERCHANT(1, "商家"),
    GROUP(3, "集团"),
    ;

    private final Integer type;
    private final String desc;

    MktUseRuleInputType(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public static MktUseRuleInputType of(Integer type) {

        return Stream.of(values())
                .filter(bean -> bean.type.equals(type))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("LogOprType: [type=" + type + "]不存在！"))
                ;
    }
}
