package com.fcb.coupon.backend.model.bo;

import com.fcb.coupon.backend.model.ao.OrgRangeAo;
import com.fcb.coupon.common.dto.UserInfo;
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
public class CouponThemePositionMarketingBo {

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
     * 状态，24:进行中(活动时间未开始或者活动时间在有效期内的券)
     */
    private Integer status;

    /**
     * 使用人群 0会员 1机构经济人 2C端用户
     */
    private TreeSet<Integer> crowdScopeIds;

    /**
     * 发布范围
     */
    private List<OrgRangeAo> rangeList;

    /**
     * 发布范围获取规则 0向上共有的 1向下递归的 2向上共有的和向下递归的
     */
    private Integer rangeRuleType;

    /**
     * 是否还需要额外查询可赠送的券活动 true是
     */
    private Boolean hasCanDonation;

    /**
     * 当前登录用户id
     */
    private Long userId;

    /**
     * 当前登录用户的令牌(user token)
     */
    private String ut;

    /**
     * 当前登录用户组织级别
     */
    private String userOrgLevelCode;

    /**
     * 当前页码
     */
    private Integer currentPage;

    /**
     * 页面pageSize
     */
    private Integer itemsPerPage;

    public void loadUserInfo(UserInfo userInfo) {
        this.userId = userInfo.getId();
        this.userOrgLevelCode = userInfo.getOrgLevelCode();
        this.ut = userInfo.getUt();
    }

    /**
     *
     * @description <pre>
     * 根据页码和每页记录数获取页起始记录
     * </pre>
     * @return
     */
    public int getStartItem() {

        int start = (currentPage - 1) * itemsPerPage;
        if (start < 0) {
            start = 0;
        }
        return start;
    }
}
