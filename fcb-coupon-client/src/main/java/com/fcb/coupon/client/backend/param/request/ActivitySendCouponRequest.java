package com.fcb.coupon.client.backend.param.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * @author 唐陆军
 * @Description 活动发券
 * @createTime 2021年06月09日 19:21:00
 */
@Data
public class ActivitySendCouponRequest implements Serializable {

    @Valid
    @NotNull(message = "发券参数不能为空")
    @ApiModelProperty(value = "发券参数")
    private ActivitySendCouponDataRequest data;

    @Data
    public static class ActivitySendCouponDataRequest implements Serializable {

        @NotBlank(message = "批次号不能为空")
        @ApiModelProperty(value = "批次号")
        private String batchNo;

        @NotNull(message = "活动ID不能为空")
        @ApiModelProperty(value = "活动ID")
        private Long couponThemeId;

        @NotNull(message = "活动ID不能为空")
        @ApiModelProperty(value = "发送用户类型")
        private Integer sendCouponUserType;

        @Size(message = "发送用户列表至少需要一条记录", min = 1)
        @NotNull(message = "发送用户列表不能为空")
        @ApiModelProperty(value = "发送用户列表")
        private List<ActivitySendCouponUserRequest> sendUsers;

        @NotNull(message = "来源不能为空")
        @ApiModelProperty(value = "来源")
        private Integer source;

        @ApiModelProperty(value = "来源id")
        private String sourceId;
    }

    @Data
    public class ActivitySendCouponUserRequest implements Serializable {


        @ApiModelProperty(value = "用户id")
        private String userId;

        @ApiModelProperty(value = "saas用户id")
        private String saasUserId;

        @ApiModelProperty(value = "unionId")
        private String unionId;
    }


}


