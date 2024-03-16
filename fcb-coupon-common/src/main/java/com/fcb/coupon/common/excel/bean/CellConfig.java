package com.fcb.coupon.common.excel.bean;

import lombok.Data;

/**
 * @author 唐陆军
 * @Description 单元格配置
 * @createTime 2021年06月17日 14:42:00
 */
@Data
public class CellConfig {

    private String cellName;

    private Integer cellNum;

    private String fieldName;

    private String cellType;

    private Integer precision;

    private Boolean wrapText;
}
