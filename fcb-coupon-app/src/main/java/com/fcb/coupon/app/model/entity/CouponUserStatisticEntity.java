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
 * 用户领券统计表
 * </p>
 *
 * @author 自动生成
 * @since 2021-07-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("coupon_user_statistic")
@ApiModel(value="CouponUserStatisticEntity对象", description="用户领券统计表")
public class CouponUserStatisticEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "券活动id")
    private Long couponThemeId;

    @ApiModelProperty(value = "绑定用户id")
    private String userId;

    @ApiModelProperty(value = "用户类型：0会员 1机构经纪人 2C端用户")
    private Integer userType;

    @ApiModelProperty(value = "总领券数量")
    private Integer totalCount;

    @ApiModelProperty(value = "当月领券数量")
    private Integer monthCount;

    @ApiModelProperty(value = "当天领券数量")
    private Integer todayCount;

    @ApiModelProperty(value = "最后领券日期")
    private Date lastReceiveDate;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;

    @TableField(fill = FieldFill.INSERT)
    @Version
    private Integer versionNo;

    @ApiModelProperty(value = "逻辑删除字段 0 正常 1 已删除")
    @TableLogic
    private Integer isDeleted;

    // ********* 以下数据库不存在的字段 *********//
    /**
     * 领券数量
     */
    @TableField(exist = false)
    private int receiveCount;
    /**
     * 总领券限制
     */
    @TableField(exist = false)
    private Integer individualLimit;

    /**
     * 每月领券限制
     */
    @TableField(exist = false)
    private Integer monthLimit;

    /**
     * 每天领券限制
     */
    @TableField(exist = false)
    private Integer dayLimit;
}
