package com.fcb.coupon.app.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * <p>
 * 劵表
 * </p>
 *
 * @author 自动生成
 * @since 2021-06-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("coupon_user")
@ApiModel(value="CouponUserEntity对象", description="劵表")
public class CouponUserEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "优惠券ID")
    @TableId(value = "coupon_id", type = IdType.INPUT)
    private Long couponId;

    @ApiModelProperty(value = "券活动id")
    private Long couponThemeId;

    @ApiModelProperty(value = "绑定用户id")
    private String userId;

    @ApiModelProperty(value = "绑定人手机号")
    private String bindTel;

    @ApiModelProperty(value = "用户类型,0是会员,1是机构经纪人,2是C端用户")
    private Integer userType;

    @ApiModelProperty(value = "券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结")
    private Integer status;

    @ApiModelProperty(value = "过期时间")
    private Date endTime;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;


}
