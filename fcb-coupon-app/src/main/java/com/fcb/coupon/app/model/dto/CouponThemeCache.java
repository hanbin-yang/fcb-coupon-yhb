package com.fcb.coupon.app.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author YangHanBin
 * @date 2021-06-15 08:20
 */
@Data
@Accessors(chain = true)
public class CouponThemeCache implements Serializable {
    private static final long serialVersionUID = -7280616442145567428L;
    /*=================coupon_theme表字段=================*/
    /**
     * id 主键
     */
    private Long id;
    /**
     * 活动名称
     */
    private String activityName;
    /**
     * 优惠券名称
     */
    private String themeTitle;
    /**
     * 活动开始时间
     */
    private Date startTime;
    /**
     * 活动结束时间活动结束时间
     */
    private Date endTime;
    /**
     * 活动描述
     */
    private String themeDesc;
    /**
     * 活动状态 0 未审核 1 待审核 2 未开始 3 审核不通过 4 进行中 5 已过期 6 已关闭
     */
    private Integer status;
    /**
     * 活动类型 0：平台券  11：商家券 5：集团券 21：店铺券
     */
    private Integer themeType;
    /**
     * 券码生成方式 0自动生成 3：第三方券码
     */
    private Integer couponType;
    /**
     * 发券类型(1:活动规则券,19:线下预制券,4:前台领券,17:主动营销券,18:权益优惠券,19:线下预制券,20:媒体广告券,21:直播券,22:营销活动页券)
     */
    private Integer couponGiveRule;
    /**
     * 使用限制  0：无限制， 其他：最小金额限制 满?元可用
     */
    private BigDecimal useLimit;
    /**
     * 使用人群ids,0是会员,1是机构经纪人,2是C端用户
     */
    private String applicableUserTypes;
    /**
     * 单个订单该类型券使用张数限制
     */
    private Integer orderUseLimit;
    /**
     * 当前券活动个人可领取券数
     */
    private Integer individualLimit;
    /**
     * 个人每日限领张数
     */
    private Integer everyDayLimit;
    /**
     * 个人每月限领张数
     */
    private Integer everyMonthLimit;
    /**
     * 费用归属组织id
     */
    private Long belongingOrgId;
    /**
     * 券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券
     */
    private Integer couponDiscountType;
    /**
     * 有效期计算方式  1：固定有效期，2：从领用开始计算
     */
    private Integer effDateCalcMethod;
    /**
     * 券图片地址
     */
    private String couponPicUrl;
    /**
     * 非数据库字段
     * 固定有效期开始时间
     * 1：固定有效期时不为空
     */
    private Date effDateStartTime;
    /**
     * 非数据库字段
     * 固定有效期结束时间
     * 1：固定有效期时不为空
     */
    private Date effDateEndTime;
    /**
     * 非数据库字段
     * 自用户领取几天后失效
     * 2：从领用开始计算时不为空
     */
    private Integer effDateDays;
    /**
     * 非数据库字段
     * 券优惠类型 金额时 ?元
     */
    private BigDecimal discountAmount;
    /**
     * 非数据库字段
     * 券优惠类型 折扣时 ?折 乘于100后的值
     */
    private Integer discountValue;
    /**
     * 是否可赠送 1可以 0不可
     */
    private Integer canDonation;
    /**
     * 是否可转让 1可以 0不可
     */
    private Integer canTransfer;

    /*=================coupon_theme_statistic表字段=================*/
    /**
     * 券活动总可领取券数
     */
    private Integer totalCount;
    /**
     * 券活动已生成券数
     */
    private Integer createdCount;
    /**
     * 已发数量
     */
    private Integer sendedCount;
}
