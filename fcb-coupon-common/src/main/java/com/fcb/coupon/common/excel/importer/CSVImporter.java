package com.fcb.coupon.common.excel.importer;

import com.fcb.coupon.common.excel.bean.CellConfig;
import com.fcb.coupon.common.excel.bean.RowParseResult;
import com.fcb.coupon.common.excel.bean.SheetConfig;
import com.fcb.coupon.common.excel.bean.SheetParseResult;
import com.fcb.coupon.common.excel.excetption.ImportErrorCode;
import com.fcb.coupon.common.excel.excetption.ImportException;
import com.fcb.coupon.common.excel.support.AnnotationConfigHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 唐陆军
 * @Description CSV解析
 * @createTime 2021年06月17日 15:53:00
 */
@Slf4j
public class CSVImporter extends AbstractImporter {


    @Override
    public SheetParseResult parse(InputStream inputStream, Class rowClass) throws ImportException {
        return parse(inputStream, rowClass, StandardCharsets.UTF_8);
    }

    @Override
    public SheetParseResult parse(InputStream inputStream, Class rowClass, Charset charset) throws ImportException {
        BufferedReader bufferedReader = null;
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            InputStreamReader reader = new InputStreamReader(bufferedInputStream, charset);
            return parseSheet(reader, rowClass);
        } catch (ImportException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("csv import error", ex);
            throw new ImportException(ImportErrorCode.ShEET_IMPORT_ERROR, "导入失败");
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception ex) {
                    log.error("关闭 csv导入 文件流失败", ex);
                }
            }
        }
    }


    /**
     * 解析Sheet
     */
    private SheetParseResult parseSheet(Reader reader, Class rowClass) throws IOException, ImportException {
        SheetConfig sheetConfig = AnnotationConfigHelper.parseSheetConfig(rowClass);
        //解析单元格配置
        Map<Integer, CellConfig> cellConfigMap = AnnotationConfigHelper.parseRowClassConfig(rowClass);
        if (CollectionUtils.isEmpty(cellConfigMap)) {
            throw new ImportException(ImportErrorCode.CELL_CONFIG_ERROR, "无配置任何导入列");
        }
        sheetConfig.setCellConfigMap(cellConfigMap);
        //List<String> header = sheetConfig.getCellConfigMap().values().stream().map(m -> m.getCellName()).collect(Collectors.toList());
        CSVFormat csvFormat = CSVFormat.DEFAULT.withAllowMissingColumnNames();
        try (CSVParser parser = csvFormat.parse(reader)) {
            //判断最大导入数量
            if (sheetConfig.getMaxCount() > 0 && parser.getRecordNumber() > sheetConfig.getMaxCount()) {
                throw new ImportException(ImportErrorCode.ShEET_LIMIT_MAX_ERROR, "只允许导入" + sheetConfig.getMaxCount() + "条数据");
            }
            return doParseSheet(parser, sheetConfig);
        }
    }

    private SheetParseResult doParseSheet(CSVParser parser, SheetConfig sheetConfig) throws ImportException {
        SheetParseResult parseResult = new SheetParseResult();
        parseResult.setRowBeanClass(sheetConfig.getRowClass());
        parseResult.setSheetName(sheetConfig.getSheetName());
        //csv是第1行算起
        int startRowNum = sheetConfig.getTitleRowNum() + 1;
        Map<Integer, RowParseResult> rowParseResultMap = new HashMap<>();
        parseResult.setRowParseResultMap(rowParseResultMap);
        //遍历数据
        for (final CSVRecord record : parser) {
            if (record.getRecordNumber() < startRowNum) {
                continue;
            }
            //解析标题列
            if (record.getRecordNumber() == startRowNum) {
                Map<Integer, CellConfig> cellConfigMap = AnnotationConfigHelper.parseCSVCellConfig(sheetConfig.getRowClass(), record);
                sheetConfig.setCellConfigMap(cellConfigMap);
                continue;
            }

            RowParseResult rowParseResult = parseRow(record, sheetConfig);
            //由子类对行处理结果拓展
            handleRowParseResult(rowParseResult);

            rowParseResultMap.put((int) record.getRecordNumber(), rowParseResult);
        }
        return parseResult;
    }

    protected void handleRowParseResult(RowParseResult rowParseResult) {

    }

    /**
     * 解析Row
     */
    private RowParseResult parseRow(CSVRecord record, SheetConfig sheetConfig) throws ImportException {
        RowParseResult.RowParseResultBuilder rowParseResultBuilder = RowParseResult.builder();
        try {
            Object rowBean = doParseRow(record, sheetConfig);
            rowValidate(rowBean, sheetConfig);
            return rowParseResultBuilder.rowNum((int) record.getRecordNumber()).isFailure(false).rowBean(rowBean).build();
        } catch (Throwable throwable) {
            String causeMessage = null;
            if (throwable instanceof ImportException) {
                ImportException importException = (ImportException) throwable;
                causeMessage = importException.getMessage();
            } else {
                log.warn("解析CSV行数据异常,数据=" + record.toString(), throwable);
                causeMessage = "格式错误";
            }
            return rowParseResultBuilder.rowNum((int) record.getRecordNumber()).isFailure(true).causeMessage(causeMessage).build();
        }
    }


    public Object doParseRow(CSVRecord record, SheetConfig sheetConfig) throws ImportException {
        boolean isBlank = true;
        Map<Integer, CellConfig> cellIndexMapping = sheetConfig.getCellConfigMap();
        Class targetClass = sheetConfig.getRowClass();
        BeanWrapper beanWrapper = new BeanWrapperImpl(targetClass);
        beanWrapper.registerCustomEditor(Date.class, EXCEL_DATE_EDITOR);
        for (Map.Entry<Integer, CellConfig> cellConfigEntry : cellIndexMapping.entrySet()) {
            Integer cellNum = cellConfigEntry.getKey();
            CellConfig cellConfig = cellConfigEntry.getValue();
            String cellValue = record.get(cellNum);
            //有一列有数据，说明不是空行
            if (StringUtils.isNotBlank(cellValue)) {
                isBlank = false;
            }
            try {
                beanWrapper.setPropertyValue(cellConfig.getFieldName(), cellValue);
            } catch (BeansException ex) {
                log.warn("CSV导入时BeanWrapper设置属性值异常，列=" + cellConfig.getCellName() + ",类型=" + cellConfig.getCellType() + ",值=" + cellValue, ex);
                throw new ImportException(ImportErrorCode.CELL_TYPE_ERROR, cellConfig.getCellName() + "列【" + cellValue + "】格式错误");
            }
        }
        //空行处理
        if (isBlank) {
            return null;
        }
        return beanWrapper.getWrappedInstance();
    }


}
