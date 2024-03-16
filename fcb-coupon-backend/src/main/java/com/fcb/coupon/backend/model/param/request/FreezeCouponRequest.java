package com.fcb.coupon.backend.model.param.request;

import cn.hutool.core.bean.BeanUtil;
import com.fcb.coupon.backend.model.bo.FreezeCouponBo;
import com.fcb.coupon.common.dto.AbstractBaseConvertor;
import com.fcb.coupon.common.dto.AuthorityHolder;
import com.fcb.coupon.common.dto.UserInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 冻结/解冻优惠券接口入参
 *
 * @Author WeiHaiQi
 * @Date 2021-06-22 17:01
 **/
@Data
public class FreezeCouponRequest extends AbstractBaseConvertor<FreezeCouponBo> implements Serializable {

    @ApiModelProperty(value = "优惠券ID列表", required = true)
    @NotNull
    private List<Long> idList;

    @ApiModelProperty(value = "是否冻结", required = true)
    @NotNull
    private Boolean freeze;

    @Override
    public FreezeCouponBo convert() {
        FreezeCouponBo bo = new FreezeCouponBo();
        BeanUtil.copyProperties(this, bo);

        //设置登录用户信息
        UserInfo userInfo = AuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        if (Objects.nonNull(userInfo)) {
            bo.loadUserInfo(userInfo);
        }
        return bo;
    }
}
