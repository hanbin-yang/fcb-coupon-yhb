package com.fcb.coupon.backend.model.dto;

import lombok.Data;

import java.util.List;
import java.util.TreeSet;

/**
 * 营销中心->主动营销->营销任务管理->编辑任务流->添加优惠券->查询优惠券列表 入参
 * @author mashiqiong
 * @date 2021-06-17 20:59
 *
 */
@Data
public class CouponThemeInitiativeMarketingDo {

    /**
     * 优惠券Id
     */
    private Long id;

    /**
     * 优惠券名称
     */
    private String themeTitle;

    /**
     * 发券类型(1:活动规则券,19:线下预制券,4:前台领券,17:主动营销券,18:权益优惠券,19:线下预制券,20:媒体广告券,21:直播券,22:营销活动页券)
     */
    private Integer couponGiveRule;

    /**
     * 状态，4 进行中
     */
    private Integer status;

    /**
     * 使用人群 0会员 1机构经济人 2C端用户
     */
    private TreeSet<Integer> applicableUserTypes;

    /**
     * 券所属商家
     */
    private List<Long> orgIdList;

    /**
     * 当前页码
     */
    private Integer startItem;

    /**
     * 页面pageSize
     */
    private Integer itemsPerPage;

}
