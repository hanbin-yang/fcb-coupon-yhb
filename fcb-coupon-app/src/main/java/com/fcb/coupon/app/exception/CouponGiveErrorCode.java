package com.fcb.coupon.app.exception;

import com.fcb.coupon.common.exception.ResponseErrorCode;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月26日 11:37:00
 */
public enum CouponGiveErrorCode implements ResponseErrorCode {

    GIVE_LOGIN_INFO_ERROR("300004", "用户信息异常"),
    GIVE_CODE_EMPTY_ERROR("300005", "转赠记录编码不能为空"),
    GIVE_CODE_FORMAT_ERROR("300006", "转赠记录编码不合法"),
    GIVE_COUPON_BUSY_ERROR("300007", "您的操作太频繁，请稍后再试"),
    GIVE_NOT_FOUND_ERROR("300008", "转赠信息不存在"),
    GIVE_EXPIRE_ERROR("300009", "来晚了，领取已失效"),
    GIVE_COUPON_NOT_FOUND_ERROR("300010", "转赠的优惠券不存在"),
    GIVE_COUPON_EXPIRE_ERROR("300011", "优惠券已失效"),
    GIVE_NOT_REPEAT_RECEIVED_ERROR("300012", "您已经领取过了"),
    GIVE_OTHER_RECEIVED_ERROR("300013", "来晚了，优惠券已被领取"),
    GIVE_COUPON_CHANGE_ERROR("300014", "当前优惠券状态发生变化，无法领取"),
    GIVE_RECEIVE_USER_ERROR("300015", "用户不存在"),
    GIVE_RECEIVE_EXCEPTION_ERROR("300016", "领取失败，请重试"),
    GIVE_COUPON_ID_NULL_ERROR("300018", "劵赠送前记录id不能未空"),
    GIVE_COUPON_NOT_FIND_ERROR("300019", "劵赠送前记录不存在"),

    ;

    private final String code;
    private final String message;

    CouponGiveErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
