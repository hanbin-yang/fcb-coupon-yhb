package com.fcb.coupon.backend.model.param.request;

import cn.hutool.core.bean.BeanUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fcb.coupon.backend.model.ao.DeleteOrgAo;
import com.fcb.coupon.backend.model.bo.MktUseRuleDeleteOrgBo;
import com.fcb.coupon.common.dto.AbstractBaseConvertor;
import com.fcb.coupon.common.dto.AuthorityHolder;
import com.fcb.coupon.common.enums.MktUseRuleInputType;
import com.fcb.coupon.common.enums.MktUseRuleTypeEnum;
import com.fcb.coupon.common.dto.UserInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author HanBin_Yang
 * @since 2021/6/21 17:41
 */
@ApiModel(description = "删除组织 入参")
@Data
public class MktUseRuleDeleteOrgRequest extends AbstractBaseConvertor<MktUseRuleDeleteOrgBo> implements Serializable {
    private static final long serialVersionUID = -5008055779324454784L;

    @ApiModelProperty(value = "券活动主键", required = true)
    @NotNull(message = "券活动id不能为空")
    @JsonProperty(value = "themeRef")
    private Long couponThemeId;

    @ApiModelProperty(value = "组织范围 2店铺 1:商家 3:集团", example = "3", required = true)
    @NotNull(message = "组织范围不能为空")
    @Max(value = 3, message = "merchantType不得大于3")
    @Min(value = 1, message = "merchantType不得小于1")
    @JsonProperty(value = "merchantType")
    private Integer orgType;

    @ApiModelProperty(value = "要删除的组织的集合", required = true)
    @NotEmpty(message = "merchantAddList不能没有数据")
    @JsonProperty(value = "merchantAddList")
    private List<DeleteOrgAo> deleteOrgList;

    @Override
    public MktUseRuleDeleteOrgBo convert() {
        MktUseRuleDeleteOrgBo bo = new MktUseRuleDeleteOrgBo();
        BeanUtil.copyProperties(this, bo);

        // 前端入参的merchantType(orgType) 和 mkt_use_rule表 rule_type字段的不一致 这里转换下
        MktUseRuleInputType inputType = MktUseRuleInputType.of(this.orgType);
        String desc = inputType.getDesc();
        Integer type = MktUseRuleTypeEnum.getTypeByDesc(desc);
        bo.setRuleType(type);

        UserInfo userInfo = AuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        bo.setUserId(userInfo.getUserId());
        bo.setUsername(userInfo.getUsername());
        return bo;
    }
}
