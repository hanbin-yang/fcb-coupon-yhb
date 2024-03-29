package com.fcb.coupon;

import com.fcb.coupon.backend.BackendApplication;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 后台单元测试基类
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BackendApplication.class})
public class BaseTest {

    static {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

}