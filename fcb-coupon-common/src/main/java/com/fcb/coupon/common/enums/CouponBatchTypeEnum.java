package com.fcb.coupon.common.enums;

import java.util.Objects;
import java.util.stream.Stream;

public enum CouponBatchTypeEnum {

    LOG_TYPE_GENERATE_COUPON(1, "批量生券"),
    LOG_TYPE_SEND_COUPON(2, "批量发券"),
    LOG_TYPE_IMPORT_COUPON(3, "批量导入券码"),
    LOG_TYPE_EXPORT_COUPON_THEME(4, "导出券活动"),
    LOG_TYPE_EXPORT_COUPON(5, "导出优惠券明细"),
    LOG_TYPE_EXPORT_DONATE_COUPON(6, "导出赠送优惠券明细"),
    LOG_TYPE_EXPORT_ASSIGN_COUPON(7, "导出转让优惠券明细"),
    ;

    private final Integer type;

    private final String desc;

    CouponBatchTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public static CouponBatchTypeEnum of(Integer type) {
        Objects.requireNonNull(type);

        return Stream.of(values())
                .filter(bean -> bean.type.equals(type))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("error CouponBatchTypeEnum: type [" + type + "] 不存在"));
    }

    public static String getDescByType(Integer type) {
        if (type == null) {
            return null;
        }
        for (CouponBatchTypeEnum item : CouponBatchTypeEnum.values()) {
            if (item.getType().equals(type)) {
                return item.getDesc();
            }
        }
        return null;
    }
}
