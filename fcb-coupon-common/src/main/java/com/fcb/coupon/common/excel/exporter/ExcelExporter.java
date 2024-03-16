package com.fcb.coupon.common.excel.exporter;

import com.alibaba.fastjson.JSON;
import com.fcb.coupon.common.excel.bean.CellConfig;
import com.fcb.coupon.common.excel.bean.ErrorBeanWrapper;
import com.fcb.coupon.common.excel.bean.SheetConfig;
import com.fcb.coupon.common.excel.constant.ImportConstant;
import com.fcb.coupon.common.excel.excetption.ImportErrorCode;
import com.fcb.coupon.common.excel.excetption.ImportException;
import com.fcb.coupon.common.excel.support.AnnotationConfigHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月17日 15:56:00
 */
@Slf4j
public class ExcelExporter extends AbstractExporter {

    @Override
    public void export(OutputStream outputStream, Class rowClass, List<?> rowDatas, Charset charset) {

    }

    @Override
    public void export(OutputStream outputStream, Class rowClass, List<? extends Object> rowDatas) {
        Workbook workbook = doExport(rowClass, rowDatas);
        try {
            workbook.write(outputStream);
            workbook.close();
        } catch (Exception e) {
            log.error("Excel导出异常", e);
            throw new ImportException(ImportErrorCode.ShEET_EXPORT_ERROR, "Excel导出异常");
        } finally {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                log.error("Excel导出输出流关闭异常", e);
            }
        }
    }

    private Workbook doExport(Class rowClass, List<? extends Object> rowDatas) {
        //Workbook workbook = new XSSFWorkbook();
        Workbook workbook = new SXSSFWorkbook();
        SheetConfig sheetConfig = AnnotationConfigHelper.parseSheetConfig(rowClass);
        Map<Integer, CellConfig> cellConfigMap = AnnotationConfigHelper.parseRowClassConfig(rowClass);
        if (CollectionUtils.isEmpty(cellConfigMap)) {
            throw new ImportException(ImportErrorCode.CELL_CONFIG_ERROR, "无配置任何导出列");
        }
        sheetConfig.setCellConfigMap(cellConfigMap);
        buildSheet(workbook, sheetConfig, rowDatas);
        return workbook;
    }

    private void buildSheet(Workbook workbook, SheetConfig sheetConfig, List<? extends Object> rowDatas) {
        if (CollectionUtils.isEmpty(sheetConfig.getCellConfigMap())) {
            throw new ImportException(ImportErrorCode.CELL_EMPTY, sheetConfig.getSheetName() + "未配置任何列！");
        }
        Sheet sheet = workbook.createSheet(sheetConfig.getSheetName());

        //创建说明行
        if (!StringUtils.isEmpty(sheetConfig.getSheetDesc()) && sheetConfig.getTitleRowNum() > 0) {
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellValue(sheetConfig.getSheetDesc());
        }
        //创建表头
        Row row = sheet.createRow(sheetConfig.getTitleRowNum());
        for (Map.Entry<Integer, CellConfig> entry : sheetConfig.getCellConfigMap().entrySet()) {
            createCell(workbook, sheet, row, entry.getKey(), entry.getValue());
        }
        //判断是否需要创建错误列
        boolean isHasErrorCell = isCreateErrorCell(rowDatas);
        int maxCellIndex = Collections.max(sheetConfig.getCellConfigMap().keySet());
        int errorCellIndex = maxCellIndex + 1;
        if (isHasErrorCell) {
            createErrorCell(workbook, row, errorCellIndex, ImportConstant.ERROR_CELL_TITLE);
        }
        //冻结标题行
        sheet.createFreezePane(0, sheetConfig.getTitleRowNum() + 1, 0, sheetConfig.getTitleRowNum() + 1);
        //创建表数据行
        if (CollectionUtils.isEmpty(rowDatas)) {
            return;
        }

        for (int i = 0; i < rowDatas.size(); i++) {
            int currentRowIndex = sheetConfig.getTitleRowNum() + 1 + i;
            Row dataRow = sheet.createRow(currentRowIndex);
            Object bean = rowDatas.get(i);
            if (bean == null) {
                continue;
            }
            //有错误列
            if (isHasErrorCell) {
                if (bean instanceof ErrorBeanWrapper) {
                    ErrorBeanWrapper errorBean = (ErrorBeanWrapper) bean;
                    bean = errorBean.getBean();
                    createErrorDataCell(workbook, dataRow, errorCellIndex, errorBean.getError());
                }
            }
            BeanWrapper beanWrapper = new BeanWrapperImpl(bean);
            for (Map.Entry<Integer, CellConfig> entry : sheetConfig.getCellConfigMap().entrySet()) {
                try {
                    createDataCell(workbook, dataRow, entry.getKey(), entry.getValue(), beanWrapper);
                } catch (Exception ex) {
                    log.error("创建excel [{}] 单元设值失败[{}] ", bean.getClass().getName(), JSON.toJSONString(bean), ex);
                    throw ex;
                }
            }
        }
    }


    /*
    创建标题列
     */
    private Cell createCell(Workbook workbook, Sheet sheet, Row row, int cellIndex, CellConfig cellConfig) {
        Cell cell = row.createCell(cellIndex);
        cell.setCellValue(cellConfig.getCellName());
        DataFormat cellDataFormat = workbook.createDataFormat();
        String dataFormat = getDataFormat(cellConfig);
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setDataFormat(cellDataFormat.getFormat(dataFormat));
        sheet.setDefaultColumnStyle(cellIndex, cellStyle);
        return cell;
    }

    /**
     * 创建错误列
     */
    private Cell createErrorCell(Workbook workbook, Row row, int errorCellNum, String cellValue) {
        //单元格样式
        CellStyle cellStyle = workbook.createCellStyle();
        Font cellFont = workbook.createFont();
        cellFont.setColor(Font.COLOR_RED);
        cellFont.setFontHeightInPoints((short) 12);
        cellFont.setFontName("黑体");
        cellStyle.setFont(cellFont);
        //标题行最后一列增加 错误信息
        Cell errorCell = row.getCell(errorCellNum);
        if (errorCell == null) {
            errorCell = row.createCell(errorCellNum);
        }
        errorCell.setCellValue(cellValue);
        errorCell.setCellStyle(cellStyle);
        return errorCell;
    }

    /**
     * 创建数据列
     *
     * @param row
     * @param cellIndex
     * @param cellConfig
     * @param beanWrapper
     * @return
     */
    private Cell createDataCell(Workbook workbook, Row row, int cellIndex, CellConfig cellConfig, BeanWrapper beanWrapper) {
        Cell cell = row.createCell(cellIndex);
        Object cellValue = beanWrapper.getPropertyValue(cellConfig.getFieldName());
        if (cellValue != null) {
            setCellValue(cellConfig, cell, cellValue);
        }
        if (cellConfig.getWrapText()) {
            // 设置自动换行
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setWrapText(true);
            cell.setCellStyle(cellStyle);
        }
        return cell;
    }

    private Cell createErrorDataCell(Workbook workbook, Row row, int cellIndex, String cellValue) {
        Cell cell = createErrorCell(workbook, row, cellIndex, cellValue);
        cell.setCellValue(cellValue);
        return cell;
    }


    private String getDataFormat(CellConfig cellConfig) {
        String dataType = cellConfig.getCellType();
        String dataFormat = null;
        //按配置类型转化数据
        if (ImportConstant.EXCEL_CELL_TYPE_STRING.equalsIgnoreCase(dataType)
                || ImportConstant.EXCEL_CELL_TYPE_BIG_DECIMAL.equalsIgnoreCase(dataType)) {
            dataFormat = "@";
        } else if (ImportConstant.EXCEL_CELL_TYPE_DOUBLE.equalsIgnoreCase(dataType)) {
            int precision = cellConfig.getPrecision();
            dataFormat = "0";
            if (precision > 0) {
                dataFormat += ".";
                for (int i = 1; i <= precision; i++) {
                    dataFormat += "0";
                }
            }
        } else if (ImportConstant.EXCEL_CELL_TYPE_INTEGER.equalsIgnoreCase(dataType)) {
            dataFormat = "0";
        } else if (ImportConstant.EXCEL_CELL_TYPE_LONG.equalsIgnoreCase(dataType)) {
            dataFormat = "0";
        } else if (ImportConstant.EXCEL_CELL_TYPE_DATE.equalsIgnoreCase(dataType)) {
            dataFormat = "yyyy/m/d h:mm:s";
        } else if (ImportConstant.EXCEL_CELL_TYPE_BOOLEAN.equalsIgnoreCase(dataType)) {
            dataFormat = "G/通用格式";
        } else if (ImportConstant.EXCEL_CELL_TYPE_ENUM.equalsIgnoreCase(dataType)) {
            //枚举值需要生产下拉列表
            dataFormat = "@";
        } else {
            dataFormat = "@";
        }
        return dataFormat;
    }

    private void setCellValue(CellConfig cellConfig, Cell cell, Object value) {
        String dataType = cellConfig.getCellType();
        //按配置类型转化数据
        if (ImportConstant.EXCEL_CELL_TYPE_STRING.equalsIgnoreCase(dataType)) {
            cell.setCellValue(value.toString());
        } else if (ImportConstant.EXCEL_CELL_TYPE_DOUBLE.equalsIgnoreCase(dataType)) {
            cell.setCellValue((double) value);
        } else if (ImportConstant.EXCEL_CELL_TYPE_BIG_DECIMAL.equalsIgnoreCase(dataType)) {
            cell.setCellValue(((BigDecimal) value).doubleValue());
        } else if (ImportConstant.EXCEL_CELL_TYPE_INTEGER.equalsIgnoreCase(dataType)) {
            cell.setCellValue((int) value);
        } else if (ImportConstant.EXCEL_CELL_TYPE_LONG.equalsIgnoreCase(dataType)) {
            cell.setCellValue((long) value);
        } else if (ImportConstant.EXCEL_CELL_TYPE_DATE.equalsIgnoreCase(dataType)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String cellValue = simpleDateFormat.format((Date) value);
            cell.setCellValue(cellValue);
        } else if (ImportConstant.EXCEL_CELL_TYPE_BOOLEAN.equalsIgnoreCase(dataType)) {
            cell.setCellValue((boolean) value);
        } else {
            cell.setCellValue(value.toString());
        }
    }

}