package com.fcb.coupon.backend.model.bo;

import com.fcb.coupon.common.dto.UserInfo;
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
public class CouponThemeInitiativeMarketingBo {

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
     * 状态，4 进行中(活动已开始且活动时间在有效期内的券)
     */
    private Integer status;

    /**
     * 使用人群 0会员 1机构经济人 2C端用户
     */
    private TreeSet<Integer> crowdScopeIds;

    /**
     * 券所属商家
     */
    private List<Long> orgIds;

    /**
     * 当前登录用户id
     */
    private Long userId;

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
