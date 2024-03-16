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
 * 劵转赠表
 * </p>
 *
 * @author 自动生成
 * @since 2021-06-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("coupon_give")
@ApiModel(value="CouponGiveEntity对象", description="劵转赠表")
public class CouponGiveEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "优惠券ID")
    @TableId(value = "coupon_id", type = IdType.INPUT)
    private Long couponId;

    @ApiModelProperty(value = "券活动id")
    private Long couponThemeId;

    @ApiModelProperty(value = "转赠类型 1赠送 2转让")
    private Integer giveType;

    @ApiModelProperty(value = "转赠人id")
    private String giveUserId;

    @ApiModelProperty(value = "转赠人名称")
    private String giveUserName;

    @ApiModelProperty(value = "转赠人手机号")
    private String giveUserMobile;

    @ApiModelProperty(value = "转赠时间")
    private Date giveTime;

    @ApiModelProperty(value = "接受人id")
    private String receiveUserId;

    @ApiModelProperty(value = "接受手机号")
    private String receiveUserMobile;

    @ApiModelProperty(value = "接收人获得的新的券id")
    private Long receiveCouponId;

    @ApiModelProperty(value = "接收人用户类型,0是会员,1是机构经纪人,2是C端用户")
    private Integer receiveUserType;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    private Long createUserid;

    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;

    private Long updateUserid;

    @Version
    @TableField(fill = FieldFill.INSERT)
    private Integer versionNo;

    @ApiModelProperty(value = "逻辑删除字段 0 正常 1 已删除")
    @TableLogic
    private Integer isDeleted;


}
