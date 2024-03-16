package com.fcb.coupon.common.enums;

/**
 * 优惠券通知事件
 */
public enum NotifyEventEnum {
    /**
     * B端用户发券成功时，触发通知事件
     */
    @Deprecated
    coupon_issue_send("coupon_issue_send"),
    /**
     * B端用户主动营销发券成功时，触发通知事件
     */
    coupon_issue_active("coupon_issue_active"),

    /**
     * C端用户发券成功时，触发通知事件
     */
    @Deprecated
    c_coupon_issue_send("c_coupon_issue_send"),
    /**
     * C端用户主动营销发券成功时，触发通知事件
     */
    c_coupon_issue_active("c_coupon_issue_active"),
    /**
     * C端用户优惠券即将过期提醒通知事件
     */
    c_coupon_overdue("c_coupon_overdue"),

    //++++++++++++++++++++++ 用户发券成功时，触发通知事件 重新定义模板++++++++++++++++++++//
    /**
     * C端 金额券 用户发券成功时，触发通知事件
     */
    c_coupon_issue_send_value("c_coupon_issue_send_value"),
    /**
     * C端 折扣券 用户发券成功时，触发通知事件
     */
    c_coupon_issue_send_discount("c_coupon_issue_send_discount"),
    /**
     * C端 自动生成券码的福利卡 用户发券成功时，触发通知事件
     */
    c_coupon_issue_send_auto_code_welfare_card("c_coupon_issue_send_auto_code_welfare_card"),
    /**
     * C端 红包券 用户发券成功时，触发通知事件
     */
    c_coupon_issue_send_hongbao("c_coupon_issue_send_hongbao"),
    /**
     * C端 第三方券码 用户发券成功时，触发通知事件
     */
    c_coupon_issue_send_third_code("c_coupon_issue_send_third_code"),

    /**
     * B端 金额券 用户发券成功时，触发通知事件
     */
    b_coupon_issue_send_value("coupon_issue_send_value"),
    /**
     * B端 折扣券 用户发券成功时，触发通知事件
     */
    b_coupon_issue_send_discount("coupon_issue_send_discount"),
    /**
     * B端 自动生成券码的福利卡 用户发券成功时，触发通知事件
     */
    b_coupon_issue_send_auto_code_welfare_card("coupon_issue_send_auto_code_welfare_card"),
    /**
     * B端 红包券 用户发券成功时，触发通知事件
     */
    b_coupon_issue_send_hongbao("coupon_issue_send_hongbao"),
    /**
     * B端 第三方券码 用户发券成功时，触发通知事件
     */
    b_coupon_issue_send_third_code("coupon_issue_send_third_code"),

    /**
     * 媒体广告领券事件码
     */
    C_COUPON_AD_RECEIVE("c_coupon_ad_receive"),
    ;

    private String code;

    NotifyEventEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}