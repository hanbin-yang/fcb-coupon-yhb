package com.fcb.coupon.client.backend.param.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

@Data
public class CouponThemeIdsCrmRequest implements Serializable {

    @Valid
    @NotNull(message = "参数错误")
    private CouponThemeIdsData data;

    @Data
    public static class CouponThemeIdsData implements Serializable {

        @NotNull(message = "券活动Id不能为空")
        @Size(min = 1, message = "券活动Id不能为空")
        @ApiModelProperty(value = "券活动Id列表")
        private List<Long> themeIds;
    }
}
