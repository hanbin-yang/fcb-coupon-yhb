package com.fcb.coupon.app.exception;

import com.fcb.coupon.common.exception.ResponseErrorCode;

/**
 * @author YangHanBin
 * @date 2021-08-16 10:12
 */
public enum MediaCouponErrorCode implements ResponseErrorCode {
    COUPON_THEME_NOT_EXIST("270001", "券活动不存在"),
    USER_TYPE_NOT_ALLOW("270002", "该券不适用C端"),
    NOT_MEDIA_ADVERT_COUPON("270003", "券非媒体广告券"),
    COUPON_THEME_STATUS_NOT_ALLOW("270004", "优惠券状态为{0}，不可用"),
    COUPON_END("270005", "来晚啦，活动已结束"),
    ;

    private final String code;

    private final String message;


    MediaCouponErrorCode(String code, String message) {
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
}
