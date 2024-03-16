package com.fcb.coupon;

import com.fcb.coupon.app.AppApplication;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 后台单元测试基类
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {AppApplication.class})
public class BaseTest {

    static {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

}