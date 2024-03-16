package com.fcb.coupon.common.excel.bean;

import com.fcb.coupon.common.excel.support.RowValidator;
import lombok.Data;

import java.util.Map;

/**
 * @author 唐陆军
 * @Description sheet配置
 * @createTime 2021年06月17日 14:42:00
 */
@Data
public class SheetConfig {

    private Class rowClass;

    private String sheetName;

    private int titleRowNum;

    private String sheetDesc;

    /*
     * 单元格跟属性映射关系
     */
    private Map<Integer, CellConfig> cellConfigMap;

    private Class<?>[] rowValidatorClazzs;

    private Integer maxCount;
}
