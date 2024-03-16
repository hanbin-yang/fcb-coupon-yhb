package com.fcb.coupon.common.excel;

import com.fcb.coupon.common.excel.exporter.CSVExporter;
import com.fcb.coupon.common.excel.exporter.ExcelExporter;
import com.fcb.coupon.common.excel.exporter.Exporter;
import com.fcb.coupon.common.excel.importer.CSVImporter;
import com.fcb.coupon.common.excel.importer.ExcelImporter;
import com.fcb.coupon.common.excel.importer.FailFastCSVImporter;
import com.fcb.coupon.common.excel.importer.Importer;
import com.fcb.coupon.common.excel.support.DefaultImportValidator;
import com.fcb.coupon.common.excel.support.ImportValidator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.validation.Validator;

/**
 * @author 唐陆军
 * @Description excel导入导出自动配置
 * @createTime 2021年06月18日 10:17:00
 */
@Configuration
public class ExcelAutoConfigure {

    /*
     * @description 注入excel解析器
     * @author 唐陆军

     * @param: validatorObjectProvider
     * @date 2021-6-18 10:20

     * @return: com.fcb.coupon.common.excel.importer.Importer
     */
    @Bean
    public Importer excelImporter(ObjectProvider<Validator> validatorObjectProvider) {
        ExcelImporter excelImporter = new ExcelImporter();
        Validator validator = validatorObjectProvider.getIfAvailable();
        if (validator != null) {
            ImportValidator importValidator = new DefaultImportValidator(validator);
            excelImporter.setImportValidator(importValidator);
        }
        return excelImporter;
    }

    @Bean
    public Importer csvImporter(ObjectProvider<Validator> validatorObjectProvider) {
        CSVImporter csvImporter = new CSVImporter();
        Validator validator = validatorObjectProvider.getIfAvailable();
        if (validator != null) {
            ImportValidator importValidator = new DefaultImportValidator(validator);
            csvImporter.setImportValidator(importValidator);
        }
        return csvImporter;
    }

    @Bean
    public Importer failFastCSVImporter(ObjectProvider<Validator> validatorObjectProvider) {
        FailFastCSVImporter csvImporter = new FailFastCSVImporter();
        Validator validator = validatorObjectProvider.getIfAvailable();
        if (validator != null) {
            ImportValidator importValidator = new DefaultImportValidator(validator);
            csvImporter.setImportValidator(importValidator);
        }
        return csvImporter;
    }

    @Bean
    public Exporter excelExporter() {
        return new ExcelExporter();
    }

    @Bean
    public Exporter csvExporter() {
        return new CSVExporter();
    }
}
