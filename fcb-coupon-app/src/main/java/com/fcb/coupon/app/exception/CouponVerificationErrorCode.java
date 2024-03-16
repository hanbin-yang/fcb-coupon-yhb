package com.fcb.coupon.app.exception;

import com.fcb.coupon.common.exception.ResponseErrorCode;

import java.util.stream.Stream;

/**
 * @author HanBin_Yang
 * @since 2021/6/23 9:33
 */
public enum CouponVerificationErrorCode implements ResponseErrorCode {
    COUPON_CODE_NOT_EXIST("250001", "验证不通过，券码 [%s] 不存在"),
    COUPON_CODE_USED("250002", "重复核销，券码已使用！"),
    PHONE_NOT_MATCH_COUPON("250003", "券码与绑定手机号不一致"),
    EFFECT_DATA_NOT_START("250004", "验证不通过，优惠券有效期还没有开始"),
    EFFECT_DATA_ENDED("250005", "验证不通过，优惠券已过期"),
    CROWD_SCOPE_NOT_MATCH("250006", "[%s]不适用该券"),
    IS_NO_PHONE_PATTERN("250007", "手机号格式不正确"),
    IS_NO_AGENCY("250008", "机构经纪人账号不正确"),
    MEMBER_NOT_EXIST("250009", "该会员不存在"),
    NO_ORG_IN_COUPON_THEME("250010", "券活动没有配置任何组织"),
    STORE_NOT_IN_RANGE("250011", "验证不通过，选择的店铺超出使用范围"),
    STORE_NOT_EXIST("250012", "店铺不存在"),
    COUPON_VERSION_NOT_MATCH("250013", "版本号校验失败，请检查是否重复核销"),
    COUPON_CODE_NOT_UNIQUE("250014", "券码不唯一"),
    COUPON_CODE_NULL("250015", "优惠券码不能为空"),
    SUBSCRIBE_CODE_NULL("250016", "明源认购书不能为空"),
    VERIFY_PHONE_NULL("250017", "核销手机号不能为空"),
    BUILD_CODE_NULL("250018", "楼盘编码不能为空"),
    COUPON_VERIFICATION_NOT_EXIST("250019", "核销记录不存在"),
    SUBSCRIBE_CODE_FORMAT_ERROR("250020", "认购书编号格式不正确，由G+7位数字组成"),
    VERIFY_TEMPLATE_DATA_EMPTY("250021", "核销模板没有数据"),
    PHONE_NOT_REGISTER("250022", "该手机号未注册"),
    ACCOUNT_DISABLE("250023", "该账号被禁用"),
    ;

    private final String code;

    private String message;

    CouponVerificationErrorCode(String code, String message) {
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

    public static CouponVerificationErrorCode of(String code) {
        return Stream.of(values())
                .filter(bean -> bean.code.equals(code))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("CouponVerificationErrorCode: code [" + code + "] 不存在"));
    }
}
