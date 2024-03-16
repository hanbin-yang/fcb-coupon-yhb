package com.fcb.coupon.app.model.param.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fcb.coupon.app.serializer.DateToLongSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author YangHanBin
 * @date 2021-08-13 9:02
 */
@Data
@ApiModel(description = "转增-领取福利 出参")
public class ReceiveBeforeGivingResponse implements Serializable {
    @ApiModelProperty(value = "劵赠送的主键id")
    private Long id;

    @ApiModelProperty(value = "券主键")
    private Long couponId;

    @ApiModelProperty(value = "提示文案")
    private String content;

    @ApiModelProperty(value = "赠送者头像地址")
    private String giveAvatar;

    @ApiModelProperty(value = "赠送者昵称/手机号 脱敏数据")
    private String giveUsernameOrMobile;

    @ApiModelProperty(value = "领取时效的终止时间")
    @JsonFormat(timezone = "GMT+8")
    @JsonSerialize(using = DateToLongSerializer.class)
    private Date expireTime;

    @ApiModelProperty(value = "优惠券名称")
    private String couponName;

    @ApiModelProperty(value = "折扣/金额 值")
    private String discountAmount;

    @ApiModelProperty(value = "折扣类型 0金额 1折扣")
    private Integer discountType;

    @ApiModelProperty(value = "券有效时间")
    @JsonFormat(timezone = "GMT+8")
    @JsonSerialize(using = DateToLongSerializer.class)
    private Date endTime;
}

