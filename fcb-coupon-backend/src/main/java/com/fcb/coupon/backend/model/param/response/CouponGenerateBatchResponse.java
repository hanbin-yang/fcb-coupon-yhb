package com.fcb.coupon.backend.model.param.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class CouponGenerateBatchResponse implements Serializable {

    @ApiModelProperty(value = "券生成批次")
    private Long id;

    @ApiModelProperty(value = "券生成类型 0: 批次生券， 为线下 1：批次发券 - 批量导入, 2：批次发券 - 优惠券手动批量导入，3：导入券码")
    private Integer type;

    @ApiModelProperty(value = "券生成类型名称")
    private String typeName;

    @ApiModelProperty(value = "生券张数")
    private Integer generateNums;

    @ApiModelProperty(value = "批量导入附件")
    private String uploadFile;

    @ApiModelProperty(value = "任务创建事时间")
    private Date createTime;

    @ApiModelProperty(value = "任务完成时间")
    private Date finishTime;

    @ApiModelProperty(value = "发券状态（0：发送中，1：发送完成）")
    private Integer sendCouponStatus;

    @ApiModelProperty(value = "总记录数")
    private Integer totalRecord;

    @ApiModelProperty(value = "成功发送记录数")
    private Integer successRecord;

    @ApiModelProperty(value = "失败发送记录数")
    private Integer failRecord;

    @ApiModelProperty(value = "失败原因")
    private String failReason;

    @ApiModelProperty(value = "任务类型")
    private String moduleName;
}
