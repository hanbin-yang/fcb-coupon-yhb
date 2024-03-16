package com.fcb.coupon.common.util;

import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * @author 唐陆军
 * @Description web容器相关工具类
 * @createTime 2021年06月16日 10:23:00
 */
public class ServletUtils {

    public static String getClientIP(HttpServletRequest request) {
        String ip = null;
        String ipAddresses = request.getHeader("X-Forwarded-For");
        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            ipAddresses = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            ipAddresses = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            ipAddresses = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            ipAddresses = request.getHeader("X-Real-IP");
        }
        if (ipAddresses != null && ipAddresses.length() != 0) {
            ip = ipAddresses.split(",")[0];
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public static String getClientIP() {
        HttpServletRequest httpServletRequest = getRequest();
        if (httpServletRequest == null) {
            return null;
        }
        return getClientIP(httpServletRequest);
    }

    public static String getUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || "".equals(userAgent.trim())) {
            userAgent = "unkonw";
        }
        return userAgent;
    }

    public static String getUserAgent() {
        HttpServletRequest httpServletRequest = getRequest();
        if (httpServletRequest == null) {
            return null;
        }
        return getUserAgent(httpServletRequest);
    }

    public static String getCookieVlue(String name, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length <= 0) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public static boolean isMultipart() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return false;
        }

        if (!"post".equalsIgnoreCase(request.getMethod())){
            return false;
        }

        String contentType = request.getContentType();
        return StringUtils.startsWithIgnoreCase(contentType, "multipart/");
    }

    private static HttpServletRequest getRequest() {
        RequestAttributes attr = RequestContextHolder.getRequestAttributes();
        if (attr == null) {
            return null;
        }
        if (attr instanceof ServletRequestAttributes) {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) attr;
            return requestAttributes.getRequest();
        }
        return null;
    }
}
