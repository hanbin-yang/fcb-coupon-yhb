package com.fcb.coupon.common.excel.importer;

import com.fcb.coupon.common.excel.bean.RowParseResult;
import com.fcb.coupon.common.excel.excetption.ImportErrorCode;
import com.fcb.coupon.common.excel.excetption.ImportException;

/**
 * @author 唐陆军
 * @Description 快速失败模式
 * @createTime 2021年06月18日 17:50:00
 */
public class FailFastCSVImporter extends CSVImporter {

    @Override
    protected void handleRowParseResult(RowParseResult rowParseResult) {
        //快速失败，有失败数据，直接返回
        if (rowParseResult.getIsFailure()) {
            throw new ImportException(ImportErrorCode.ROW_PARSE_ERROR, rowParseResult.getCauseMessage());
        }
    }
}
