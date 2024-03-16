package com.fcb.coupon.app.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月26日 09:22:00
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("coupon_video")
@ApiModel(value = "券视频直播领券表", description = "券视频直播领券表")
public class CouponVideoEntity extends BaseEntity {


    @ApiModelProperty(value = "优惠券ID")
    @TableId(value = "coupon_id", type = IdType.INPUT)
    private Long couponId;

    @ApiModelProperty(value = "券活动id")
    private Long couponThemeId;

    @ApiModelProperty(value = "openid")
    private String openId;

    @ApiModelProperty(value = "城市")
    private String city;

    @ApiModelProperty(value = "姓名")
    private String name;

    @ApiModelProperty(value = "视频号")
    private String videoNo;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

}
