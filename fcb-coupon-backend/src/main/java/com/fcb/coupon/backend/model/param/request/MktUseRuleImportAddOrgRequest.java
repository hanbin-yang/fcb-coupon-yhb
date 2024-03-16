package com.fcb.coupon.backend.model.param.request;

import cn.hutool.core.bean.BeanUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fcb.coupon.backend.model.bo.MktUseRuleImportAddOrgBo;
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
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author HanBin_Yang
 * @since 2021/6/21 20:42
 */
@ApiModel(description = "删除组织 入参")
@Data
public class MktUseRuleImportAddOrgRequest extends AbstractBaseConvertor<MktUseRuleImportAddOrgBo> implements Serializable {
    private static final long serialVersionUID = 9097240117828072742L;

    @ApiModelProperty(value = "券活动主键", required = true)
    @NotNull(message = "券活动id不能为空")
    @JsonProperty(value = "themeRef")
    private Long themeRef;

    @ApiModelProperty(value = "组织范围 2店铺 1:商家 3:集团", example = "3", required = true)
    @NotNull(message = "组织范围不能为空")
    @Max(value = 3, message = "merchantType不得大于3")
    @Min(value = 1, message = "merchantType不得小于1")
    private Integer merchantType;

    @Override
    public MktUseRuleImportAddOrgBo convert() {
        MktUseRuleImportAddOrgBo bo = new MktUseRuleImportAddOrgBo();
        bo.setCouponThemeId(this.themeRef);

        // 前端入参的merchantType(orgType) 和 mkt_use_rule表 rule_type字段的不一致 这里转换下
        MktUseRuleInputType inputType = MktUseRuleInputType.of(this.merchantType);
        String desc = inputType.getDesc();
        Integer type = MktUseRuleTypeEnum.getTypeByDesc(desc);
        bo.setRuleType(type);

        AuthorityHolder authorityHolder = AuthorityHolder.AuthorityThreadLocal.get();
        if (Objects.nonNull(authorityHolder)) {
            UserInfo userInfo = authorityHolder.getUserInfo();
            bo.setUserId(userInfo.getUserId());
            bo.setUsername(userInfo.getUsername());
            bo.setUt(userInfo.getUt());
        }
        return bo;
    }
}
