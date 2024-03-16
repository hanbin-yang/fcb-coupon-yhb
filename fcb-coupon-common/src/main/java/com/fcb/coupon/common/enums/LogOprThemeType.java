package com.fcb.coupon.common.enums;

import java.util.stream.Stream;

/**
 * 操作类型 1 券 2 券活动
 * @author YangHanBin
 * @date 2021-06-16 18:48
 */
public enum LogOprThemeType {
    COUPON(1, "券"),
    COUPON_THEME(2, "券活动"),
    ;

    private final Integer type;
    private final String desc;

    LogOprThemeType(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public LogOprThemeType of(Integer type) {

        return Stream.of(values())
                .filter(bean -> bean.type.equals(type))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("LogOprThemeType: [type=" + type + "]不存在！"))
                ;
    }
}
