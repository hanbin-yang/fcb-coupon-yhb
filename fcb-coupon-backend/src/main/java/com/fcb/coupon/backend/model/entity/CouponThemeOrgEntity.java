package com.fcb.coupon.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * <p>
 * 优惠券所属组织商家关联表
 * </p>
 *
 * @author 自动生成
 * @since 2021-06-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("coupon_theme_org")
@ApiModel(value="CouponThemeOrgEntity对象", description="优惠券所属组织商家关联表")
public class CouponThemeOrgEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "关联的优惠券ID")
    private Long couponThemeId;

    @ApiModelProperty(value = "关联商家ID")
    private Long orgId;

    private String orgLevelCode;

    private Long createUserid;

    private String createUsername;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    private Long updateUserid;

    private String updateUsername;

    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;

    @Version
    @TableField(fill = FieldFill.INSERT)
    private Integer versionNo;

    @ApiModelProperty(value = "逻辑删除字段 0 正常 1 已删除")
    @TableLogic
    private Integer isDeleted;


}
