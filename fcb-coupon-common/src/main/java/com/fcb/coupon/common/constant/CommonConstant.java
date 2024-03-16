package com.fcb.coupon.common.constant;

import java.text.DecimalFormat;

/**
 * @author mashiqiong
 * @date 2021-8-4 16:14
 */
public class CommonConstant {
    public static final Integer DEFAULT_BATCH_SIZE = 100;
    // 默认渠道code
    public static final String DEFAULT_CHANNEL_CODE = "-1";
    //商家商品类型
    public static final Integer DATA_TYPE_MERCHANT_MP = 2;
    /**
     * remote服务返回正确结果码
     */
    public static final String REMOTE_RESULT_CODE_SUCCESS = "0";

    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
}
