package com.fcb.coupon.app.exception;

import com.fcb.coupon.common.exception.ResponseErrorCode;

/**
 * @author YangHanBin
 * @date 2021-07-29 10:15
 */
public enum CouponBeforeGiveErrorCode implements ResponseErrorCode {
    ID_NULL("240001", "劵赠送前记录id不能未空"),
    RECORD_NOT_EXIST("240002", "劵赠送前记录不存在"),
    GIVETYPE_NOT_EXIST("240003", "赠送类型giveType值不在有效范围【1短信赠送,2面对面赠送,3微信朋友圈分享】"),
    CLIENTTYPE_NOT_EXIST("240004", "用户端类型clientType值不在有效范围【\"B_USER\", \"B端用户\";\"C_USER\", \"C端用户;\"J_USER\", \"机构用户\"】"),
    SMS_REACH_THE_UPPER_LIMIT("240005", "短信转赠次数已达上限"),
    COUPON_ID_NOT_EMPTY("240006", "参数couponId不能为空"),
    COUPON_NOT_EXIST("240007", "该优惠券不存在或已转赠"),
    COUPON_CAN_NOT_GIVE("240008", "该优惠券不允许转赠"),
    USER_LOGIN_INVALID("M4030", "用户登录失效，请重新登录"),
    COUPON_GIVE_CAN_NOT_GIVE_SELF("M4031", "优惠券不能转赠给自己"),
    COUPON_GIVE_MOBILE_CAN_NOT_EMPTY("M4031", "短信赠送，接收手机号不能为空"),
    COUPON_GIVE_USER_B_NOT_FIND("240009", "用户信息未找到"),
    ;

    private final String code;

    private final String message;

    CouponBeforeGiveErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return null;
    }

    @Override
    public String getMessage() {
        return null;
    }
}
