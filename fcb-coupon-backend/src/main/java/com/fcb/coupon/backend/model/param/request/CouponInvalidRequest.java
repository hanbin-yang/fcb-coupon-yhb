package com.fcb.coupon.backend.model.param.request;

import cn.hutool.core.bean.BeanUtil;
import com.fcb.coupon.backend.model.bo.CouponInvalidBo;
import com.fcb.coupon.common.dto.AbstractBaseConvertor;
import com.fcb.coupon.common.dto.AuthorityHolder;
import com.fcb.coupon.common.dto.UserInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 作废请求参数
 *
 * @Author WeiHaiQi
 * @Date 2021-06-19 7:51
 **/
@Data
@ApiModel(description = "作废请求参数")
public class CouponInvalidRequest extends AbstractBaseConvertor<CouponInvalidBo> implements Serializable {

    @ApiModelProperty(value = "优惠券ID集合", required = true)
    @NotNull
    private List<Long> idList;

    @Override
    public CouponInvalidBo convert() {
        CouponInvalidBo bo = new CouponInvalidBo();
        BeanUtil.copyProperties(this, bo);

        //设置登录用户信息
        UserInfo userInfo = AuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        if (Objects.nonNull(userInfo)) {
            bo.loadUserInfo(userInfo);
        }
        return bo;
    }
}
