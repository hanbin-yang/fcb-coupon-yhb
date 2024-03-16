package com.fcb.coupon.app.infra.inteceptor;

import com.fcb.coupon.app.facade.ClientUserFacade;
import com.fcb.coupon.common.enums.ClientTypeEnum;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.exception.CommonErrorCode;
import com.fcb.coupon.common.util.SpringBeanFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 根据注解配置登录拦截和获取用户信息
 *
 * @author YangHanBin
 * @date 2021-08-18 17:21
 */
@Slf4j
public class AppLoginInterceptor extends HandlerInterceptorAdapter {
    private final ClientUserFacade clientUserFacade = SpringBeanFactory.getBean(ClientUserFacade.class);


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //需要 放行的
        if (isIgnorePath(request.getServletPath())) {
            return true;
        }

        AppLogin annotation = findAppLoginAnnotation((HandlerMethod) handler);
        // 没有注解，放行
        if (annotation == null) {
            return true;
        }

        String clientType = annotation.clientType();
        // 如果注解没有指定clientType，从请求头里获取
        if (clientType.isEmpty()) {
            clientType = getClientTypeFromRequest(request);
        }

        if (StringUtils.equals(ClientTypeEnum.C.getKey(), clientType)) {
            customerLoginHandle(request, annotation);
        } else if (StringUtils.equals(ClientTypeEnum.B.getKey(), clientType)) {
            brokerLoginHandle(request, annotation);
        } else if (StringUtils.equals(ClientTypeEnum.SAAS.getKey(), clientType)) {
            saasLoginHandle(request, annotation);
        } else {
            throw new IllegalArgumentException("error clientType [" + clientType + "] 非法");
        }

        return true;
    }

    public void customerLoginHandle(HttpServletRequest request, AppLogin annotation) {
        if (!clientUserFacade.validateCustomerLogin(request)) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
        // 需要获取用户信息
        if (annotation.needUserInfo()) {
            AppUserInfo userInfo = clientUserFacade.getCustomerInfoByUnionId(request.getHeader("unionId"), annotation.cacheUserInfo());
            AppAuthorityHolder holder = new AppAuthorityHolder();
            holder.setUserInfo(userInfo);
            AppAuthorityHolder.AuthorityThreadLocal.set(holder);
        }
    }

    public void brokerLoginHandle(HttpServletRequest request, AppLogin annotation) {
        if (!clientUserFacade.validateMemberLogin(request)) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
        // 需要获取用户信息
        if (annotation.needUserInfo()) {
            AppUserInfo userInfo = clientUserFacade.getMemberInfoByUnionId(request.getHeader("unionId"), annotation.cacheUserInfo());
            AppAuthorityHolder holder = new AppAuthorityHolder();
            holder.setUserInfo(userInfo);
            AppAuthorityHolder.AuthorityThreadLocal.set(holder);
        }
    }

    private void saasLoginHandle(HttpServletRequest request, AppLogin annotation) {
        String token = request.getHeader("authorization");
        if (StringUtils.isBlank(token)) {
            throw new BusinessException(CommonErrorCode.AUTHORIZATION_NULL);
        }
        //校验token
        if (!clientUserFacade.validateSaasLogin(request)) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
        AppUserInfo userInfo = clientUserFacade.getSaasInfoByToken(token);
        AppAuthorityHolder holder = new AppAuthorityHolder();
        holder.setUserInfo(userInfo);
        AppAuthorityHolder.AuthorityThreadLocal.set(holder);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //需要 放行的
        if (isIgnorePath(request.getServletPath())) {
            return;
        }

        AppLogin annotation = findAppLoginAnnotation((HandlerMethod) handler);
        // 没有注解，放行
        if (annotation == null) {
            return;
        }

        if (annotation.needUserInfo()) {
            // 清除用户信息
            AppAuthorityHolder.AuthorityThreadLocal.remove();
        }
    }

    private String getClientTypeFromRequest(HttpServletRequest request) {
        String clientType = request.getHeader("clientType");
        if (StringUtils.isBlank(clientType)) {
            throw new BusinessException(CommonErrorCode.CLIENT_TYPE_NULL);
        }
        return clientType;
    }


    private AppLogin findAppLoginAnnotation(HandlerMethod handlerMethod) {
        // 注解在类上
        AppLogin beanAnnotation = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), AppLogin.class);
        if (beanAnnotation == null) {
            return handlerMethod.getMethodAnnotation(AppLogin.class);
        }

        return beanAnnotation;
    }

    /**
     * if need to be ignored path
     *
     * @param servletPath request path
     * @return true:ignore  false:need to login validate
     */
    private boolean isIgnorePath(String servletPath) {
        if (StringUtils.contains(servletPath, "/error")) {
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