package com.fcb.coupon.app.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author YangHanBin
 * @date 2021-08-24 11:19
 */
@Data
public class CouponDo {
    private static final long serialVersionUID = 1L;

    //***************coupon表数据**************
    /**
     * 券主键
     */
    private Long id;

    /**
     * 券活动id
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
     * 券码
     */
    private String couponCode;

    /**
     * 券活动名称
     */
    private String themeTitle;

    /**
     * 券类型 0自动生成 3第三方券码
     */
    private Integer couponType;

    /**
     * 生效时间
     */
    private Date startTime;

    /**
     * 失效时间
     */
    private Date endTime;

    /**
     * 用户类型,0是会员,1是机构经纪人,2是C端用户
     */
    private Integer userType;

    /**
     * 绑定用户id
     */
    private String userId;
    /**
     * 券创建时间
     */
    private Date couponCreateTime;
    /**
     * 券创建人id
     */
    private Long couponCreateUserid;
    /**
     * 券创建人名称
     */
    private String couponCreateUsername;

    /**
     * 券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结
     */
    private Integer status;
    //***************coupon_verification表数据**************
    /**
     * 使用券的订单号
     */
    private String orderCode;

    /**
     * 核销房源guid
     */
    private String usedRoomGuid;

    /**
     * 核销商品名称
     */
    private String productName;

    /**
     * 核销商品编码
     */
    private String productCode;
}
