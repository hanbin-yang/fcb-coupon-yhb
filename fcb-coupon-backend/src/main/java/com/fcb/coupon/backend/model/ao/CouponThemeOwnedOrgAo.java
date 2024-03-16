package com.fcb.coupon.backend.model.ao;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * 所属商家 入参
 * @author YangHanBin
 * @date 2021-06-15 10:07
 */
@ApiModel(description = "券活动 所属商家 入参")
@Data
public class CouponThemeOwnedOrgAo implements Serializable {
    private static final long serialVersionUID = -6544148902039487642L;
    @ApiModelProperty(value = "组织id", required = true)
    @NotNull
    private Long orgId;

    @ApiModelProperty(value = "组织名称", required = true)
    @NotNull
    private String orgName;

    @ApiModelProperty(value = "组织级别Code", required = true)
    @NotNull
    private String orgLevelCode;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CouponThemeOwnedOrgAo that = (CouponThemeOwnedOrgAo) o;
        return Objects.equals(orgId, that.orgId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orgId);
    }
}
