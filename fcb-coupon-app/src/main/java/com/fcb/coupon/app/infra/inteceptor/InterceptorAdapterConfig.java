package com.fcb.coupon.app.infra.inteceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author YangHanBin
 * @date 2021-06-11 18:21
 */
@Configuration
public class InterceptorAdapterConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        // 添加拦截器
        registry.addInterceptor(new AppLoginInterceptor()).addPathPatterns("/**");
    }
}
