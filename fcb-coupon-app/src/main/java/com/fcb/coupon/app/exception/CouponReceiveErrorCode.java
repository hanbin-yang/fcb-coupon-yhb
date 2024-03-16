package com.fcb.coupon.app.exception;

import com.fcb.coupon.common.exception.ResponseErrorCode;

/**
 * @author YangHanBin
 * @date 2021-08-16 9:38
 */
public enum CouponReceiveErrorCode implements ResponseErrorCode {
    USER_TYPE_ILLEGAL("250000", "用户类型非法"),
    INVENTORY_SHORTAGE("250001", "库存不足"),
    NOT_SUPPORT_C("250002", "不支持C端用户领券"),
    NOT_SUPPORT_B("250003", "不支持B端用户领券"),
    NOT_SUPPORT_SAAS("250004", "不支持SAAS端用户领券"),
    OUT_OF_RATE_LIMIT("250005", "活动太火爆了，请稍后重试"),
    COUPON_THEME_NOT_EXIST("250006", "券活动不存在"),
    COUPON_THEME_NOT_EFFECTIVE("250007", "券活动非进行中"),
    COUPON_THEME_NOT_START("250008", "券活动未开始"),
    COUPON_THEME_ENDED("250008", "券活动已结束"),
    OUT_OF_STOCK("250009", "库存不足"),
    SOURCE_ILLEGAL("250010", "发券类型非法"),
    COUPON_TYPE_ILLEGAL("250011", "券码生成方式非法"),
    RECEIVE_UPDATE_ERROR("250012", "领券行锁校验失败"),
    COUPON_GIVE_RULE_ILLEGAL("250013", "该券非[%s],非法"),
    SYSTEM_BUSY_LIMIT("M4014", "您的操作太频繁，请稍后再试"),
    ;

    private final String code;

    private String message;


    CouponReceiveErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
