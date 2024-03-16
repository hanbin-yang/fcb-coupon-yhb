package com.fcb.coupon.app.model.param.request;

import cn.hutool.core.bean.BeanUtil;
import com.fcb.coupon.app.model.bo.CouponThemeListBo;
import com.fcb.coupon.common.dto.AbstractBaseConvertor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 微吼查询优惠券活动列表（后台）
 * @author mashiqiong
 * @date 2021-8-17 15:30
 */
@ApiModel(description = "查询券活动列表-入参")
@Data
public class CouponThemeListRequest extends AbstractBaseConvertor<CouponThemeListBo> implements Serializable {

    @ApiModelProperty(value = "券活动ID", dataType = "List<Long>")
    private List<Long> ids;

    /**
     * 使用人群 0会员 1机构经济人 2C端用户
     */
    @ApiModelProperty(value = "券活动ID", dataType = "Integer")
    private Integer crowdScope;

    @ApiModelProperty(value = "当前页", dataType = "Integer")
    private int currentPage;

    @ApiModelProperty(value = "每页显示条数", dataType = "Integer")
    private int itemsPerPage;

    @Override
    public CouponThemeListBo convert() {
        CouponThemeListBo bo = new CouponThemeListBo();
        BeanUtil.copyProperties(this, bo);

        return bo;
    }
}
