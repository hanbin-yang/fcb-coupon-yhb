package com.fcb.coupon.backend.model.bo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author HanBin_Yang
 * @since 2021/6/23 9:00
 */
@Data
public class CouponSingleVerifyBo {
    private Long userId;
    private String username;
    /**
     * 券码
     */
    private String couponCode;
    /**
     * 是否线下预制券
     */
    private Boolean offlineCouponFlag;
    /**
     * 明源认购书编号
     */
    private String subscribeCode;
    /**
     * 核销手机号
     */
    private String bindTel;

    /**
     * 券主键
     */
    private Long couponId;
    /**
     * 券活动主键
     */
    private Long couponThemeId;
    /**
     * 券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券
     */
    private Integer couponDiscountType;
    /**
     * 折扣时?折 乘于100后的值 金额时?元
     */
    private BigDecimal couponValue;
    /**
     * 核销店铺Id
     */
    private Long usedStoreId;
    /**
     * 核销店铺名称
     */
    private String usedStoreName;
    /**
     * 核销楼盘编码
     */
    private String usedStoreCode;
    /**
     * coupon表版本号
     */
    private Integer couponOldVersionNo;
    /**
     * 优惠券名称
     */
    private String themeTitle;
    /**
     * 优惠券生券时间
     */
    private Date couponCreateTime;

    private String bindUserId;
    /**
     * 用户类型 对应coupon表的
     */
    private Integer userType;
    /**
     * 生效时间
     */
    private Date startTime;
    /**
     * 失效时间
     */
    private Date endTime;

    /**
     * 券核销时间
     */
    private Date usedTime;
}
