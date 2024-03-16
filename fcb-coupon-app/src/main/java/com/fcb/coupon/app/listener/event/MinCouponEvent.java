package com.fcb.coupon.app.listener.event;

import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * 保底券事件
 *
 * @Author WeiHaiQi
 * @Date 2021-06-21 23:23
 **/
public class MinCouponEvent extends ApplicationEvent {

    public MinCouponEvent(List<String> mobiles) {
        super(mobiles);
    }
}
