package com.fcb.coupon.client.backend.param.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月09日 19:26:00
 */
@Data
public class ActivitySendCouponResponse implements Serializable {

    @ApiModelProperty(value = "领券结果列表")
    private List<ActivitySendCouponDataResponse> data;

    @Data
    public static class ActivitySendCouponDataResponse implements Serializable {

        @ApiModelProperty(value = "优惠券ID")
        private Long couponId;

        @ApiModelProperty(value = "用户id")
        private String userId;

        @ApiModelProperty(value = "用户类型,0是会员,1是机构经纪人,2是C端用户")
        private Integer userType;

        @ApiModelProperty(value = "是否失败")
        private Boolean isFailure;

        @ApiModelProperty(value = "失败原因")
        private String failureReason;

        @ApiModelProperty(value = "能否重试")
        private Boolean canRetry;
    }


}
