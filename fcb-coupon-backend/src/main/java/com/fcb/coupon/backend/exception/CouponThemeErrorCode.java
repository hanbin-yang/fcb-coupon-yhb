package com.fcb.coupon.backend.exception;

import com.fcb.coupon.common.exception.ResponseErrorCode;

/**
 * @author YangHanBin
 * @date 2021-06-15 11:11
 */
public enum CouponThemeErrorCode implements ResponseErrorCode {
    CREATE_PLATFORM_COUPON_WITHOUT_AUTH("222001", "您没有权限创建优惠券！"),
    COUPON_DISCOUNT_TYPE_ERROR("222002", "优惠方式类型错误"),
    DELETE_FAIL("222003", "该券活动无法删除，请检查"),
    CLOSE_FAIL("222004", "该券活动无法关闭，请检查"),
    CAN_NOT_EDIT("222005", "券活动不允许编辑"),
    CAN_NOT_CLOSE("222006", "券活动不允许关闭"),
    CAN_NOT_SUBMIT_AUDIT("222007", "券活动不允许提交审核"),
    CAN_NOT_DELETE("222008", "券活动不允许删除"),
    CAN_NOT_BUILD_CACHE("222009", "该券活动不可创建缓存"),
    COUPON_THEME_NOT_EXIST("222010", "券活动不存在"),
    COUPON_THEME_ID_IS_NULL("220011", "券活动Id不能为空"),
    GENERATE_COUPONS_OUT_OF_LIMIT("222012", "生券数量超过券活动总限制数量"),
    COUPON_IMPORT_FILE_NOT_EXISTS("222013", "请选择要导入的文件"),
    COUPON_IMPORT_FILE_ERROR("222014", "请导入CSV文件"),
    COUPON_IMPORT_PRASE_ERROR("222015", "CSV文件解析异常"),
    COUPON_IMPORT_NOT_DATA_ERROR("222016", "无导入数据"),
    COUPON_IMPORT_DATA_REPEAT_ERROR("222017", "存在重复数据"),
    COUPON_IMPORT_OUT_TOTAL_ERROR("222018", "超过优惠券可发行总数"),
    COUPON_IMPORT_LOCK_FAIL("222019", "当前券活动正在导入券码，请稍候"),
    COUPON_IMPORT_NOT_ALLOW("222020", "当前券活动不允许导入第三方券码"),
    COUPON_EFF_DATE_NOT_EXIST("222021", "券固定有效期设置不存在"),
    COUPON_THEME_NOT_EFFECTIVE("222022", "券活动不是进行中状态"),
    COUPON_THEME_NOT_IN_START_END_TIME("222023", "不在券活动有效时间内"),
    COUPON_THEME_NOT_CONFIG_SEND_USER("222024", "券活动未配置适用人群"),
    COUPON_THEME_SEND_USER_NOT_MATCH("222025", "发送对象不在适用范围"),
    COUPON_THEME_SEND_USER_EMPTY("222026", "发送对象不能为空"),
    COUPON_THEME_SEND_USER_REPEAT("222027", "发送对象存在重复数据,请检查"),
    COUPON_THEME_SEND_USER_CONFLICT("222028", "发送对象不能同时使用账号和手机号"),
    COUPON_THEME_SEND_COUNT_ERROR("222029", "发送数量不能少于1"),
    COUPON_THEME_CAN_SEND_COUNT_ERROR("222030", "券活动可发行数量不足"),
    COUPON_THEME_SEND_TYPE_NOT_SUPPORT("222031", "不支持的发券对象"),
    COUPON_THEME_COUPON_TYPE_NOT_SUPPORT("222032", "券活动类型不支持发券"),
    COUPON_THEME_COUPON_SOURCE_NOT_SUPPORT("222033", "券活动来源不支持发券"),
    COUPON_IMPORT_ERROR("222034", "第三方券码导入异常"),
    COUPON_THEME_TYPE_ERROR("222035", "发券类型错误"),
    COUPON_SEND_ERROR("222036", "发券失败"),
    ;

    private final String code;

    private final String message;

    CouponThemeErrorCode(String code, String message) {
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
