package com.fcb.coupon.common.excel.constant;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月17日 16:09:00
 */
public interface ImportConstant {

    String EXCEL_CELL_TYPE_STRING = "string";
    String EXCEL_CELL_TYPE_BOOLEAN = "boolean";
    String EXCEL_CELL_TYPE_INTEGER = "integer";
    String EXCEL_CELL_TYPE_LONG = "long";
    String EXCEL_CELL_TYPE_DOUBLE = "double";
    String EXCEL_CELL_TYPE_DATE = "date";
    String EXCEL_CELL_TYPE_ENUM = "enum";
    String EXCEL_CELL_TYPE_BIG_DECIMAL = "BigDecimal";

    String CSV_SUFFIX = ".csv";
    String EXCEL_2003_SUFFIX = ".xls";
    String EXCEL_2007_SUFFIX = ".xlsx";
    String EXCEL_2007_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8";
    String EXCEL_2003_CONTENT_TYPE = "application/vnd.ms-excel";
    String FILE_CONTENT_TYPE = "application/octet-stream";
    String FILE_CONTENT_TYPE_UTF8 = "application/octet-stream;charset=UTF-8";

    String ERROR_CELL_TITLE = "错误信息";
}
