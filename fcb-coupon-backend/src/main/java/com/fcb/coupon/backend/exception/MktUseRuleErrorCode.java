package com.fcb.coupon.backend.exception;

import com.fcb.coupon.common.exception.ResponseErrorCode;

/**
 * @author HanBin_Yang
 * @since 2021/6/21 11:40
 */
public enum MktUseRuleErrorCode implements ResponseErrorCode {
    ORG_NOT_EXIST("240001", "该%s不存在"),
    STORE_OFFLINE("240002", "该店铺未在对应端上架"),
    IMPORT_TEMPLATE_FORMAT_ERROR("240003", "模板格式不正确，请使用正确的模板"),
    IMPORT_DATA_EMPTY("240004", "导入模板没有数据"),
    NO_GROUP_AND_MERCHANT_PERMISSIONS("240005","该用户没有任何集团和商家的权限"),
    NO_STORE_PERMISSIONS("240006", "该用户没有任何店铺权限"),
    ORG_TYPE_NOT_SUPPORT("240007", "组织类型不支持"),
    DOWNLOAD_TEMPLATE_FAIL("240008", "下载导入模板失败"),
    STORE_NOT_EXIST("240009", "该店铺不存在"),
    ;

    private final String code;

    private final String message;

    MktUseRuleErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
