package com.fcb.coupon.common.excel.bean;

import lombok.Data;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月17日 16:05:00
 */
@Data
public class ErrorBeanWrapper {

    private Object bean;

    private String error;

    public ErrorBeanWrapper(Object bean, String error) {
        this.bean = bean;
        this.error = error;
    }
}
