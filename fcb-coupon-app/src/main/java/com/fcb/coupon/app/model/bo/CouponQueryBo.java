package com.fcb.coupon.app.model.bo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 券明细列表查询BO
 *
 * @Author WeiHaiQi
 * @Date 2021-08-16 15:47
 **/
@Data
public class CouponQueryBo implements Serializable {

    private static final long serialVersionUID = -5086445445031264003L;

    @ApiModelProperty(value = "券优惠类型：0 按金额；1 按折扣")
    private Integer couponDiscountType;

    @ApiModelProperty(value = "用户UnionID")
    private String unionId;

    @ApiModelProperty(value = "用户ID")
    private String realUserId;

    @ApiModelProperty(value = "排序： 0领取时间；1按照到期时间")
    private Integer sortedBy;

    @ApiModelProperty(value = "是否倒序：缺省或true为倒序")
    private Boolean whetherDesc;

    @ApiModelProperty(value = "当前页码")
    private int currentPage;

    @ApiModelProperty(value = "页记录数")
    private int itemsPerPage;

    @ApiModelProperty(value = "优惠券使用状态")
    private List<Integer> couponStatus;

    @ApiModelProperty(value = "绑定账号类型")
    private Integer userType;

//    @ApiModelProperty(value = "是否只查询失效优惠券：true是")
//    private Boolean invalidCoupon;
}
