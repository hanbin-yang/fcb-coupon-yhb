package com.fcb.coupon.backend.model.param.request;

import cn.hutool.core.bean.BeanUtil;
import com.fcb.coupon.backend.model.bo.CouponThemeDetailBo;
import com.fcb.coupon.common.dto.AbstractBaseConvertor;
import com.fcb.coupon.common.dto.AuthorityHolder;
import com.fcb.coupon.common.dto.UserInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * 营销中心->优惠券管理->查看惠券活动详情 入参
 * @author mashiqiong
 * @date 2021-06-23 9:56
 */
@ApiModel(description = "查看优惠券活动详情 -入参")
@Data
public class CouponThemeDetailRequest extends AbstractBaseConvertor<CouponThemeDetailBo> implements Serializable {

    @ApiModelProperty(value = "券活动ID", dataType = "Long", required = true)
    private Long id;

    @Override
    public CouponThemeDetailBo convert() {
        CouponThemeDetailBo bo = new CouponThemeDetailBo();
        BeanUtil.copyProperties(this, bo);

        //设置登录用户信息
        UserInfo userInfo = AuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        if (Objects.nonNull(userInfo)) {
            bo.loadUserInfo(userInfo);
        }
        return bo;
    }
}
