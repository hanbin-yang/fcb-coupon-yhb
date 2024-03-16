package com.fcb.coupon.app.model.dto;

import com.fcb.coupon.common.enums.CouponStatusEnum;
import com.fcb.coupon.common.enums.SortBy;
import com.fcb.coupon.common.enums.UserTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * 查询条件
 */
@Data
public class CouponEsConditionDto {

    @ApiModelProperty(value = "券ID")
    private Long id;

    @ApiModelProperty(value = "券ID列表")
    private List<Long> couponIdList;

    @ApiModelProperty(value = "优惠券状态")
    private List<Integer> couponStatus;

    @ApiModelProperty(value = "优惠券码")
    private String couponCode;

    /**
     * 用户ID（broker_id,customer_id)
     */
    @ApiModelProperty(value = "用户ID")
    private String userId;

    /**
     * 手机号/账号
     */
    @ApiModelProperty(value = "手机号/账号")
    private String bindTel;

    /**
     * 账号类型
     */
    @ApiModelProperty(value = "账号类型")
    private Integer userType;

    /**
     * 券优惠类型 0 按金额 1 按折扣
     */
    @ApiModelProperty(value = "券优惠类型 0 按金额 1 按折扣")
    private Integer couponDiscountType;

    /** 是否只查询失效优惠券*/
    @ApiModelProperty(value = "是否只查询失效优惠券")
    private Boolean invalidCoupon;

    /**
     * 排序字段 0:领取时间  1: 按照到期时间
     */
    @ApiModelProperty(value = "0:领取时间  1: 按照到期时间")
    private Integer sortedBy;

    /**
     * 是否倒序
     */
    @ApiModelProperty(value = "是否倒序")
    private Boolean whetherDesc;

    /**
     * 当前页码
     */
    @ApiModelProperty(value = "当前页码")
    private int currentPage = 0;

    /**
     * 页大小
     */
    @ApiModelProperty(value = "页大小")
    private int itemsPerPage = 0;
    /**
     * 是否倒序
     */
    @ApiModelProperty(value = "倒序方式")
    private SortBy sortBy;
    
    /**
     * 批次号
     */
    @ApiModelProperty(value = "批次号")
    private String batchNo;

    /**
     * 用户unionId
     */
    @ApiModelProperty(value = "用户unionId")
    private String unionId;
}
