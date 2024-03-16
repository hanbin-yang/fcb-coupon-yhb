package com.fcb.coupon.backend.model.param.request;

import cn.hutool.core.bean.BeanUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fcb.coupon.backend.model.bo.GenerateCouponBo;
import com.fcb.coupon.common.dto.AbstractBaseConvertor;
import com.fcb.coupon.common.dto.AuthorityHolder;
import com.fcb.coupon.common.dto.UserInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author YangHanBin
 * @date 2021-06-18 11:53
 */
@ApiModel(description = "券活动-生券 入参")
@Data
public class GenerateCouponRequest extends AbstractBaseConvertor<GenerateCouponBo> implements Serializable {
    private static final long serialVersionUID = -1912327250393256064L;

    @ApiModelProperty(value = "券活动id", required = true, dataType = "long")
    @NotNull(message = "券活动Id不能为空")
    @JsonProperty(value = "id")
    private Long couponThemeId;

    @ApiModelProperty(value = "7线下发券", required = true, dataType = "int")
    private Integer source;

    @ApiModelProperty(value = "券活动id", required = true, dataType = "long")
    @NotNull(message = "生券数量不能为空")
    @Min(value = 1L, message = "生券数量不能小于1")
    private Integer generateAmount;

    @Override
    public GenerateCouponBo convert() {
        GenerateCouponBo bo = new GenerateCouponBo();
        BeanUtil.copyProperties(this, bo);

        UserInfo userInfo = AuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        bo.setUserId(userInfo.getUserId());
        bo.setUsername(userInfo.getUsername());
        return bo;
    }
}
