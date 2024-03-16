package com.fcb.coupon.common.excel.importer;

import com.fcb.coupon.common.excel.bean.CellConfig;
import com.fcb.coupon.common.excel.bean.RowParseResult;
import com.fcb.coupon.common.excel.bean.SheetConfig;
import com.fcb.coupon.common.excel.bean.SheetParseResult;
import com.fcb.coupon.common.excel.excetption.ImportErrorCode;
import com.fcb.coupon.common.excel.excetption.ImportException;
import com.fcb.coupon.common.excel.support.AnnotationConfigHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.util.CollectionUtils;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 唐陆军
 * @Description Excel解析
 * @createTime 2021年06月17日 14:32:00
 */
@Slf4j
public class ExcelImporter extends AbstractImporter {

    @Override
    public SheetParseResult parse(InputStream inputStream, Class rowClass) throws ImportException {
        return parse(inputStream, rowClass, null);
    }

    @Override
    public SheetParseResult parse(InputStream inputStream, Class rowClass, Charset charset) throws ImportException {
        Workbook workbook = null;
        try {
            workbook = WorkbookFactory.create(inputStream);
            return parseSheet(workbook, rowClass);
        } catch (ImportException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("excel import error", ex);
            throw new ImportException(ImportErrorCode.ShEET_IMPORT_ERROR, "导入失败");
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception ex) {
                    log.error("关闭 excel导入 文件流失败, 异常原因：{}", ex.getMessage());
                }
            }
        }
    }

    /**
     * 解析Sheet
     */
    private SheetParseResult parseSheet(Workbook workbook, Class rowClass) {
        SheetConfig sheetConfig = AnnotationConfigHelper.parseSheetConfig(rowClass);
        Sheet sheet = workbook.getSheet(sheetConfig.getSheetName());
        if (sheet == null) {
            log.error("表{}不存在", sheetConfig.getSheetName());
            throw new ImportException(ImportErrorCode.SHEET_CONFIG_ERROR, "模板错误");
        }
        //判断最大导入数量
        if (sheetConfig.getMaxCount() > 0 && sheet.getLastRowNum() + 1 > sheetConfig.getMaxCount()) {
            throw new ImportException(ImportErrorCode.ShEET_LIMIT_MAX_ERROR, "只允许导入" + sheetConfig.getMaxCount() + "条数据");
        }

        //解释标题
        Row titleRow = sheet.getRow(sheetConfig.getTitleRowNum());
        if (null == titleRow) {
            int titleRowNum = sheetConfig.getTitleRowNum() + 1;
            log.error("在第【{}】行未找到标题！", titleRowNum);
            throw new ImportException(ImportErrorCode.SHEET_TITLE_ERROR, "模板标题行解析失败");
        }
        //解析单元格配置
        Map<Integer, CellConfig> cellConfigMap = AnnotationConfigHelper.parseCellConfig(rowClass, titleRow);
        if (CollectionUtils.isEmpty(cellConfigMap)) {
            throw new ImportException(ImportErrorCode.CELL_CONFIG_ERROR, "无配置任何导入列");
        }
        sheetConfig.setCellConfigMap(cellConfigMap);
        //解析表
        return doParseSheet(sheet, sheetConfig);
    }

    private SheetParseResult doParseSheet(Sheet sheet, SheetConfig sheetConfig) {
        SheetParseResult parseResult = new SheetParseResult();
        parseResult.setRowBeanClass(sheetConfig.getRowClass());
        parseResult.setSheetName(sheetConfig.getSheetName());
        int startRowNum = sheetConfig.getTitleRowNum();
        int lastRowNum = sheet.getLastRowNum();
        Map<Integer, RowParseResult> rowParseResultMap = new HashMap<>(lastRowNum);
        parseResult.setRowParseResultMap(rowParseResultMap);
        //遍历数据
        for (int rowNum = startRowNum + 1; rowNum < lastRowNum + 1; rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null) {
                continue;
            }
            RowParseResult rowParseResult = parseRow(row, sheetConfig);
            rowParseResultMap.put(rowNum, rowParseResult);
        }
        return parseResult;
    }


    /**
     * 解析Row
     */
    private RowParseResult parseRow(Row row, SheetConfig sheetConfig) {
        RowParseResult.RowParseResultBuilder rowParseResultBuilder = RowParseResult.builder();
        try {
            Object rowBean = doParseRow(row, sheetConfig);
            rowValidate(rowBean, sheetConfig);
            return rowParseResultBuilder.rowNum(row.getRowNum()).isFailure(false).rowBean(rowBean).build();
        } catch (Throwable throwable) {
            String causeMessage = null;
            if (throwable instanceof ImportException) {
                ImportException importException = (ImportException) throwable;
                causeMessage = importException.getMessage();
            } else {
                log.warn("解析Excel行数据错误,行号=" + row.getRowNum(), throwable);
                causeMessage = "格式错误";
            }
            return rowParseResultBuilder.rowNum(row.getRowNum()).isFailure(true).causeMessage(causeMessage).build();
        }
    }

    public Object doParseRow(Row row, SheetConfig sheetConfig) {
        boolean isBlank = true;
        Map<Integer, CellConfig> cellConfigMap = sheetConfig.getCellConfigMap();
        Class targetClass = sheetConfig.getRowClass();
        BeanWrapper beanWrapper = new BeanWrapperImpl(targetClass);
        for (Map.Entry<Integer, CellConfig> cellConfigEntry : cellConfigMap.entrySet()) {
            Cell cell = row.getCell(cellConfigEntry.getKey());
            if (cell == null) {
                continue;
            }
            Object cellValue = getCellValue(cell, cellConfigEntry.getValue());
            if (cellValue != null && StringUtils.isNotBlank(cellValue.toString())) {
                isBlank = false;
            }
            try {
                beanWrapper.setPropertyValue(cellConfigEntry.getValue().getFieldName(), cellValue);
            } catch (BeansException ex) {
                log.warn("Excel导入时BeanWrapper设置属性值异常", ex);
                throw new ImportException(ImportErrorCode.CELL_TYPE_ERROR, "格式错误");
            }
        }
        //空行处理
        if (isBlank) {
            return null;
        }
        return beanWrapper.getWrappedInstance();
    }


    /**
     * 获取单元格的值
     */
    public Object getCellValue(Cell cell, CellConfig cellConfig) {
        if (cell == null || cellConfig == null || cellConfig.getFieldName() == null) {
            return null;
        }
        CellType cellType = cell.getCellType();
        Object cellValue;
        switch (cellType) {
            case NUMERIC:
                //有可能是字符串类型被设置为了数字
                if (String.class.getName().equals(cellConfig.getCellType())) {
                    cellValue = NumberToTextConverter.toText(cell.getNumericCellValue());
                    break;
                }
                if (DateUtil.isCellDateFormatted(cell)) {
                    cellValue = cell.getDateCellValue();
                    break;
                }
                cellValue = cell.getNumericCellValue();
                break;
            case STRING:
                cellValue = cell.getStringCellValue();
                cellValue = StringUtils.trimToEmpty(cellValue.toString());
                break;
            case FORMULA:
                cellValue = cell.getCellFormula();
                break;
            case BLANK:
                cellValue = "";
                break;
            case BOOLEAN:
                cellValue = cell.getBooleanCellValue();
                break;
            case ERROR:
                throw new ImportException(ImportErrorCode.CELL_DATA_ERROR, "[" + cellConfig.getCellName() + "]格式错误");
            default:
                log.warn("cell解析未知类型：" + cellType.getClass().getName());
                throw new ImportException(ImportErrorCode.CELL_TYPE_UNKOWN, "[" + cellConfig.getCellName() + "]格式错误");
        }
        return cellValue;
    }
}
