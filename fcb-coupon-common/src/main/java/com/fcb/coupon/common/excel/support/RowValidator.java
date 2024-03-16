package com.fcb.coupon.common.excel.support;

import com.fcb.coupon.common.excel.excetption.ImportException;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月18日 18:10:00
 */
public interface RowValidator {

    void validate(Object bean) throws ImportException;

}
