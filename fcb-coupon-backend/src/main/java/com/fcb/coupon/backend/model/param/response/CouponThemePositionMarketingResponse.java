package com.fcb.coupon.backend.model.param.response;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
/**
 * 营销中心->优惠券管理->优惠券运营位管理->添加优惠券->查询优惠券列表 出参
 * @author mashiqiong
 * @date 2021-06-17 20:59
 */
@ApiModel(value="优惠券列表",description="优惠券列表")
@Data
public class CouponThemePositionMarketingResponse extends CouponThemeBaseResponse implements Serializable {
    private static final long serialVersionUID = -1698868954971347739L;


}
