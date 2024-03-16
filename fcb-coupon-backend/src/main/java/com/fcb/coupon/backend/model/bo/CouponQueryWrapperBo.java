package com.fcb.coupon.backend.model.bo;

import lombok.Data;

import java.util.Date;

/**
 *
 * @Author WeiHaiQi
 * @Date 2021-07-14 17:37
 **/
@Data
public class CouponQueryWrapperBo {
    /**
     * 分页查询每页数据量
     */
    private Integer pageSize;
    /**
     * 分页查询偏移
     */
    private Integer offset;

    /**
     * 主键id
     */
    private Long id;
    /**
     * 券活动id
     */
    private Long couponThemeId;
    /**
     * 用户类型
     */
    private Integer userType;
    /**
     * 绑定手机号
     */
    private String bindTel;

    /**
     * 支持时间范围刷新 coupon表中的createTime字段 开始时间
     */
    private Date createTimeStart;

    /**
     * 支持时间范围刷新 coupon表中的createTime字段 结束时间
     */
    private Date createTimeEnd;
}
