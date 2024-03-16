package com.fcb.coupon.app.model.param.request;

import cn.hutool.core.bean.BeanUtil;
import com.fcb.coupon.app.model.bo.CouponQueryBo;
import com.fcb.coupon.common.dto.AbstractBaseConvertor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Objects;

/**
 * 查询券明细列表请求
 *
 * @Author WeiHaiQi
 * @Date 2021-08-16 11:32
 **/
@ApiModel(description = "查询券明细列表请求")
@Data
public class QueryCouponRequest extends AbstractBaseConvertor<CouponQueryBo> implements Serializable {
    private static final long serialVersionUID = -6140185121388322197L;

    @ApiModelProperty(value = "券优惠类型：0 按金额；1 按折扣")
    private Integer couponDiscountType;

    @NotBlank(message = "userId不能为空")
    @ApiModelProperty(value = "用户UnionID")
    private String userId;

    @ApiModelProperty(value = "排序： 0领取时间；1按照到期时间")
    private Integer sortedBy;

    @ApiModelProperty(value = "是否倒序：缺省或true为倒序")
    private Boolean whetherDesc;

    @ApiModelProperty(value = "当前页码")
    private int currentPage;

    @ApiModelProperty(value = "页记录数")
    private int itemsPerPage;

    @Override
    public CouponQueryBo convert() {
        CouponQueryBo bo = new CouponQueryBo();
        BeanUtil.copyProperties(this, bo);

        // unionid
        bo.setUnionId(this.getUserId());

        if (this.getCurrentPage() < 1) {
            bo.setCurrentPage(1);
        }
        if (this.getItemsPerPage() < 1) {
            bo.setItemsPerPage(10);
        }

        if (Objects.equals(this.getCouponDiscountType(), 999)) {
            bo.setCouponDiscountType(null);
        }

        if (this.getSortedBy() == null) {
            bo.setSortedBy(0);
        }

        return bo;
    }
}
