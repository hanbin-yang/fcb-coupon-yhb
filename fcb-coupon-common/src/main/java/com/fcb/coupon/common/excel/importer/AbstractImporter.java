package com.fcb.coupon.common.excel.importer;

import com.alibaba.fastjson.JSON;
import com.fcb.coupon.common.excel.bean.SheetConfig;
import com.fcb.coupon.common.excel.bean.SheetParseResult;
import com.fcb.coupon.common.excel.excetption.ImportErrorCode;
import com.fcb.coupon.common.excel.excetption.ImportException;
import com.fcb.coupon.common.excel.support.ExcelDateEditor;
import com.fcb.coupon.common.excel.support.ImportValidator;
import com.fcb.coupon.common.excel.support.RowValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.CollectionUtils;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月17日 16:55:00
 */
@Slf4j
public abstract class AbstractImporter implements Importer {

    protected static final ExcelDateEditor EXCEL_DATE_EDITOR = new ExcelDateEditor();

    protected Map<Class<?>, RowValidator> rowValidatorMap;

    public void setRowValidatorMap(Map<Class<?>, RowValidator> rowValidatorMap) {
        this.rowValidatorMap = rowValidatorMap;
    }

    protected ImportValidator importValidator;

    public void setImportValidator(ImportValidator importValidator) {
        this.importValidator = importValidator;
    }


    /**
     * 构造验证错误信息
     */
    protected String getValidationErrorMessage(ConstraintViolationException ex) {
        Set<ConstraintViolation<?>> errors = ex.getConstraintViolations();
        String errorMesssage = "";
        if (CollectionUtils.isEmpty(errors)) {
            return errorMesssage;
        }
        for (ConstraintViolation constraintViolation : ex.getConstraintViolations()) {
            errorMesssage = errorMesssage + constraintViolation.getMessage() + "\n";
        }
        return errorMesssage;
    }

    protected void rowValidate(Object rowBean, SheetConfig sheetConfig) {
        if (rowBean == null) {
            throw new ImportException(ImportErrorCode.ROW_EMPTY_ERROR, "数据为空");
        }
        try {
            //bean参数验证
            if (importValidator != null) {
                importValidator.validate(rowBean);
            }
        } catch (ConstraintViolationException throwable) {
            String causeMessage = getValidationErrorMessage(throwable);
            throw new ImportException(ImportErrorCode.ROW_VALID_ERROR, causeMessage);
        }
        //自定义的验证
        if (!ArrayUtils.isEmpty(sheetConfig.getRowValidatorClazzs())) {
            doRowCustomValidate(rowBean, sheetConfig.getRowValidatorClazzs());
        }
    }

    private RowValidator getRowValidatorClazz(Class<?> rowValidatorClass) {
        return rowValidatorMap.get(rowValidatorClass);
    }

    private void doRowCustomValidate(Object rowBean, Class<?>[] rowValidatorClazzs) {
        for (Class<?> rowValidatorClazz : rowValidatorClazzs) {
            RowValidator rowValidator = getRowValidatorClazz(rowValidatorClazz);
            if (rowValidator == null) {
                throw new ImportException(ImportErrorCode.ROW_VALID_CONFIG_ERROR, "行校验器[" + rowValidatorClazz.getSimpleName() + "]找不到");
            }
            try {
                rowValidator.validate(rowBean);
            } catch (ImportException ex) {
                throw ex;
            } catch (Exception ex) {
                log.error("导入行数据校验异常：" + JSON.toJSONString(rowBean), ex);
                throw new ImportException(ImportErrorCode.ROW_VALID_ERROR, "自定义校验异常");
            }
        }
    }

    @Override
    public <T> List<T> parseSimple(InputStream inputStream, Class<T> rowClass, Charset charset) throws ImportException {
        SheetParseResult result = parse(inputStream, rowClass, charset);
        return result.getRowParseResultMap().entrySet().stream()
                .filter(m -> !m.getValue().getIsFailure())
                .map(m -> (T) m.getValue().getRowBean())
                .collect(Collectors.toList());
    }

    @Override
    public <T> List<T> parseSimple(InputStream inputStream, Class<T> rowClass) throws ImportException {
        return parseSimple(inputStream, rowClass, StandardCharsets.UTF_8);
    }
}
