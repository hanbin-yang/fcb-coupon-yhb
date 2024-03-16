package com.fcb.coupon.app.business.mingyuan;

import com.fcb.coupon.app.model.bo.CouponMingyuanBo;
import com.fcb.coupon.app.model.bo.OperateCouponBo;
import com.fcb.coupon.app.model.param.response.CouponMingyuanResponse;
import com.fcb.coupon.app.model.param.response.OperateCoupons4OrderResponse;
import com.fcb.coupon.app.model.param.response.QueryUsefulCouponResponse;

import java.util.List;

/**
 * @author YangHanBin
 * @date 2021-08-24 10:10
 */
public interface MingYuanVerificationBusiness {
    List<OperateCoupons4OrderResponse> operateCoupons4Order(OperateCouponBo bo);


}
