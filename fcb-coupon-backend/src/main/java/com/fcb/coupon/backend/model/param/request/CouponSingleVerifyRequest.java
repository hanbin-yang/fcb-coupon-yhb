package com.fcb.coupon.backend.model.param.request;

import cn.hutool.core.bean.BeanUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fcb.coupon.backend.model.bo.CouponSingleVerifyBo;
import com.fcb.coupon.backend.model.bo.SingleVerifyBo;
import com.fcb.coupon.common.dto.AbstractBaseConvertor;
import com.fcb.coupon.common.dto.AuthorityHolder;
import com.fcb.coupon.common.dto.UserInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author HanBin_Yang
 * @since 2021/6/23 8:58
 */
@ApiModel(description = "单个券核销-入参")
@Data
@Slf4j
public class CouponSingleVerifyRequest extends AbstractBaseConvertor<SingleVerifyBo> implements Serializable {
    private static final long serialVersionUID = -8019973069288611251L;

    @ApiModelProperty(value = "券码", required = true)
    @NotBlank(message = "优惠券码不能为空")
    private String couponCode;

    @ApiModelProperty(value = "券码", required = true)
    @NotBlank(message = "优惠券码不能为空")
    @JsonProperty(value = "orderCode")
    private String subscribeCode;

    @ApiModelProperty(value = "核销手机号", required = true)
    @NotBlank(message = "核销手机号不能为空")
    private String bindTel;

    @JsonSerialize(using = ToStringSerializer.class)
    @NotNull(message = "核销店铺Id不能为空")
    private Long usedStoreId;

    @Override
    public SingleVerifyBo convert() {
        SingleVerifyBo bo = new SingleVerifyBo();
        BeanUtil.copyProperties(this, bo);

        UserInfo userInfo = AuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        bo.setVerifyUserId(userInfo.getUserId());
        bo.setVerifyUsername(userInfo.getUsername());

        return bo;
    }
}
