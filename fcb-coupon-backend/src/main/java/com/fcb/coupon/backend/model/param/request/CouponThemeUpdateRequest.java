package com.fcb.coupon.backend.model.param.request;

import cn.hutool.core.bean.BeanUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fcb.coupon.backend.model.ao.CouponThemeOwnedOrgAo;
import com.fcb.coupon.backend.model.bo.CouponThemeUpdateBo;
import com.fcb.coupon.common.dto.AbstractBaseConvertor;
import com.fcb.coupon.common.dto.AuthorityHolder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

/**
 * @author YangHanBin
 * @date 2021-06-15 17:48
 */
@Data
@ApiModel(description = "修改券活动-入参")
@Slf4j
public class CouponThemeUpdateRequest extends AbstractBaseConvertor<CouponThemeUpdateBo> implements Serializable {
    private static final long serialVersionUID = 4416371891373951096L;

    @ApiModelProperty(value = "券活动主键", required = true)
    @NotNull
    private Long id;

    @ApiModelProperty(value = "发券类型(1:活动规则券,19:线下预制券,4:前台领券,17:主动营销券,18:权益优惠券,19:线下预制券,20:媒体广告券,21:直播券,22:营销活动页券)", required = true)
    private Integer couponGiveRule;

    @ApiModelProperty(value = "所属商家", required = true)
    private List<CouponThemeOwnedOrgAo> orgList;

    @ApiModelProperty(value = "费用归属", required = true)
    private List<CouponThemeOwnedOrgAo> belongingOrgList;

    @ApiModelProperty(value = "每ID总共可以领取", required = true)
    private Integer individualLimit;

    @ApiModelProperty(value = "每ID总共可以领取", required = true)
    private Integer everyDayLimit;

    @ApiModelProperty(value = "每个ID每月可以领取", required = true)
    private Integer everyMonthLimit;

    @ApiModelProperty(value = "优惠券名称", required = true)
    private String themeTitle;

    @ApiModelProperty(value = "券活动名称", required = true)
    private String activityName;

    @ApiModelProperty(value = "券码生成方式 0自动生成 3：第三方券码", required = true)
    private Integer couponType;

    @ApiModelProperty(value = "优惠方式 0：金额 1：折扣 11：福利卡 12：红包券", required = true)
    private Integer couponDiscountType;

    @ApiModelProperty(value = "适用范围 0：平台券  11：商家券 5：集团券 21：店铺券", required = true)
    @JsonProperty(value = "applicationScope")
    private Integer themeType;

    @ApiModelProperty(value = "优惠券面值")
    private BigDecimal couponAmount;

    @ApiModelProperty(value = "优惠券折扣 7折为70")
    private Integer couponDiscount;

    @ApiModelProperty(value = "折扣上限 ?元")
    private BigDecimal couponAmountExt;

    @ApiModelProperty(value = "发行总量", required = true)
    private Integer totalLimit;

    @ApiModelProperty(value = "券活动日期 开始", required = true)
    private Date startTime;

    @ApiModelProperty(value = "券活动日期 结束", required = true)
    private Date endTime;

    @ApiModelProperty(value = "使用说明", required = true)
    private String themeDesc;

    @ApiModelProperty(value = "使用人群 格式：{ids:[0,1]} 0会员 1机构经济人 2C端用户")
    private TreeSet<Integer> crowdIds;

    @ApiModelProperty(value = "券码有效期类型 1：固定有效期，2：从领用开始计算")
    private Integer effdateCalcMethod;

    @ApiModelProperty(value = "固定有效时间 开始")
    private Date startTimeConfig;

    @ApiModelProperty(value = "固定有效时间 结束")
    private Date endTimeConfig;

    @ApiModelProperty(value = "自用户领取?天后失效")
    private Integer effDays;

    @ApiModelProperty(value = "使用条件 订单满?元")
    private BigDecimal useLimit;

    @ApiModelProperty(value = "每个订单号每次最多可使用")
    private Integer orderUseLimit;

    @ApiModelProperty(value = "是否可赠送", required = true)
    private Integer canDonation;

    @ApiModelProperty(value = "是否可转让", required = true)
    private Integer canAssign;

    @Override
    public CouponThemeUpdateBo convert() {
        CouponThemeUpdateBo bo = new CouponThemeUpdateBo();
        BeanUtil.copyProperties(this, bo);
        bo.setCouponThemeId(this.getId());
        // 费用归属转换
        List<CouponThemeOwnedOrgAo> ownedOrgAoList = this.getBelongingOrgList();
        CouponThemeOwnedOrgAo ao = ownedOrgAoList.get(0);
        bo.setBelongingOrgId(ao.getOrgId());
        // 适用人群转换
        TreeSet<Integer> crowdIds = this.getCrowdIds();
        String s = crowdIds.toString();
        String scopeIds = s.replaceAll("\\s", "");
        scopeIds = "{\"ids\":" + scopeIds + "}";
        bo.setCrowdScopeIds(scopeIds);

        //设置登录用户信息
        AuthorityHolder authorityHolder = AuthorityHolder.AuthorityThreadLocal.get();
        if (Objects.nonNull(authorityHolder)) {
            bo.loadUserInfo(authorityHolder.getUserInfo());
        } else {
            log.warn("获取authorityHolder null, 用户未登录操作接口！");
        }
        return bo;
    }
}
