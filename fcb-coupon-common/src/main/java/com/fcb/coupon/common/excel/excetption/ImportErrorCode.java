package com.fcb.coupon.common.excel.excetption;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月17日 14:53:00
 */
public interface ImportErrorCode {

    String SHEET_CONFIG_ERROR = "1";

    String SHEET_TITLE_ERROR = "2";

    String ShEET_EXPORT_ERROR = "3";

    String ShEET_IMPORT_ERROR = "4";

    String ShEET_LIMIT_MAX_ERROR = "5";

    String ShEET_TITLE_ERROR = "6";

    String CELL_TYPE_ERROR = "11";

    String CELL_TYPE_UNKOWN = "12";

    String CELL_DATA_ERROR = "13";

    String CELL_EMPTY = "14";

    String CELL_CONFIG_ERROR = "15";

    String ROW_PARSE_ERROR = "21";

    String ROW_EMPTY_ERROR = "22";

    String ROW_VALID_ERROR = "23";

    String ROW_VALID_CONFIG_ERROR = "24";

    String ROW_CUSTOM_VALID_ERROR = "25";
}
