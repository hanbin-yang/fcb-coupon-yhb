package com.fcb.coupon.app.model.bo;

import lombok.Data;

/**
 * @author YangHanBin
 * @date 2021-08-16 8:44
 */
@Data
public class CouponReceiveBo {
    /**
     * 券活动id
     */
    private Long couponThemeId;

    /**
     * 用户类型
     */
    private Integer userType;

    /**
     * 领券用户名称
     */
    private String userMobile;
    /**
     * 领券用户id
     */
    private String userId;

    /**
     * 4主动领券 24媒体广告领券 25直播领券  26营销活动页领券
     */
    private Integer source;
    /**
     * 来源id
     */
    private String sourceId;
    /**
     * 领取张数, 暂不支持领券多张
     */
    private int receiveCount = 1;
}
