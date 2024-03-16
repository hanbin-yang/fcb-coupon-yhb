package com.fcb.coupon.app.business.mingyuan;

import com.fcb.coupon.app.model.bo.CouponMingyuanBo;
import com.fcb.coupon.app.model.param.response.CouponMingyuanResponse;
import com.fcb.coupon.app.model.bo.CheckCouponUsefulBo;
import com.fcb.coupon.app.model.bo.QueryUsefulCouponBo;
import com.fcb.coupon.app.model.param.response.CheckCouponUsefulResponse;
import com.fcb.coupon.app.model.param.response.QueryUsefulCouponResponse;
import com.fcb.coupon.common.dto.ResponseDto;

import java.util.List;

/**
 * @author mashiqiong
 * @date 2021-08-24 10:10
 */
public interface MingYuanVerificationReadBusiness {

    /**
     * 明源-根据券id等查询对应优惠券信息
     *
     * @param bo
     * @return
     */
    List<CouponMingyuanResponse> getMingyuanCouponListByIds(CouponMingyuanBo bo);

    /**
     * 根据手机号、roomGuid(房源id)、transactionId(交易号)获取可使用优惠券信息---明源专用
     *
     * @param bo
     * @return
     */
    List<QueryUsefulCouponResponse> queryCouponList4Order(QueryUsefulCouponBo bo);

    ResponseDto<List<CheckCouponUsefulResponse>> validateCoupons4OrderForQuery(CheckCouponUsefulBo bo);
}
