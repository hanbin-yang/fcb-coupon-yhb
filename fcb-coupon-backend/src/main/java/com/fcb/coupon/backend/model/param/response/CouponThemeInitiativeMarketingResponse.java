package com.fcb.coupon.backend.model.param.response;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

/**
 * 营销中心->主动营销->营销任务管理->编辑任务流->添加优惠券->查询优惠券列表 出参
 * @author mashiqiong
 * @date 2021-06-17 20:59
 */
@ApiModel(value="优惠券列表",description="优惠券列表")
@Data
public class CouponThemeInitiativeMarketingResponse extends CouponThemeBaseResponse implements Serializable {
    private static final long serialVersionUID = -1698868954971347739L;


}
