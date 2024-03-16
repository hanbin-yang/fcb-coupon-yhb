package com.fcb.coupon.backend.listener;

import com.fcb.coupon.backend.listener.event.MinCouponEvent;
import com.fcb.coupon.backend.service.MinCouponService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.Future;

/**
 *
 * @Author WeiHaiQi
 * @Date 2021-06-21 23:24
 **/
@Component
public class MinCouponListener {

    @Resource
    private MinCouponService minCouponService;

    @Async
    @TransactionalEventListener(fallbackExecution = true, classes = MinCouponEvent.class)
    public Future<?> onApplicationEvent(MinCouponEvent event) {
        List<String> mobiles = (List<String>) event.getSource();
        if (CollectionUtils.isEmpty(mobiles)) {
            return new AsyncResult<>(null);
        }

        minCouponService.sendMinCoupon(mobiles);

        return new AsyncResult<>(null);
    }
}
