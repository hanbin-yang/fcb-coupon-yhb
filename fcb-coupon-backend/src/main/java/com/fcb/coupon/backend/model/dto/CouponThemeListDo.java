package com.fcb.coupon.backend.model.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 营销中心->优惠券管理->优惠券活动列表 入参
 * @author mashiqiong
 * @date 2021-06-16 10:43
 */
@Data
public class CouponThemeListDo {

    /**
     * 优惠券Id
     */
    private Long id;

    /**
     * 优惠券名称
     */
    private String themeTitle;

    /**
     * 券活动名称
     */
    private String activityName;

    /**
     * 券码生成方式 0电子券 1实体券/预制券 2红包券 3：第三方券码
     */
    private Integer couponType;

    /**
     * 发券类型(1:活动规则券,19:线下预制券,4:前台领券,17:主动营销券,18:权益优惠券,19:线下预制券,20:媒体广告券,21:直播券,22:营销活动页券)
     */
    private Integer couponGiveRule;

    /**
     * 状态，-1:全部；0 未审核 1 待审核 3 审核不通过 4 进行中 5 已过期 6 已关闭
     */
    private Integer status;

    /**
     * 券活动日期 开始
     */
    private Date startTime;

    /**
     * 券活动日期 结束
     */
    private Date endTime;

    /**
     * 使用人群 0会员 1机构经济人 2C端用户
     */
    private Integer applicableUserTypes;

    /**
     * 券所属商家
     */
    private List<Long> orgIdList;

    /**
     * 优惠方式 0：金额 1：折扣 11：福利卡 12：红包券
     */
    private Integer couponDiscountType;

    /**
     * 当前页码
     */
    private Integer startItem;

    /**
     * 页面pageSize
     */
    private Integer itemsPerPage;

}
