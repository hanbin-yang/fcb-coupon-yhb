package com.fcb.coupon.backend.infra.inteceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 路径无需权限校验
 * @author YangHanBin
 * @date 2021-08-06 10:50
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreAuthorityPath {
}
