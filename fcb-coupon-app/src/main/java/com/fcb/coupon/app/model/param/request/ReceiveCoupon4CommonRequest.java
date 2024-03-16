package com.fcb.coupon.app.model.param.request;

import com.fcb.coupon.app.infra.inteceptor.AppAuthorityHolder;
import com.fcb.coupon.app.infra.inteceptor.AppUserInfo;
import com.fcb.coupon.app.model.bo.CouponReceiveBo;
import com.fcb.coupon.common.dto.AbstractBaseConvertor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author YangHanBin
 * @date 2021-08-17 10:20
 */
@Data
@ApiModel(description = "通用领券--入参")
public class ReceiveCoupon4CommonRequest extends AbstractBaseConvertor<CouponReceiveBo> implements Serializable {
    @ApiModelProperty("券活动Id, 加密后的数据")
    @NotBlank(message = "券活动id不能为空")
    private String couponThemeId;

    @ApiModelProperty("券来源 4主动领券")
    private Integer source;

    /** 来源id：【转赠】-会员手机号\机构账号、【前台领券】-楼盘id、【直播领券】-直播间id、【媒体广告领券】-广告批次号、【营销活动页领券】-页面id **/
    @ApiModelProperty("来源id")
    @NotBlank(message = "来源id不能为空")
    private String sourceId;

    @Override
    public CouponReceiveBo convert() {
        CouponReceiveBo bo = new CouponReceiveBo();
        bo.setSource(this.source);
        bo.setSourceId(this.sourceId);
        bo.setCouponThemeId(Long.parseLong(this.couponThemeId));
        bo.setReceiveCount(1);

        AppUserInfo userInfo = AppAuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        bo.setUserId(userInfo.getUserId());
        bo.setUserMobile(userInfo.getUserMobile());
        bo.setUserType(userInfo.getUserType());

        return bo;
    }
}
