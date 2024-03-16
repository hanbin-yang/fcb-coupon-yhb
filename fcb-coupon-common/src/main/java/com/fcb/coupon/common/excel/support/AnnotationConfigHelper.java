package com.fcb.coupon.common.excel.support;

import com.fcb.coupon.common.excel.annotation.XCell;
import com.fcb.coupon.common.excel.annotation.XSheet;
import com.fcb.coupon.common.excel.bean.CellConfig;
import com.fcb.coupon.common.excel.bean.SheetConfig;
import com.fcb.coupon.common.excel.excetption.ImportErrorCode;
import com.fcb.coupon.common.excel.excetption.ImportException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月17日 16:22:00
 */
@Slf4j
public class AnnotationConfigHelper {


    /**
     * 解析Sheet 表的配置
     */
    public static SheetConfig parseSheetConfig(Class rowClass) {
        XSheet xSheet = AnnotationUtils.findAnnotation(rowClass, XSheet.class);
        if (xSheet == null) {
            log.warn("target {} is not config sheet name ", rowClass.getName());
            throw new ImportException(ImportErrorCode.SHEET_CONFIG_ERROR, "Sheet解析配置有误");
        }
        SheetConfig sheetConfig = new SheetConfig();
        sheetConfig.setRowClass(rowClass);
        sheetConfig.setSheetName(xSheet.name());
        sheetConfig.setTitleRowNum(xSheet.titleRowNum());
        sheetConfig.setSheetDesc(xSheet.desc());
        sheetConfig.setRowValidatorClazzs(xSheet.rowValidatorClazzs());
        sheetConfig.setMaxCount(xSheet.maxCount());
        return sheetConfig;
    }


    /**
     * 解析标题
     */
    public static Map<Integer, CellConfig> parseCellConfig(Class rowBeanClass, Row titleRow) throws ImportException {
        try {
            //解析excel标题栏
            short lastCellNum = titleRow.getLastCellNum();
            Map<String, Integer> cellNameIndexMap = new HashMap<>(lastCellNum);
            for (int cellNum = 0; cellNum < lastCellNum; cellNum++) {
                Cell cell = titleRow.getCell(cellNum);
                if (cell == null || StringUtils.isEmpty(cell.getStringCellValue())) {
                    continue;
                }
                String cellName = cell.getStringCellValue();
                cellNameIndexMap.put(cellName, cellNum);
            }

            Field[] fields = rowBeanClass.getDeclaredFields();
            //excel标题跟字段名的映射
            Map<Integer, CellConfig> cellConfigMap = new HashMap<>(fields.length);
            for (Field field : fields) {
                XCell xCell = field.getAnnotation(XCell.class);
                if (xCell == null) {
                    continue;
                }
                CellConfig cellConfig = new CellConfig();
                //通过配置找到对于标题列
                if (!cellNameIndexMap.containsKey(xCell.name())) {
                    throw new ImportException(ImportErrorCode.ShEET_TITLE_ERROR, "模板错误，请检查模板");
                }
                Integer cellNum = cellNameIndexMap.get(xCell.name());
                cellConfig.setCellType(field.getType().getName());
                cellConfig.setFieldName(field.getName());
                cellConfig.setCellName(xCell.name());
                cellConfig.setCellNum(cellNum);
                cellConfigMap.put(cellNum, cellConfig);
            }

            return cellConfigMap;
        } catch (ImportException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("标题行解析异常：" + rowBeanClass.getTypeName(), ex);
            throw new ImportException(ImportErrorCode.SHEET_CONFIG_ERROR, "Sheet解析配置有误");
        }
    }

    /**
     * 解析bean的配置
     */
    public static Map<Integer, CellConfig> parseRowClassConfig(Class rowBeanClass) {
        Field[] fields = rowBeanClass.getDeclaredFields();
        Map<Integer, CellConfig> cellIndexMapping = new TreeMap<>();
        int cellNum = 0;
        for (Field field : fields) {
            XCell xCell = field.getAnnotation(XCell.class);
            if (xCell == null) {
                continue;
            }
            CellConfig cellConfig = new CellConfig();
            cellConfig.setFieldName(field.getName());
            cellConfig.setCellName(xCell.name());
            cellConfig.setCellNum(cellNum);
            if (xCell.cellNum() != -1) {
                cellConfig.setCellNum(xCell.cellNum());
            }
            cellConfig.setCellType(field.getType().getSimpleName());
            cellConfig.setPrecision(xCell.precision());
            cellConfig.setWrapText(xCell.wrapText());
            cellIndexMapping.put(cellConfig.getCellNum(), cellConfig);
            cellNum++;
        }
        return cellIndexMapping;
    }


    /**
     * 解析标题
     */
    public static Map<Integer, CellConfig> parseCSVCellConfig(Class rowBeanClass, CSVRecord record) throws ImportException {
        try {
            //解析excel标题栏
            Map<String, Integer> cellNameIndexMap = new HashMap<>(record.size());
            for (int cellNum = 0; cellNum < record.size(); cellNum++) {
                String cellName = record.get(cellNum);
                cellNameIndexMap.put(cellName, cellNum);
            }

            Field[] fields = rowBeanClass.getDeclaredFields();
            //excel标题跟字段名的映射
            Map<Integer, CellConfig> cellConfigMap = new HashMap<>(fields.length);
            for (Field field : fields) {
                XCell xCell = field.getAnnotation(XCell.class);
                if (xCell == null) {
                    continue;
                }
                CellConfig cellConfig = new CellConfig();
                //通过配置找到对于标题列
                if (!cellNameIndexMap.containsKey(xCell.name())) {
                    throw new ImportException(ImportErrorCode.ShEET_TITLE_ERROR, "模板错误，请检查模板");
                }
                Integer cellNum = cellNameIndexMap.get(xCell.name());
                cellConfig.setCellType(field.getType().getName());
                cellConfig.setFieldName(field.getName());
                cellConfig.setCellName(xCell.name());
                cellConfig.setCellNum(cellNum);
                cellConfigMap.put(cellNum, cellConfig);
            }

            return cellConfigMap;
        } catch (ImportException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("标题行解析异常：" + rowBeanClass.getTypeName(), ex);
            throw new ImportException(ImportErrorCode.SHEET_CONFIG_ERROR, "Sheet解析配置有误");
        }
    }
}
