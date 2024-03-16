package com.fcb.coupon.common.excel.bean;

import lombok.Data;

import java.util.Map;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月17日 14:44:00
 */
@Data
public class SheetParseResult {

    private Class rowBeanClass;

    private String sheetName;

    private Map<Integer, RowParseResult> rowParseResultMap;

}
