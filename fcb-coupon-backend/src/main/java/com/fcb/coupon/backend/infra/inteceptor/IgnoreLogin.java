package com.fcb.coupon.backend.infra.inteceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 后台接口 无需登录校验
 * @author YangHanBin
 * @date 2021-06-11 21:21
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreLogin {
}
