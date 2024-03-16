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
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月17日 19:19:00
 */
@Slf4j
public class CSVExporter extends AbstractExporter {

    @Override
    public void export(OutputStream outputStream, Class rowClass, List<?> rowDatas) {
        export(outputStream, rowClass, rowDatas, StandardCharsets.UTF_8);
    }

    @Override
    public void export(OutputStream outputStream, Class rowClass, List<? extends Object> rowDatas, Charset charset) {
        CSVPrinter csvPrinter = null;
        try {
            OutputStreamWriter ow = new OutputStreamWriter(outputStream, charset);
            PrintWriter printWriter = new PrintWriter(ow);
            csvPrinter = CSVFormat.DEFAULT.print(printWriter);
            doExport(csvPrinter, rowClass, rowDatas);
        } catch (Exception e) {
            log.error("CSV导出异常", e);
            throw new ImportException(ImportErrorCode.ShEET_EXPORT_ERROR, "CSV导出异常");
        } finally {
            try {
                csvPrinter.flush();
                csvPrinter.close();
            } catch (IOException e) {
                log.error("CSV导出输出流关闭异常", e);
            }
        }
    }

    private void doExport(CSVPrinter csvPrinter, Class rowClass, List<? extends Object> rowDatas) throws IOException {
        SheetConfig sheetConfig = AnnotationConfigHelper.parseSheetConfig(rowClass);
        Map<Integer, CellConfig> cellConfigMap = AnnotationConfigHelper.parseRowClassConfig(rowClass);
        if (CollectionUtils.isEmpty(cellConfigMap)) {
            throw new ImportException(ImportErrorCode.CELL_CONFIG_ERROR, "无配置任何导出列");
        }
        sheetConfig.setCellConfigMap(cellConfigMap);

        if (sheetConfig.getTitleRowNum() > 0) {
            csvPrinter.printRecord(sheetConfig.getSheetDesc());
        }

        List<String> headerRecords = new ArrayList<>();
        //创建表头
        for (Map.Entry<Integer, CellConfig> entry : sheetConfig.getCellConfigMap().entrySet()) {
            headerRecords.add(entry.getValue().getCellName());
        }

        //判断是否需要创建错误列
        boolean isHasErrorCell = isCreateErrorCell(rowDatas);
        if (isHasErrorCell) {
            headerRecords.add(ImportConstant.ERROR_CELL_TITLE);
        }
        csvPrinter.printRecord(headerRecords);

        //创建表数据行
        if (CollectionUtils.isEmpty(rowDatas)) {
            return;
        }

        for (int i = 0; i < rowDatas.size(); i++) {
            Object bean = rowDatas.get(i);
            if (bean == null) {
                continue;
            }
            List<String> rowRecords = new ArrayList<>();
            BeanWrapper beanWrapper = new BeanWrapperImpl(bean);
            for (Map.Entry<Integer, CellConfig> entry : sheetConfig.getCellConfigMap().entrySet()) {
                try {
                    Object cellValue = beanWrapper.getPropertyValue(entry.getValue().getFieldName());
                    if (cellValue == null) {
                        rowRecords.add("");
                    } else {
                        rowRecords.add(formatCellValue(entry.getValue(), cellValue));
                    }
                } catch (Exception ex) {
                    log.error("创建excel [{}] 单元设值失败[{}] ", bean.getClass().getName(), JSON.toJSONString(bean), ex);
                    throw ex;
                }
            }
            //有错误列
            if (isHasErrorCell) {
                if (bean instanceof ErrorBeanWrapper) {
                    ErrorBeanWrapper errorBean = (ErrorBeanWrapper) bean;
                    rowRecords.add(errorBean.getError());
                }
            }
            csvPrinter.printRecord(rowRecords);

        }
    }

    private String formatCellValue(CellConfig cellConfig, Object value) {
        String dataType = cellConfig.getCellType();
        //按配置类型转化数据
        if (ImportConstant.EXCEL_CELL_TYPE_DATE.equalsIgnoreCase(dataType)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return simpleDateFormat.format((Date) value);
        } else {
            return value.toString();
        }
    }
}
