package com.fcb.coupon.common.constant;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月09日 19:36:00
 */
public interface CouponConstant {

    String SUCCESS_CODE = "000000";
    String SUCCESS_MESSAGE = "成功";
    String FAIL_MESSAGE = "失败";
    int YES = 1;
    int NO = 0;

    /**
     * 总部
     **/
    String ZB = "ZB";

    /**
     * 券明细查询类型：  1核销
     **/
    int COUPON_QUERY_TYPE = 1;
    String COUPON_UNIT_AMOUNT = "元";
    String COUPON_UNIT_DISCOUNT = "折";

    String RESPONSE_SUCCESS_CODE = "000000";

    // coupon_generate_batch.type
    int BATCH_GENERATE_TYPE = 0; // 0: 批次生券， 为线下
    int BATCH_SEND_TYPE = 1; // 1：批次发券 - 批量导入
    int BATCH_SEND_TYPE_ARTIFICAL_INTRODUCTION = 2; // 2：批次发券 - 优惠券手动批量导入
    int BATCH_IMPORT_TYPE_ARTIFICAL_INTRODUCTION = 3; // 3：导入券码
    int BATCH_EXPORT_COUPONT_INFO = 4; // 3：导出
}
