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
 * 劵批次表
 * </p>
 *
 * @author 自动生成
 * @since 2021-06-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("coupon_generate_batch")
@ApiModel(value = "CouponGenerateBatchEntity对象", description = "劵批次表")
public class CouponGenerateBatchEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "券生成批次")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "券生成类型 0: 批次生券， 为线下 1：批次发券 - 批量导入, 2：批次发券 - 优惠券手动批量导入，3：导入券码")
    private Integer type;

    @ApiModelProperty(value = "券活动ID")
    private Long themeId;

    @ApiModelProperty(value = "生券张数")
    private Integer generateNums;

    @ApiModelProperty(value = "批量导入附件")
    private String uploadFile;

    @ApiModelProperty(value = "总记录数")
    private Integer totalRecord;

    @ApiModelProperty(value = "发券状态（0：发送中，1：发送完成）")
    private Integer sendCouponStatus;

    @ApiModelProperty(value = "成功发送记录数")
    private Integer successRecord;

    @ApiModelProperty(value = "失败发送记录数")
    private Integer failRecord;

    @ApiModelProperty(value = "发送完成时间")
    private Date finishTime;

    @ApiModelProperty(value = "批量下载附件")
    private String downloadFile;

    @ApiModelProperty(value = "失败原因")
    private String failReason;

    private Long createUserid;

    private String createUsername;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    private Long updateUserid;

    private String updateUsername;

    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;

    @TableField(fill = FieldFill.INSERT)
    @Version
    private Integer versionNo;

    @ApiModelProperty(value = "逻辑删除字段 0 正常 1 已删除")
    @TableLogic
    private Integer isDeleted;


}
