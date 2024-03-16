package com.fcb.coupon.app.exception;

import com.fcb.coupon.common.exception.ResponseErrorCode;

/**
 * 优惠券操作异常码
 *
 * @Author WeiHaiQi
 * @Date 2021-06-19 9:59
 **/
public enum CouponErrorCode implements ResponseErrorCode {

    COUPON_NOT_FOUND("333001", "优惠券不存在"),
    COUPON_NOT_CAN_USE("333002", "优惠券不是可使用状态"),
    UPDATE_COUPON_INVALID_EXCEPTION("333002", "作废优惠券异常."),
    UPDATE_COUPON_FREEZE_EXCEPTION("333003", "冻结/解冻优惠券异常."),
    UPDATE_COUPON_FREEZE_STATUS_NOT_ALLOWED("333004", "非已发行/可使用的优惠券不能冻结."),
    UPDATE_COUPON_POSTPONE_USED_NOT_ALLOWED("333006", "已使用的优惠券不能延期."),
    UPDATE_COUPON_POSTPONE_EXCEPTION("333007", "延期优惠券异常."),
    REFRESH_ES_COUPON_EXCEPTION("333008", "延期优惠券异常."),
    QUERY_COUPON_USERID_NULL_EXCEPTION("311001", "用户id不能为空"),
    QUERY_COUPON_USER_NONE_EXCEPTION("311002", "未查询到unionId对应的用户"),
    QUERY_EXCEPTION("311003", "查询异常"),
    QUERY_COUPON_PARAM_NULL_EXCEPTION("311004", "必传参数不能为空"),

    QUERY_COUPON_PARAMS_ERROR("311005","参数错误"),
    COUPON_IDS_REQUIRED("220023", "优惠券主键ids必须填写"),
    ;

    private final String code;
    private final String message;

    CouponErrorCode(String code, String message) {
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
