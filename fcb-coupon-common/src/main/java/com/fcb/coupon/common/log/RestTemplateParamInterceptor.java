package com.fcb.coupon.common.log;

import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.exception.CommonErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月04日 10:44:00
 */
@Slf4j
public class RestTemplateParamInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        log.info("{}\nrequest parameters：{}", request.getURI().toURL(), new String(body, StandardCharsets.UTF_8));
        long start = System.currentTimeMillis();
        ClientHttpResponse response = null;
        try {
            response = execution.execute(request, body);
            return response;
        } catch (Exception ex) {
            log.error("restTemplate call api error", ex);
            throw new BusinessException(CommonErrorCode.API_CALL_ERROR);
        } finally {
            //当然图片、文件一类的就可以省了，打出日志没啥用处
//            if (response == null || response.getHeaders().getContentType() == null || !response.getHeaders().getContentType().includes(MediaType.APPLICATION_JSON)) {
//                log.error("response parameters is null or is not a json format");
//                return response;
//            }
            long cost = System.currentTimeMillis() - start;
            log.info("{}\nresponse parameters: cost={}ms statusCode={},content-type={}", new Object[]{
                    request.getURI().toURL(),
                    Long.valueOf(cost),
                    response.getRawStatusCode(),
                    response.getHeaders().getContentType()
            });
        }
    }


    protected byte[] getResponseBody(ClientHttpResponse response) {

        try {
            return FileCopyUtils.copyToByteArray(response.getBody());
        } catch (IOException ex) {
            // ignore
        }
        return new byte[0];
    }


    protected Charset getCharset(ClientHttpResponse response) {
        HttpHeaders headers = response.getHeaders();
        MediaType contentType = headers.getContentType();
        return (contentType != null ? contentType.getCharset() : null);
    }
}
