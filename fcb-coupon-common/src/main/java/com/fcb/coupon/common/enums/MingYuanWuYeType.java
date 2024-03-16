package com.fcb.coupon.common.enums;

import java.util.Objects;

/**
 * @author YangHanBin
 * @date 2021-08-25 14:28
 */
public enum MingYuanWuYeType {
    HOUSE(0, "住宅"),
    APARTMENT(1, "公寓"),
    SHOP(2, "商铺"),
    OFFICE(3, "写字楼"),
    PARKING(4, "车位"),
    STORAGE(5, "储藏室"),
    ;

    private Integer type;
    private String desc;

    MingYuanWuYeType(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public static String getDescByType(Integer type) {
        Objects.requireNonNull(type);
        for (MingYuanWuYeType value : MingYuanWuYeType.values()) {
            if (value.getType().equals(type)) {
                return value.getDesc();
            }
        }
        return null;
    }
}

