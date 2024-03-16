package com.fcb.coupon.backend.model.bo;

import com.fcb.coupon.backend.model.ao.CouponThemeOwnedOrgAo;
import com.fcb.coupon.common.dto.UserInfo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author YangHanBin
 * @date 2021-06-15 18:09
 */
@Data
public class CouponThemeUpdateBo {
    /**
     * 券活动主键id
     */
    private Long couponThemeId;
    /**
     * 当前登录用户id
     */
    private Long userId;
    /**
     * 当前登录用户名
     */
    private String username;
    /**
     * 当前登录用户组织级别
     */
    private String userOrgLevelCode;

    /**
     * 发券类型(1:活动规则券,19:线下预制券,4:前台领券,17:主动营销券,18:权益优惠券,19:线下预制券,20:媒体广告券,21:直播券,22:营销活动页券)
     */
    private Integer couponGiveRule;
    /**
     * 所属商家
     */
    private List<CouponThemeOwnedOrgAo> orgList;
    /**
     * 费用归属组织id
     */
    private Long belongingOrgId;
    /**
     * 适用范围 ：0:平台  1:自营券
     */
    private Integer applicationScope;
    /**
     * 每ID总共可以领取
     */
    private Integer individualLimit;
    /**
     * 每ID总共可以领取
     */
    private Integer everyDayLimit;
    /**
     * 每个ID每月可以领取
     */
    private Integer everyMonthLimit;
    /**
     * 优惠券名称
     */
    private String themeTitle;
    /**
     * 券活动名称
     */
    private String activityName;
    /**
     * 券码生成方式 0自动生成 3：第三方券码
     */
    private Integer couponType;
    /**
     * 优惠方式 0：金额 1：折扣 11：福利卡 12：红包券
     */
    private Integer couponDiscountType;
    /**
     * 适用范围 0：平台券  11：商家券 5：集团券 21：店铺券
     */
    private Integer themeType;
    /**
     * 优惠券面值
     */
    private BigDecimal couponAmount;
    /**
     * 优惠券折扣 7折为70
     */
    private Integer couponDiscount;
    /**
     * 折扣上限 ?元
     */
    private BigDecimal couponAmountExt;
    /**
     * 发行总量
     */
    private Integer totalLimit;
    /**
     * 券活动日期 开始
     */
    private Date startTime;
    /**
     * 券活动日期 结束
     */
    private Date endTime;
    /**
     * 使用说明
     */
    private String themeDesc;
    /**
     * 使用人群 格式：{ids:[0,1]} 0会员 1机构经济人 2C端用户
     */
    String crowdScopeIds;
    /**
     * 券码有效期类型 1：固定有效期，2：从领用开始计算
     */
    private Integer effdateCalcMethod;
    /**
     * 固定有效时间 开始
     */
    private Date startTimeConfig;
    /**
     * 固定有效时间 结束
     */
    private Date endTimeConfig;
    /**
     * 自用户领取?天后失效
     */
    private Integer effDays;
    /**
     * 使用条件 订单满?元
     */
    private BigDecimal useLimit;
    /**
     * 每个订单号每次最多可使用
     */
    private Integer orderUseLimit;
    /**
     * 是否可赠送
     */
    private Integer canDonation;
    /**
     * 是否可转让
     */
    private Integer canAssign;


    public void loadUserInfo(UserInfo userInfo) {
        this.userId = userInfo.getId();
        this.username = userInfo.getUsername();
        this.userOrgLevelCode = userInfo.getOrgLevelCode();
    }
}
