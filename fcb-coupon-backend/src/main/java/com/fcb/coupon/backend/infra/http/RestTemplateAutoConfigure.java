package com.fcb.coupon.backend.infra.http;

import com.fcb.coupon.common.log.RestTemplateInterceptor;
import com.fcb.coupon.common.log.RestTemplateParamInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;


@Component
@Configuration
@ConfigurationProperties(prefix = "fcb.rest")
public class RestTemplateAutoConfigure {

    @Value("${spring.application.name:unknow}")
    private String applicationName;

    private Integer connectTimeout = 2000;

    private Integer readTimeout = 3000;


    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate(apacheHttpClientHttpRequestFactory());
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(restTemplate.getInterceptors());
        interceptors.add(new RestTemplateInterceptor(applicationName));
        interceptors.add(new RestTemplateParamInterceptor());
        restTemplate.setInterceptors(interceptors);
//        if (!CollectionUtils.isEmpty(httpMessageConverters)) {
//            restTemplate.setMessageConverters(httpMessageConverters);
//        }
        return restTemplate;
    }


    @Bean
    public ClientHttpRequestFactory apacheHttpClientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(this.readTimeout.intValue());
        factory.setConnectTimeout(this.connectTimeout.intValue());
        return factory;
    }
}
