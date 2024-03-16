package com.fcb.coupon.common.enums;

public enum YesNoEnum {

    NO(0, "否"),
    YES(1, "是");

    private Integer value;

    private String desc;

    YesNoEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public Integer getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }
}
