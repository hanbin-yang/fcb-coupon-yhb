package com.fcb.coupon.common.excel.excetption;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月17日 14:52:00
 */
public class ImportException extends RuntimeException {


    private String code;

    private String message;

    public ImportException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static ImportException newValidatorException(String message) {
        return new ImportException(ImportErrorCode.ROW_CUSTOM_VALID_ERROR, message);
    }
}
