package com.fcb.coupon.common.excel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 唐陆军
 * @Description 单元格
 * @createTime 2021年06月17日 14:28:00
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface XCell {

    /**
     * 列的标题
     */
    String name();

    /**
     * 列所在的列号,-1 不使用该列号，解析是如果配置了cellNum将优先使用列号匹配
     */
    int cellNum() default -1;

    /**
     * 列宽
     */
//    int width() default 0;

    /**
     * 精确位数
     */
    int precision() default 0;

    /**
     * 设置自动换行
     */
    boolean wrapText() default false;
}
