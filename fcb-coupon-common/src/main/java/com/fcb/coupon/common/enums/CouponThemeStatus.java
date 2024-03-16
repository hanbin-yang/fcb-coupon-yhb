package com.fcb.coupon.common.enums;

import java.util.stream.Stream;

/**
 * @author YangHanBin
 * @date 2021-06-16 17:50
 */
public enum CouponThemeStatus {
    CREATE(0,"待提交"),
    AWAITING_APPROVAL(1,"待审核"),
    APPROVED(2,"未开始"),
    UN_APPROVE(3,"审核未通过"),
    EFFECTIVE(4,"进行中"),
    INEFFECTIVE(5,"已失效"),
    CLOSED(6,"已关闭"),
    ;
    private final Integer status;
    private final String desc;

    CouponThemeStatus(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    public static CouponThemeStatus of(Integer status) {

        return Stream.of(values())
                .filter(bean -> bean.status.equals(status))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("CouponThemeStatus: [type=" + status + "]不存在！"))
                ;
    }
}
