package com.fcb.coupon.backend.model.param.request;

import cn.hutool.core.bean.BeanUtil;
import com.fcb.coupon.backend.model.bo.CouponOprLogQueryBo;
import com.fcb.coupon.common.dto.AbstractBaseConvertor;
import com.fcb.coupon.common.dto.AuthorityHolder;
import com.fcb.coupon.common.dto.UserInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * 查询优惠券操作日志请求参数
 *
 * @Author WeiHaiQi
 * @Date 2021-06-23 17:07
 **/
@Data
public class CouponOprLogQueryRequest extends AbstractBaseConvertor<CouponOprLogQueryBo> implements Serializable {
    private static final long serialVersionUID = -6375670319352805622L;

    @ApiModelProperty(value = "对应操作主体的ID", required = false)
    private Long oprRefId;

    @ApiModelProperty(value = "操作主体类型", required = false)
    private Integer oprThemeType;

    @ApiModelProperty(value = "页码", required = false)
    private int currentPage;

    @ApiModelProperty(value = "页大小", required = false)
    private int itemsPerPage;

    @Override
    public CouponOprLogQueryBo convert() {
        CouponOprLogQueryBo bo = new CouponOprLogQueryBo();
        BeanUtil.copyProperties(this, bo);

        //设置登录用户信息
        UserInfo userInfo = AuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        if (Objects.nonNull(userInfo)) {
            bo.loadUserInfo(userInfo);
        }
        return bo;
    }
}
