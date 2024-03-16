package com.fcb.coupon.app.infra.inteceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author YangHanBin
 * @date 2021-08-18 17:24
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AppLogin {
    /**
     * 客户端用户类型, 不指定则应该从请求头获取
     * B_USER: B端用户 C_USER: C端用户 SAAS_USER: SAAS端用户 J_USER: 机构用户(废弃)
     */
    String clientType() default "";
    /**
     * 是否需要获取用户信息，默认需要
     */
    boolean needUserInfo() default true;
    /**
     * 是否缓存用户信息，默认是
     */
    boolean cacheUserInfo() default true;
}
