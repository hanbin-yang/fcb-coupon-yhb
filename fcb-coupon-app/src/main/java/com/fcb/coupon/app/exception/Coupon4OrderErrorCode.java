package com.fcb.coupon.app.exception;

import com.fcb.coupon.common.exception.ResponseErrorCode;

/**
 * @author YangHanBin
 * @date 2021-08-24 9:59
 */
public enum Coupon4OrderErrorCode implements ResponseErrorCode {
    CAN_OPERATE_VERIFY_OR_OTHER("220000", "核销和其他操作不能同时存在"),
    OPERATE_OUT_OF_RANGE("220001", "操作的优惠券超限"),
    NO_REAL_COUPON("220002", "查询不到任何优惠券"),
    COUPON_NOT_EXIST("220003", "优惠券不存在"),
    PHONE_NOT_MATCH("220004", "手机号不匹配"),
    ORDER_CODE_NOT_MATCH("220005", "交易id不匹配"),
    CROWD_TYPE_NOT_MATCH("220006", "券适用人群不匹配"),
    NO_BUILDING("220007", "无可用楼盘"),
    BUILDING_OFFLINE("220008", "楼盘未上线"),
    EXPIRED("220009", "券不在有效期"),
    HAS_PROMOTION("220010", "房源正在参与促销活动，不能叠加使用优惠券"),
    NO_MATCH_BUILDING("220011", "券不适用于该项目下的楼盘"),
    PHONE_FORMAT_IS_WRONG("220012", "手机号格式不正确"),
    ROOM_GUID_REQUIRED("220013", "房源id不能为空"),
    TRANSACTION_ID_REQUIRED("220014", "交易id不能为空"),
    PROPERTY_TYPE_REQUIRED("220015", "物业类型不能为空, 0住宅 1公寓 2商铺 3写字楼 4车位 5储藏室"),
    ITEM_ID_REQUIRED("220016", "项目id不能为空"),
    OUT_OF_ORDER_LIMIT("220017", "所选券数量超过单笔订单限制"),
    NOT_CAN_USE_STATUS("220018", "要上锁的券状态不是可使用"),
    REPEAT_VERIFY("220019", "重复核销"),
    REPEAT_UNLOCK("220020", "重复解锁"),
    REPEAT_REBIND("220021", "重复换绑"),
    REPEAT_LOCK("220022", "重复上锁"),
    COUPON_IDS_REQUIRED("220023", "优惠券主键ids必须填写"),
    BIND_OTHER_ORDER_CODE("220024", "券已被其他交易id绑定"),
    OLD_ORDER_CODE_NOT_MATCH("220025", "原交易id不匹配"),
    NOT_LOCK_STATUS_FOR_VERIFY("220026", "要核销的券不是上锁状态"),
    NOT_LOCK_STATUS_FOR_UNLOCK("220027", "要解锁的券不是上锁状态"),
    NOT_LOCK_OR_USED_STATUS_FOR_REBIND("220028", "要换绑的券不是上锁或已使用状态"),
    NOT_FOUND_COUPON_THEME("220029", "券活动不存在"),
    NO_OPERATE_COUPONS("220030", "无可操作的优惠券"),
    UNLOCK_DB_ERROR("220031", "解锁优惠券,数据库行锁校验失败"),
    LOCK_COUPON_DB_ERROR("220032", "上锁优惠券,数据库行锁校验失败"),
    REBIND_COUPON_DB_ERROR("220033", "换绑优惠券,数据库行锁校验失败"),
    VERIFY_COUPON_DB_ERROR("220034", "核销优惠券,数据库行锁校验失败"),
    CANT_NOT_OPERATE_THIRD_COUPON("220035", "第三方券不能操作"),
    COUPON_EXPIRED("220036", "要上锁的券不在有效期"),
    OLD_AND_NEW_TRANSACTION_EQUALS("220037", "新旧交易id不一致，不能换绑"),
    PHONE_NOT_NULL("220038", "手机号不能为空"),
    REQUEST_DATA_EMPTY_ERROR("220039","请求业务参数为空"),
    CAN_NOT_FIND_BUILDING("220040","根据明源项目id查询不到楼盘"),

    SERVICE_EXCEPTION("220050", "服务异常"),
    FREQUENT_OPERATE("220051", "操作太频繁"),
    FAIL("220052", "操作失败"),

    PHONE_NOT_REGISTER("250053", "该手机号未注册"),
    ;
    private final String code;
    private final String message;

    Coupon4OrderErrorCode(String code, String message) {
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

