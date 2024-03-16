package com.fcb.coupon.common.exception;

/**
 * @author YangHanBin
 * @date 2021-06-11 20:30
 */
public enum CommonErrorCode implements ResponseErrorCode {

    PARAMS_ERROR("400", "参数错误"),
    UNAUTHORIZED("401", "认证失败"),
    NO_FOUND("404", "请求地址错误"),
    METHOD_NOT_ALLOWED("405", "Method不支持"),
    API_CALL_ERROR("5000", "接口调用异常"),

    NO_LOGIN("000001", "用户未登录！"),
    UT_EXPIRED("000002", "ut失效，请重新登录！"),
    SYSTEM_ERROR("000003", "系统繁忙"),
    OPERATE_FREQUENTLY("000004", "操作太频繁，请稍后重试"),
    GET_AUTHORITY_MERCHANT_FAIL("000005", "获取权限merchant失败，请重新登录"),
    GET_AUTHORITY_STORE_INFO_FAIL("000006", "获取权限storeInfo失败，请重新登录"),
    GET_COUPON_THEM_CACHE("000007", "获取券活动缓存异常"),
    NO_AUTH_PATH("000008", "请求地址无访问权限"),
    USER_TYPE_UNSUPPORTED("000009", "用户类型不支持"),
    AUTHORIZATION_NULL("000010", "请求头Authorization为空"),
    UNION_ID_NULL("000011", "请求头unionId为空"),
    CLIENT_TYPE_NULL("000012", "请求头clientType为空"),
    ;

    private final String code;

    private final String message;

    CommonErrorCode(String code, String message) {
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
