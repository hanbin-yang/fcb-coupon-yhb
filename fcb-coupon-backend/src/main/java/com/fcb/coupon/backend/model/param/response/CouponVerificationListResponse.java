package com.fcb.coupon.backend.model.param.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 营销中心->优惠券管理->优惠券活动列表 出参
 * @author mashiqiong
 * @date 2021-06-16 20:59
 */
@ApiModel(value="优惠券列表",description="优惠券列表")
@Data
public class CouponVerificationListResponse implements Serializable {
    private static final long serialVersionUID = -1698868954971347739L;

    @ApiModelProperty(value = "优惠券ID")
    private Long id;

    @ApiModelProperty(value = "券活动ID")
    private Long couponThemeId;

    @ApiModelProperty(value = "优惠券名称")
    private String themeTitle;

    @ApiModelProperty(value = "优惠券价值 ？折 或 ？元")
    private String couponAmount;

    @ApiModelProperty(value = "券码")
    private String couponCode;

    @ApiModelProperty(value = "券类型 0电子券 1实体券/预制券 2红包券 3：第三方券码")
    private Integer couponType;

    @ApiModelProperty(value = "订单号")
    private String orderCode;

    @ApiModelProperty(value = "生券时间")
    private Date createTime;

    @ApiModelProperty(value = "生效时间")
    private Date startTime;

    @ApiModelProperty(value = "失效时间")
    private Date endTime;

    @ApiModelProperty(value = "使用人群ids,0是会员,1是机构经纪人,2是C端用户")
    private Integer crowdScopeId;

    @ApiModelProperty(value = "使用人群成翻译成中文形式")
    private String crowdScopedIdStr;

    @ApiModelProperty(value = "绑定手机号")
    private String cellNo;

    @ApiModelProperty(value = "领取时间")
    private Date bindTime;

    @ApiModelProperty(value = "核销时间")
    private Date usedTime;

    @ApiModelProperty(value = "店铺名称")
    private String usedStoreName;

    @ApiModelProperty(value = "核销人")
    private String updateUsername;
}
