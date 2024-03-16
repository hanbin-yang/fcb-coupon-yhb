package com.fcb.coupon.app.model.param.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fcb.coupon.app.serializer.DateToLongSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author 唐陆军
 * @Description 优惠券列表响应参数
 * @createTime 2021年08月27日 15:00:00
 */
@Data
public class CouponListResponse implements Serializable {

    @ApiModelProperty(value = "优惠券Id")
    private Long couponId;

    @ApiModelProperty(value = "优惠券code")
    private String couponCode;

    @ApiModelProperty(value = "优惠券主题类型")
    private Integer themeType;

    @ApiModelProperty(value = "优惠券名称")
    private String themeTitle;

    @JsonSerialize(using = DateToLongSerializer.class)
    @ApiModelProperty(value = "优惠券生效时间")
    private Date startTime;

    @JsonSerialize(using = DateToLongSerializer.class)
    @ApiModelProperty(value = "优惠券最迟使用时间")
    private Date endTime;

    @ApiModelProperty(value = "优惠券使用状态")
    private Integer status;

    @ApiModelProperty(value = "券优惠类型 0 按金额 1 按折扣")
    private Integer couponDiscountType;

    @ApiModelProperty(value = "券金额、折扣")
    private BigDecimal couponValue;

    @ApiModelProperty(value = "使用规则")
    private String useRuleRemark;

    @JsonSerialize(using = DateToLongSerializer.class)
    @ApiModelProperty(value = "优惠券绑定时间")
    private Date bindTime;

    @ApiModelProperty(value = "优惠券类型")
    private Integer couponType;

    @ApiModelProperty(value = "适用人群")
    private List<Integer> crowdScopeIds;
}
