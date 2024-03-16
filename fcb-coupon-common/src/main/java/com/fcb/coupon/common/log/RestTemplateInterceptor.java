package com.fcb.coupon.common.log;

import com.fcb.coupon.common.constant.InfraConstant;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * @author 唐陆军
 * @Description RestTemplate日志续传
 * @createTime 2021年06月17日 10:55:00
 */
public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {

    private String applicationName;

    public RestTemplateInterceptor(String applicationName) {
        this.applicationName = applicationName;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        HttpHeaders headers = request.getHeaders();
        headers.add(InfraConstant.TRACE_ID, MDC.get(InfraConstant.TRACE_ID));
        headers.add(InfraConstant.SOURCE_APP_NAME, this.applicationName);
        return execution.execute(request, body);
    }
}
