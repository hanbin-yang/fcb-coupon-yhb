package com.fcb.coupon.backend.model.cache;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author YangHanBin
 * @date 2021-07-29 10:26
 */
@Data
public class CouponBeforeGiveCache {
    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "转增的券id")
    private Long couponId;

    @ApiModelProperty(value = "赠送者头像地址")
    private String giveAvatar;

    @ApiModelProperty(value = "赠送者昵称")
    private String giveNickname;

    @ApiModelProperty(value = "赠送者手机号")
    private String giveUserMobile;

    @ApiModelProperty(value = "领取时效的终止时间")
    private Date expireTime;

    @ApiModelProperty(value = "优惠券名称")
    private String couponName;

    @ApiModelProperty(value = "折扣/金额 值")
    private String discountAmount;

    @ApiModelProperty(value = "折扣类型 0金额 1折扣")
    private Integer discountType;

    @ApiModelProperty(value = "券有效时间")
    private Date endTime;

    /**
     * 领取人用户id
     */
    private String receiveUserId;
    /**
     * 券状态
     */
    private Integer couponStatus;
}
