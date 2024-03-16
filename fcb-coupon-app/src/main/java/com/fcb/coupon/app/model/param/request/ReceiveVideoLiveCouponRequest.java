package com.fcb.coupon.app.model.param.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月25日 19:23:00
 */
@Data
@ApiModel(value = "领取视频直播活动优惠券入参")
public class ReceiveVideoLiveCouponRequest implements Serializable {

    @NotNull(message = "券活动ID不能为空")
    @ApiModelProperty(value = "券活动ID")
    private Long couponThemeId;

    @Length(max = 50, message = "定位城市长度错误")
    @NotBlank(message = "定位城市不能为空")
    @ApiModelProperty(value = "定位城市")
    private String city;

    @Length(max = 50, message = "客户姓名长度错误")
    @NotBlank(message = "客户姓名不能为空")
    @ApiModelProperty(value = "客户姓名")
    private String customerName;

    @Length(max = 50, message = "openid长度错误")
    @NotBlank(message = "openid不能为空")
    @ApiModelProperty(value = "openid")
    private String openid;

    @Length(max = 50, message = "视频号长度错误")
    @ApiModelProperty("视频号")
    private String videoNo;
}
