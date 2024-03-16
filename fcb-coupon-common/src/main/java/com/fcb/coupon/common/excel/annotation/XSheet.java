package com.fcb.coupon.common.excel.annotation;

import com.fcb.coupon.common.excel.support.RowValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月17日 14:28:00
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface XSheet {

    /*
     * sheet名称，csv不会根据sheet名称读取
     */
    String name();

    /*
     * 标题行号，从第0行算起，值应该<=1
     */
    int titleRowNum() default 0;

    /*
     * 说明，添加了说明会放在在文件的第一行
     */
    String desc() default "";

    /*
     * 行校验器
     */
    Class<?>[] rowValidatorClazzs() default {};

    /*
     * 最大导入数量，0为不限制
     */
    int maxCount() default 0;
}
