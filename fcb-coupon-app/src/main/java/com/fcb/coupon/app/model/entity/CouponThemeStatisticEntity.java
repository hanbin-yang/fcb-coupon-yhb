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
 * 劵活动统计表
 * </p>
 *
 * @author 自动生成
 * @since 2021-06-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("coupon_theme_statistic")
@ApiModel(value="CouponThemeStatisticEntity对象", description="劵活动统计表")
public class CouponThemeStatisticEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "券活动id")
    @TableId(value = "coupon_theme_id", type = IdType.INPUT)
    private Long couponThemeId;

    @ApiModelProperty(value = "当前券活动总可领取券数")
    private Integer totalCount;

    @ApiModelProperty(value = "当前券活动已生成券数")
    private Integer createdCount;

    @ApiModelProperty(value = "已发数量")
    private Integer sendedCount;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;

    @Version
    @TableField(fill = FieldFill.INSERT)
    private Integer versionNo;

    @ApiModelProperty(value = "逻辑删除字段 0 正常 1 已删除")
    @TableLogic
    private Integer isDeleted;


}
