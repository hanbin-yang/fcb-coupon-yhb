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
 * @createTime 2021年08月20日 14:31:00
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("coupon_send_log")
@ApiModel(value = "CouponSendLogEntity", description = "发券记录表")
public class CouponSendLogEntity extends BaseEntity {

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "券活动ID")
    private Long couponThemeId;

    @ApiModelProperty(value = "发券事务id")
    private String transactionId;

    private Long createUserid;

    private String createUsername;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}
