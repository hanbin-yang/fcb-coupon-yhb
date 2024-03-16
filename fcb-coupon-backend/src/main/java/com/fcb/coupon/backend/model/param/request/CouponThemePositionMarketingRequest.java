package com.fcb.coupon.backend.model.param.request;

import cn.hutool.core.bean.BeanUtil;
import com.fcb.coupon.backend.model.ao.OrgRangeAo;
import com.fcb.coupon.backend.model.bo.CouponThemePositionMarketingBo;
import com.fcb.coupon.common.dto.AbstractBaseConvertor;
import com.fcb.coupon.common.dto.AuthorityHolder;
import com.fcb.coupon.common.dto.UserInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

/**
 * 营销中心->优惠券管理->优惠券运营位管理->添加优惠券->查询优惠券列表 入参
 * @author mashiqiong
 * @date 2021-06-17 20:59
 */
@ApiModel(description = "查询运营位券活动列表-入参")
@Data
public class CouponThemePositionMarketingRequest extends AbstractBaseConvertor<CouponThemePositionMarketingBo> implements Serializable {

    @ApiModelProperty(value = "券活动IDs", dataType = "List<Long>")
    private List<Long> ids;

    @ApiModelProperty(value = "优惠券名称", dataType = "String")
    private String themeTitle;

    @ApiModelProperty(value = "发券类型(4:前台领券)", dataType = "Integer")
    private Integer couponGiveRule;

    @ApiModelProperty(value = "状态，24:进行中(活动时间未开始或者活动时间在有效期内的券)", dataType = "Integer")
    private Integer status;

    @ApiModelProperty(value = "适用人群 0会员 1机构经纪人 2C端用户", dataType = "TreeSet<Integer>")
    private TreeSet<Integer> crowdScopeIds;

    @ApiModelProperty(value = "发布范围", dataType = "List<OrgRangeDto>")
    private List<OrgRangeAo> rangeList;

    @ApiModelProperty(value = "发布范围获取规则 0向上共有的 1向下递归的 2向上共有的和向下递归的", dataType = "Integer")
    private Integer rangeRuleType;

    @ApiModelProperty(value = "是否还需要额外查询可赠送的券活动 true是", dataType = "Boolean")
    private Boolean hasCanDonation;

    @Override
    public CouponThemePositionMarketingBo convert() {
        CouponThemePositionMarketingBo bo = new CouponThemePositionMarketingBo();
        BeanUtil.copyProperties(this, bo);

        //设置登录用户信息
        UserInfo userInfo = AuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        if (Objects.nonNull(userInfo)) {
            bo.loadUserInfo(userInfo);
        }
        return bo;
    }
}
