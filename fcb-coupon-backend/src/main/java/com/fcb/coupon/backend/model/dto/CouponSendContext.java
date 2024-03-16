package com.fcb.coupon.backend.model.dto;

import com.fcb.coupon.backend.model.entity.CouponEntity;
import com.fcb.coupon.backend.model.entity.CouponUserEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/*
用户发券上下文
 */
@Builder
public class CouponSendContext {

    @Setter
    @Getter
    @ApiModelProperty(value = "券活动id")
    private Long couponThemeId;

    @Setter
    @Getter
    @ApiModelProperty(value = "绑定用户id")
    private String userId;

    @Setter
    @Getter
    @ApiModelProperty(value = "绑定人手机号")
    private String bindTel;

    @Setter
    @Getter
    @ApiModelProperty(value = "UnionId")
    private String unionId;

    @Setter
    @Getter
    @ApiModelProperty(value = "用户类型,0是会员,1是机构经纪人,2是C端用户")
    private Integer userType;
    /*
     来源
      */
    @Setter
    @Getter
    private Integer source;

    @Setter
    @Getter
    private String sourceId;

    @Setter
    @Getter
    private String transactionId;

    @Setter
    @Getter
    private Long createUserid;

    @Setter
    @Getter
    private String createUsername;

    /*
    是否失败
     */
    @Getter
    private Boolean isFailure;
    /*
    失败原因
     */
    @Getter
    private String failureReason;

    /*
     能否重试
      */
    @Getter
    private Boolean canRetry;

    /*
     * @description 发券信息
     * @author 唐陆军
     * @date 2021-8-6 9:51
     */
    @Getter
    private CouponEntity couponEntity;

    @Getter
    private CouponUserEntity couponUserEntity;


    public void error(Boolean canRetry, String failureReason) {
        this.isFailure = true;
        this.canRetry = canRetry;
        this.failureReason = failureReason;
    }

    public void success(CouponEntity couponEntity, CouponUserEntity couponUserEntity) {
        this.isFailure = false;
        this.couponEntity = couponEntity;
        this.couponUserEntity = couponUserEntity;
    }
}
