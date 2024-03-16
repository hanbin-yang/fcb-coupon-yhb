package com.fcb.coupon.common.log;

import com.fcb.coupon.common.constant.CouponConstant;
import com.fcb.coupon.common.constant.InfraConstant;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author 唐陆军
 * @Description feign调用日志信息续传
 * @createTime 2021年06月17日 10:02:00
 */
public class FeginRequestInterceptor implements RequestInterceptor {

    @Value("${spring.application.name:unknow}")
    private String applicationName;

    @Override
    public void apply(RequestTemplate template) {
        template.header(InfraConstant.SOURCE_APP_NAME, new String[]{this.applicationName});
        template.header(InfraConstant.TRACE_ID, new String[]{MDC.get(InfraConstant.TRACE_ID)});
    }
}
