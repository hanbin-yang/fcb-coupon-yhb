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
 * 规则设置表
 * </p>
 *
 * @author 自动生成
 * @since 2021-06-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("mkt_use_rule")
@ApiModel(value="MktUseRuleEntity对象", description="规则设置表")
public class MktUseRuleEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "6店铺 1商家 11集团")
    private Integer ruleType;

    @ApiModelProperty(value = "组织id,即orgId")
    private Long limitRef;

    @ApiModelProperty(value = "组织名称,即orgName")
    private String refDescription;

    @ApiModelProperty(value = "组织代码,即orgCode")
    private String extendRef;

    @ApiModelProperty(value = "券活动主键id")
    private Long themeRef;

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
