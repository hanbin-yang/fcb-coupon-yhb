package com.fcb.coupon.app.model.param.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fcb.coupon.app.model.bo.CouponReceiveBo;
import com.fcb.coupon.common.dto.AbstractBaseConvertor;
import com.fcb.coupon.common.enums.CouponSourceTypeEnum;
import com.fcb.coupon.common.enums.UserTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author YangHanBin
 * @date 2021-08-16 18:13
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "媒体广告领券--入参")
@Data
public class ReceiveCoupon4MediaRequest extends AbstractBaseConvertor<CouponReceiveBo> implements Serializable {
    @ApiModelProperty("券活动Id")
    @NotNull(message = "券活动id不能为空")
    @JsonProperty(value = "themeId")
    private Long couponThemeId;

    /** 来源id：【转赠】-会员手机号\机构账号、【前台领券】-楼盘id、【直播领券】-直播间id、【媒体广告领券】-广告批次号、【营销活动页领券】-页面id **/
    @ApiModelProperty("来源id, 媒体广告id")
    @NotBlank(message = "媒体广告来源id不能为空")
    private String sourceId;

    @ApiModelProperty("手机号码")
    @NotBlank(message = "手机号不能为空")
    private String cellNo;

    @Override
    public CouponReceiveBo convert() {
        CouponReceiveBo bo = new CouponReceiveBo();
        bo.setCouponThemeId(this.couponThemeId);
        bo.setSourceId(sourceId);
        bo.setSource(CouponSourceTypeEnum.COUPON_SOURCE_MEDIA_ADVERT.getSource());
        bo.setUserMobile(this.cellNo);
        bo.setUserType(UserTypeEnum.C.getUserType());
        bo.setReceiveCount(1);

        return bo;
    }
}
