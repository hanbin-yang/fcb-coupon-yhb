package com.fcb.coupon.backend.model.param.request;

import cn.hutool.core.bean.BeanUtil;
import com.fcb.coupon.backend.model.bo.PostponeCouponBo;
import com.fcb.coupon.common.dto.AbstractBaseConvertor;
import com.fcb.coupon.common.dto.AuthorityHolder;
import com.fcb.coupon.common.dto.UserInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 优惠券延期请求
 *
 * @Author WeiHaiQi
 * @Date 2021-06-23 8:40
 **/
@Data
public class PostponeCouponRequest extends AbstractBaseConvertor<PostponeCouponBo> implements Serializable {

    @ApiModelProperty(value = "优惠券ID列表", required = true)
    @NotNull
    private List<Long> idList;

    @ApiModelProperty(value = "延期日期")
    @NotNull
    private Date postponeDate;

    @Override
    public PostponeCouponBo convert() {
        PostponeCouponBo bo = new PostponeCouponBo();
        BeanUtil.copyProperties(this, bo);

        //设置登录用户信息
        UserInfo userInfo = AuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        if (Objects.nonNull(userInfo)) {
            bo.loadUserInfo(userInfo);
        }
        return bo;
    }
}
