package com.fcb.coupon.backend.model.bo;

import com.fcb.coupon.common.dto.UserInfo;
import lombok.Data;

import java.util.Date;

/**
 * 后台管理->营销中心->优惠券管理->券核销->券核销列表
 * @author mashiqiong
 * @date 2021-6-23 21:23
 */
@Data
public class CouponVerificationListBo {
    /**
     * 优惠券ID
     */
    private Long couponActivityId;
    /**
     * 券活动名称
     */
    private String couponActivityName;
    /**
     * 优惠券状态
     */
    private Integer couponStatus;
    /**
     * 券号
     */
    private String couponCode;
    /**
     * 券生效开始时间
     */
    private Date couponEffectiveStartTime;
    /**
     * 券生效结束时间
     */
    private Date couponEffectiveEndTime;
    /**
     * 创建开始时间
     */
    private Date couponCreateStartTime;
    /**
     * 创建结束时间
     */
    private Date couponCreateEndTime;
    /**
     * 核销开始时间
     */
    private Date usedStartTime;
    /**
     * 核销结束时间
     */
    private Date usedEndTime;
    /**
     * 优惠券绑定开始时间
     */
    private Date couponBindStartTime;
    /**
     * 优惠券绑定截至时间
     */
    private Date couponBindEndTime;
    /**
     * 手机号码
     */
    private String cellNo;
    /**
     * 核销人
     */
    private String updateUsername;
    /**
     * 使用人群编号，0是会员,1是机构经纪人,2是C端用户
     */
    private Integer crowdScopeId;
    /**
     * 核销店铺Id
     */
    private Long usedStoreId;
    /**
     * 核销店铺名称
     */
    private String usedStoreName;
    /**
     * 楼盘编码
     */
    private String usedStoreCode;
    /**
     * 订单code
     */
    private String orderCode;

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

    private String ut;

    public void loadUserInfo(UserInfo userInfo) {
        this.userId = userInfo.getId();
        this.ut = userInfo.getUt();
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
