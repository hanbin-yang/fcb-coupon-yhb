package com.fcb.coupon.client.backend.param.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class CouponThemeListCmsResponse implements Serializable {

    private List<CouponThemeListCmsObj> listObj;
    private int total;

    @Data
    public static class CouponThemeListCmsObj implements Serializable {

        @ApiModelProperty(value = "活动Id")
        private Long id;
        @ApiModelProperty(value = "优惠券名称")
        private String themeTitle;
        @ApiModelProperty(value = "活动开始时间")
        private Date startTime;
        @ApiModelProperty(value = "活动结束时间")
        private Date endTime;
        @ApiModelProperty(value = "活动状态 0 未审核 1 待审核 2 未开始 3 审核不通过 4 进行中 5 已过期 6 已关闭")
        private Integer status;
        @ApiModelProperty(value = "券类型 0电子券 1实体券/预制券 2红包券 3：第三方券码")
        private Integer couponType;
        @ApiModelProperty(value = "当前券活动总可领取券数")
        private Integer totalLimit;
        @ApiModelProperty(value = "当前券活动已生成券数")
        private Integer drawedCoupons;
        @ApiModelProperty(value = "已发数量")
        private Integer sendedCouopns;

        private Date createTime;

        @ApiModelProperty(value = "发券类型(1:活动规则券,19:线下预制券,4:前台领券,17:主动营销券,18:权益优惠券,19:线下预制券,20:媒体广告券,21:直播券,22:营销活动页券)")
        private Integer couponGiveRule;
        @ApiModelProperty(value = "当前券活动个人可领取券数")
        private Integer individualLimit;
        @ApiModelProperty(value = "使用限制  0：无限制， 其他：最小金额限制")
        private BigDecimal useLimit;
        @ApiModelProperty(value = "审核备注")
        private String remark;
        @ApiModelProperty(value = "活动类型 0：平台券  11：商家券 5：集团券 21：店铺券")
        private Integer themeType;
        @ApiModelProperty(value = "活动Id")
        private BigDecimal couponAmount;
        @ApiModelProperty(value = "优惠方式")
        private Integer couponDiscountType;
    }

}
