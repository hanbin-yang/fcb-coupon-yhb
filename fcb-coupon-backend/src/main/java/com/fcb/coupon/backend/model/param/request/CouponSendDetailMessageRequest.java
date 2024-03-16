package com.fcb.coupon.backend.model.param.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月16日 19:21:00
 */
@Data
public class CouponSendDetailMessageRequest implements Serializable {

    @ApiModelProperty(value = "券活动id")
    private Long couponThemeId;

    @ApiModelProperty(value = "发送用户信息")
    private List<CouponSendUserRequest> sendUsers;

//    @ApiModelProperty(value = "批次号")
//    private String batchNo;

    @ApiModelProperty(value = "来源id")
    private String sourceId;

    @ApiModelProperty(value = "用户类型(0=会员,1=机构经纪人,2=C端用户)")
    private Integer sendCouponUserType;

}
