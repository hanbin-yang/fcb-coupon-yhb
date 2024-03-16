package com.fcb.coupon.backend.infra.inteceptor;

import com.fcb.coupon.common.dto.AuthorityHolder;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.exception.CommonErrorCode;
import com.fcb.coupon.common.midplatform.MidPlatformLoginHelper;
import com.fcb.coupon.common.dto.FunctionInfo;
import com.fcb.coupon.common.dto.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

/**
 * login interceptor
 * @author YangHanBin
 * @date 2021-06-11 17:48
 */
@Component
@Slf4j
public class BackendLoginInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    private MidPlatformLoginHelper midPlatformLoginHelper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String servletPath = request.getServletPath();
        //需要 放行的
        if (isIgnorePath(servletPath) || isIgnoreLogin((HandlerMethod) handler)) {
            return true;
        }

        String ut = getUt(request);
        if (StringUtils.isBlank(ut)) {
            log.error("ut空, 用户未登录！servletPath={}", servletPath);
            throw new BusinessException(CommonErrorCode.NO_LOGIN);
        }
        // 登录校验
        UserInfo userInfo = midPlatformLoginHelper.getUserInfoByUt(ut);
        // 权限信息
        FunctionInfo functionInfo = midPlatformLoginHelper.getFunctionInfoByUt(ut);

        // 需要校验请求路径的权限
        if (!isIgnoreAuthorityPath((HandlerMethod) handler)) {
            Set<String> functionPaths = functionInfo.getFunctionPaths();
            if (CollectionUtils.isEmpty(functionPaths) || !isAuthorityPath(servletPath, functionPaths)) {
                throw new BusinessException(CommonErrorCode.NO_AUTH_PATH);
            }
        }

        AuthorityHolder holder = new AuthorityHolder(userInfo,functionInfo);
        // 记录用户信息
        AuthorityHolder.AuthorityThreadLocal.set(holder);
        return true;
    }

    /**
     * 权限路径
     * @param servletPath
     * @param functionPaths
     * @return
     */
    private boolean isAuthorityPath( String servletPath, Set<String> functionPaths) {
        return functionPaths.stream().anyMatch(functionPath -> StringUtils.contains(functionPath, servletPath));
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String servletPath = request.getServletPath();
        //需要 放行的
        if (isIgnorePath(servletPath) || isIgnoreLogin((HandlerMethod) handler)) {
            return ;
        }
        // 清除用户信息
        AuthorityHolder.AuthorityThreadLocal.remove();
    }

    private String getUt(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("ut")) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private boolean isIgnoreLogin(HandlerMethod handlerMethod) {
        // 注解在类上
        IgnoreLogin beanAnnotation = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), IgnoreLogin.class);
        if (beanAnnotation != null) {
            return true;
        }
        // 注解在方法上
        return handlerMethod.hasMethodAnnotation(IgnoreLogin.class);
    }

    private boolean isIgnoreAuthorityPath(HandlerMethod handlerMethod) {
        // 注解在类上
        IgnoreAuthorityPath beanAnnotation = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), IgnoreAuthorityPath.class);
        if (beanAnnotation != null) {
            return true;
        }
        // 注解在方法上
        return handlerMethod.hasMethodAnnotation(IgnoreAuthorityPath.class);
    }

    /**
     * if need to be ignored path
     * @param servletPath request path
     * @return true:ignore  false:need to login validate
     */
    private boolean isIgnorePath(String servletPath) {
        if (StringUtils.contains(servletPath, "/error"))
        {
            return true;
        } else if (isSwaggerPath(servletPath)) {
            return true;
        }

        return false;
    }

    private boolean isSwaggerPath(String servletPath) {
        return StringUtils.contains(servletPath, "/doc.html")
                || StringUtils.contains(servletPath, "swagger")
                || StringUtils.contains(servletPath, "webjars")
                || StringUtils.contains(servletPath, "v2")
                || StringUtils.contains(servletPath, "favicon")
                ;
    }
}
