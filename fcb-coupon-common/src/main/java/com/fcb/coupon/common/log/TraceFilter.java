package com.fcb.coupon.common.log;

import com.fcb.coupon.common.util.ServletUtils;
import com.fcb.coupon.common.constant.InfraConstant;
import com.fcb.coupon.common.util.UUIDUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.UUID;


/**
 * @author 唐陆军
 * @Description 请求头过来，traceid设置
 * @createTime 2021年06月09日 19:51:00
 */
@Slf4j
public class TraceFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        StringBuilder requestHeaderBuilder = new StringBuilder();
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        String headerName = null;
        while (headerNames.hasMoreElements()) {
            headerName = headerNames.nextElement();
            requestHeaderBuilder.append(headerName).append(": ").append(httpServletRequest.getHeader(headerName)).append("\n");
        }
        String apiUri = httpServletRequest.getRequestURI();
        String queryString = httpServletRequest.getQueryString();
        if (queryString != null && !"".equals(queryString.trim())) {
            apiUri = apiUri + "?" + queryString;
        }
        // 从请求头中获取traceId
        String traceId = httpServletRequest.getHeader(InfraConstant.TRACE_ID);
        // 不存在就生成一个
        if (StringUtils.isBlank(traceId)) {
            traceId = UUIDUtils.getShortUUID();
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            httpServletResponse.addHeader(InfraConstant.TRACE_ID, traceId);
        }
        MDC.put(InfraConstant.TRACE_ID, traceId);
        log.info("({}) {} \n headers: \n{}", new Object[]{apiUri, ServletUtils.getClientIP(httpServletRequest), requestHeaderBuilder.toString()});
        chain.doFilter(request, response);
    }
}