package com.fcb.coupon.backend.model.param.response;

import com.fcb.coupon.common.excel.annotation.XCell;
import com.fcb.coupon.common.excel.annotation.XSheet;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 转让优惠券明细Excel导出对象
 *
 * @Author WeiHaiQi
 * @Date 2021-06-25 20:26
 **/
@XSheet(name = "转让优惠券明细")
@ApiModel(value="转让优惠券明细",description="转让优惠券明细")
@Data
public class CouponAssignExportResponse implements Serializable {
    private static final long serialVersionUID = 6790305321147818911L;

    @XCell(name = "优惠券活动ID")
    @ApiModelProperty(value = "优惠券活动ID")
    private String couponThemeId;
    @XCell(name = "优惠券名称")
    @ApiModelProperty(value = "优惠券名称")
    private String themeTitle;
    @XCell(name = "券码")
    @ApiModelProperty(value = "券码")
    private String couponCode;
    @XCell(name = "第三方平台券码")
    @ApiModelProperty(value = "第三方平台券码")
    private String thirdCouponCode;
    @XCell(name = "所属商家")
    @ApiModelProperty(value = "所属商家")
    private String orgNames;
    @XCell(name = "生券日期")
    @ApiModelProperty(value = "生券日期")
    private String createTime;
    @XCell(name = "有效时间")
    @ApiModelProperty(value = "有效时间")
    private String validTimeStr;
    @XCell(name = "绑定账号类型")
    @ApiModelProperty(value = "绑定账号类型")
    private String couponUserType;
    @XCell(name = "绑定手机号")
    @ApiModelProperty(value = "绑定手机号")
    private String cellNo;
    @XCell(name = "状态")
    @ApiModelProperty(value = "状态")
    private String status;
    @XCell(name = "绑券时间")
    @ApiModelProperty(value = "绑券时间")
    private String bindTime;
    @XCell(name = "转让账号类型")
    @ApiModelProperty(value = "转让账号类型")
    private String donateAccountType;
    @XCell(name = "转让时间")
    @ApiModelProperty(value = "转让时间")
    private String donateTimeName;
    @XCell(name = "转让手机号")
    @ApiModelProperty(value = "转让手机号")
    private String donateMobile;
}
