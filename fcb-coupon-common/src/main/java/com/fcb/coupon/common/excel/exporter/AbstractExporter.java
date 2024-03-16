package com.fcb.coupon.common.excel.exporter;

import com.fcb.coupon.common.excel.bean.ErrorBeanWrapper;

import java.util.List;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月17日 19:37:00
 */
public abstract class AbstractExporter implements Exporter {


    protected boolean isCreateErrorCell(List<? extends Object> rowDatas) {
        if (rowDatas != null && rowDatas.size() > 0) {
            Object bean = rowDatas.get(0);
            if (bean instanceof ErrorBeanWrapper) {
                return true;
            }
        }
        return false;
    }
}
