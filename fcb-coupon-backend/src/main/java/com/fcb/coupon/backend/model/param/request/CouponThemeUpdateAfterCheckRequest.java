package com.fcb.coupon.backend.model.param.request;

import cn.hutool.core.bean.BeanUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fcb.coupon.backend.model.bo.CouponThemeUpdateAfterCheckBo;
import com.fcb.coupon.common.dto.AbstractBaseConvertor;
import com.fcb.coupon.common.dto.AuthorityHolder;
import com.fcb.coupon.common.dto.UserInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @author YangHanBin
 * @date 2021-06-16 17:06
 */
@Data
@ApiModel(description = "更新规则--入参")
public class CouponThemeUpdateAfterCheckRequest extends AbstractBaseConvertor<CouponThemeUpdateAfterCheckBo> implements Serializable {
    private static final long serialVersionUID = -1139191863619727284L;

    @ApiModelProperty(value = "券活动ID", required = true, dataType = "Long")
    @JsonProperty(value = "id")
    @NotNull(message = "券活动id不能为空")
    private Long couponThemeId;

    @ApiModelProperty(value = "券活动结束时间", required = true, dataType = "Date")
    private Date endTime;

    @ApiModelProperty(value = "券活动使用说明", required = true, dataType = "String")
    private String themeDesc;

    @ApiModelProperty(value = "券码有效期 结束时间", required = true, dataType = "Date")
    private Date endTimeConfig;

    @Override
    public CouponThemeUpdateAfterCheckBo convert() {
        CouponThemeUpdateAfterCheckBo bo = new CouponThemeUpdateAfterCheckBo();
        BeanUtil.copyProperties(this, bo);

        UserInfo userInfo = AuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        bo.setUserId(userInfo.getUserId());
        bo.setUsername(userInfo.getUsername());
        bo.setUserOrgLevelCode(userInfo.getOrgLevelCode());
        return bo;
    }
}
