package com.fcb.coupon.app;

import cn.hutool.core.thread.NamedThreadFactory;
import com.fcb.coupon.common.util.AESPromotionUtil;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import org.apache.poi.ss.formula.functions.T;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * @author HanBin_Yang
 * @since 2021/6/24 10:23
 */
public class CommonMainTest {

    @Test
    public void wheelTimerTest() throws InterruptedException {
        HashedWheelTimer hashedWheelTimer = new HashedWheelTimer(new NamedThreadFactory("", false), 2, TimeUnit.SECONDS, 2);
        hashedWheelTimer.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                System.out.println("hello world1 " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                timeout.timer().newTimeout(this, 0, TimeUnit.SECONDS);
            }
        }, 1, TimeUnit.SECONDS);

        hashedWheelTimer.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                System.err.println("hello world2 " + LocalDateTime.now().format( DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                timeout.timer().newTimeout(this, 0, TimeUnit.SECONDS);
            }
        }, 2, TimeUnit.SECONDS);

        while (true) {
            Thread.sleep(1000);
        }
    }

}
