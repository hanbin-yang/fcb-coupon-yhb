package com.fcb.coupon.common.excel.support;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.beans.PropertyEditorSupport;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月18日 09:46:00
 */
public class ExcelDateEditor extends PropertyEditorSupport {

    private static final String DATE_PATTERN = "yyyy-MM-dd";

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Override
    public void setAsText(@Nullable String text) throws IllegalArgumentException {
        if (!StringUtils.hasText(text)) {
            // Treat empty String as null value.
            setValue(null);
        } else {
            try {
                setValue(DateUtils.parseDate(text, DATE_PATTERN, DATE_TIME_PATTERN));
            } catch (ParseException ex) {
                throw new IllegalArgumentException("Could not parse date: " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public String getAsText() {
        Date value = (Date) getValue();
        return (value != null ? DateFormatUtils.format(value, DATE_TIME_PATTERN) : "");
    }

}
