package com.fcb.coupon.backend.model.param.request;

import cn.hutool.core.bean.BeanUtil;
import com.fcb.coupon.backend.model.bo.CouponThemeInitiativeMarketingBo;
import com.fcb.coupon.common.dto.AbstractBaseConvertor;
import com.fcb.coupon.common.dto.AuthorityHolder;
import com.fcb.coupon.common.dto.UserInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;
import java.util.TreeSet;

/**
 * 营销中心->主动营销->营销任务管理->编辑任务流->添加优惠券->查询优惠券列表 入参
 * @author mashiqiong
 * @date 2021-06-17 20:59
 */
@ApiModel(description = "查询主动营销券活动列表-入参")
@Data
public class CouponThemeInitiativeMarketingRequest extends AbstractBaseConvertor<CouponThemeInitiativeMarketingBo> implements Serializable {

    @ApiModelProperty(value = "券活动ID", dataType = "Long")
    private Long id;

    @ApiModelProperty(value = "优惠券名称", dataType = "String")
    private String themeTitle;

    @ApiModelProperty(value = "发券类型(17:主动营销券)", dataType = "Integer")
    private Integer couponGiveRule;

    @ApiModelProperty(value = "状态，4 进行中(活动已开始且活动时间在有效期内的券)", dataType = "Integer")
    private Integer status;

    @ApiModelProperty(value = "适用人群 0会员 1机构经纪人 2C端用户", dataType = "TreeSet<Integer>")
    private TreeSet<Integer> crowdScopeIds;

    @Override
    public CouponThemeInitiativeMarketingBo convert() {
        CouponThemeInitiativeMarketingBo bo = new CouponThemeInitiativeMarketingBo();
        BeanUtil.copyProperties(this, bo);

        //设置登录用户信息
        UserInfo userInfo = AuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        if (Objects.nonNull(userInfo)) {
            bo.loadUserInfo(userInfo);
        }
        return bo;
    }
}
