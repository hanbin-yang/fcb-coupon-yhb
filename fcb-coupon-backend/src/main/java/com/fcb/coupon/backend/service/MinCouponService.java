package com.fcb.coupon.backend.service;

import java.util.List;

/**
 * 保底券服务
 *
 * @Author WeiHaiQi
 * @Date 2021-06-21 20:47
 **/
public interface MinCouponService {

    /**
     * 发送保底券
     * @param mobiles
     */
    void sendMinCoupon(List<String> mobiles);
}
