package com.fcb.coupon.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fcb.coupon.app.model.bo.ReceiveBeforeGivingBo;
import com.fcb.coupon.app.model.entity.CouponBeforeGiveEntity;
import com.fcb.coupon.app.model.bo.CouponBeforeGiveAddBo;
import com.fcb.coupon.app.model.param.response.CouponBeforeGiveAddResponse;
import com.fcb.coupon.app.model.param.response.ReceiveBeforeGivingResponse;

/**
 * @author YangHanBin
 * @date 2021-08-13 9:01
 */
public interface CouponBeforeGiveService extends IService<CouponBeforeGiveEntity> {
    ReceiveBeforeGivingResponse getCouponBeforeGivingInfo(ReceiveBeforeGivingBo bo);
    /**
    * 方法描述:根据券id统计转赠前的优惠券信息次数 (短信赠送) - B
    * @author liubingpei
    * @date 11:45 2021-9-15
    * @param couponId
    * @return
    */
    Integer getCouponBeforeGiveCanSendSmsCount(Long couponId);

    /**
    * 方法描述: 券转赠业务处理
    * @author liubingpei
    * @date 11:46 2021-9-15
    * @param bo
    * @return
    */
    CouponBeforeGiveAddResponse addCouponBeforeGiveById(CouponBeforeGiveAddBo bo);
    Integer getBeforeGiveCount(Long couponId);
}
