package com.fcb.coupon.common.enums;

/**
 * @author YangHanBin
 * @date 2021-08-24 10:35
 */
public enum OperateCouponEnum {
    LOCK("上锁"),
    UNLOCK("解锁"),
    REBIND("换绑"),
    VERIFY("核销"),
    ;
    private final String desc;

    OperateCouponEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
