package com.fcb.coupon.app.model.bo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 明源-查询对应优惠券信息入参
 *
 * @Author WeiHaiQi
 * @Date 2021-08-20 15:08
 **/
@Data
public class CouponMingyuanBo implements Serializable {

    /**
     * 券id
     */
    private List<Long> couponIds;
}
