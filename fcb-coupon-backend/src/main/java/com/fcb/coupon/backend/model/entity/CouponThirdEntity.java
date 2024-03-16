package com.fcb.coupon.backend.model.entity;

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
 * @createTime 2021年08月11日 17:26:00
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("coupon_third")
@ApiModel(value = "第三方劵表", description = "第三方劵表")
public class CouponThirdEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "优惠券ID")
    @TableId(value = "coupon_id", type = IdType.INPUT)
    private Long couponId;

    @ApiModelProperty(value = "券活动id")
    private Long couponThemeId;

    @ApiModelProperty(value = "第三方优惠券码")
    private String thirdCouponCode;

    @ApiModelProperty(value = "第三方优惠券密码(保存密文)")
    private String thirdCouponPassword;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "逻辑删除字段 0 正常 1 已删除")
    @TableLogic
    private Integer isDeleted;


}
