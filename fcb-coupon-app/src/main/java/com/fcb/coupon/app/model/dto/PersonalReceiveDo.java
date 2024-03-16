package com.fcb.coupon.app.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author YangHanBin
 * @date 2021-08-23 9:32
 */
@Data
@Accessors(chain = true)
public class PersonalReceiveDo {
    /**
     * 券活动Id
     */
    private Long couponThemeId;
    /**
     * 用户id 必须确保真实性，否则数据库出现垃圾数据
     */
    private String userId;
    /**
     * 用户类型 0会员 1Saas 2C端
     */
    private Integer userType;
    /**
     * 领券数量
     */
    private int receiveCount;
    /**
     * 个人总领券上限
     */
    private Integer totalLimit;
    /**
     * 个人每月领券上限
     */
    private Integer monthLimit;
    /**
     * 个人每天领券上限
     */
    private Integer dayLimit;
    /**
     * 领券时间
     */
    private Date receiveTime;
}
