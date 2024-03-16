package com.fcb.coupon.app.infra.inteceptor;

import com.fcb.coupon.common.log.FeginRequestInterceptor;
import com.fcb.coupon.common.log.FeignParamInterceptor;
import com.fcb.coupon.common.log.TraceFilter;
import com.fcb.coupon.common.log.ApiParamInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 唐陆军
 * @Description 跟踪日志配置
 * @createTime 2021年06月16日 14:32:00
 */
@Configuration
public class TraceLogConfig {

    @Bean
    public TraceFilter traceFilter() {
        return new TraceFilter();
    }

    @Bean
    public ApiParamInterceptor apiParamInterceptor() {
        return new ApiParamInterceptor();
    }

    @Bean
    public FeginRequestInterceptor feginRequestInterceptor() {
        return new FeginRequestInterceptor();
    }

    @Bean
    public FeignParamInterceptor feignParamInterceptor() {
        return new FeignParamInterceptor();
    }
}
