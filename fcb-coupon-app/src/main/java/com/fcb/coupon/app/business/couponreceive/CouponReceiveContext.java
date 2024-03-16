package com.fcb.coupon.app.business.couponreceive;

import com.fcb.coupon.app.model.dto.CouponThemeCache;
import com.fcb.coupon.app.model.dto.UserReceiveMemento;
import com.fcb.coupon.app.model.entity.CouponEntity;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * @author YangHanBin
 * @date 2021-08-16 11:20
 */
@Builder
@Data
public class CouponReceiveContext {
    /**
     * 券活动id
     */
    private Long couponThemeId;

    /**
     * 用户类型 0是会员 1是SAAS端用户 2是C端用户
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
     * 领取张数
     */
    private int receiveCount;

    /**
     * 领券时间
     */
    private Date receiveTime;
    /**
     * 扣减redis个人领券限制后的备忘录实体，异常回滚用
     */
    private UserReceiveMemento userReceiveMemento;
    /**
     * 券活动缓存
     */
    private CouponThemeCache couponThemeCache;

    /**
     * 领券之后填充，用于更新es
     */
    private CouponEntity couponEntity;
}
