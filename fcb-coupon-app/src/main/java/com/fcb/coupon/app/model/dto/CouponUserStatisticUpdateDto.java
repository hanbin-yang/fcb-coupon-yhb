package com.fcb.coupon.app.model.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author YangHanBin
 * @date 2021-08-19 15:46
 */
@Data
@Builder
public class CouponUserStatisticUpdateDto {
    /**
     * 券活动id
     */
    @NotNull
    private Long couponThemeId;
    /**
     * 用户id
     */
    @NotNull
    private String userId;
    /**
     * 用户类型
     */
    @NotNull
    private Integer userType;
    /**
     * 需要领取或发券的数量
     */
    private Integer count;

    /**
     * 最后更新时间
     */
    private Date updateTime;
}
