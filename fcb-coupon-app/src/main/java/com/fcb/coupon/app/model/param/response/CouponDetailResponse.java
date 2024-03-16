package com.fcb.coupon.app.model.param.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月31日 10:49:00
 */
@Data
public class CouponDetailResponse extends CouponListResponse {

    @ApiModelProperty(value= "绑定账号类型")
    private String bindUserType;

    @ApiModelProperty(value= "是否可赠送")
    private Boolean canDonation;

    @ApiModelProperty(value= "是否可转让")
    private Boolean canAssign;

    @ApiModelProperty(value = "是否可以发送转赠通知短信")
    private Boolean canSendMsg;


    @ApiModelProperty(value= "券来源")
    private Integer source;

    @ApiModelProperty(value= "券来源描述")
    private String sourceStr;

    @ApiModelProperty(value="活动描述")
    private String themeDesc;

    @ApiModelProperty(value="优惠券主题Id")
    private Long themeId;
}
