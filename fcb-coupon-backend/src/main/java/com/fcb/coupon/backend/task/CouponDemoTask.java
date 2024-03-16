package com.fcb.coupon.backend.task;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月09日 19:57:00
 */
public class CouponDemoTask {

    @XxlJob("demoJobHandler")
    public void demoJobHandler() throws Exception {
        XxlJobHelper.log("XXL-JOB, Hello World.");
    }

}
