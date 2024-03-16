package com.fcb.coupon.backend.model.dto;

import lombok.Data;

import java.util.List;
import java.util.TreeSet;

/**
 * 营销中心->优惠券管理->优惠券运营位管理->添加优惠券->查询优惠券列表 入参
 * @author mashiqiong
 * @date 2021-06-17 20:59
 *
 */
@Data
public class CouponThemePositionMarketingDo {

    /**
     * 优惠券Ids
     */
    private List<Long> ids;

    /**
     * 优惠券名称
     */
    private String themeTitle;

    /**
     * 发券类型(1:活动规则券,19:线下预制券,4:前台领券,17:主动营销券,18:权益优惠券,19:线下预制券,20:媒体广告券,21:直播券,22:营销活动页券)
     */
    private Integer couponGiveRule;

    /**
     * 状态，-1:全部；456:进行中、已过期、已关闭；0 未审核 1 待审核 3 审核不通过 4 进行中 5 已过期 6 已关闭
     */
    private Integer status;

    /**
     * 使用人群 0会员 1机构经济人 2C端用户
     */
    private TreeSet<Integer> applicableUserTypes;

    /**
     * 发布范围获取规则 0向上共有的 1向下递归的 2向上共有的和向下递归的
     */
    private Integer rangeRuleType;

    private Integer hasCanDonation;

    /**
     * 券所属商家
     */
    private List<Long> orgIdList;

    /**
     * 发布范围商家ID列表
     */
    private List<Long> rangeMerchantIdList;

    /**
     * 发布范围组织ID列表
     */
    private List<Long> rangeGroupIdList;

    /**
     * 发布范围门店列表
     */
    private List<Long> rangeStoreIdList;

    /**
     * 当前页码
     */
    private Integer startItem;

    /**
     * 页面pageSize
     */
    private Integer itemsPerPage;

}
