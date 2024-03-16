package com.fcb.coupon.app.infra.pageinfo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author YangHanBin
 * @date 2021-06-18 11:06
 */
@Getter
@Setter
@ConfigurationProperties(prefix="promotion.page-config")
@Configuration(value = "PAGE_CONFIG")
public class PageConfigProperties {
    @JSONField(name = "applicablePlatform")
    private String applicablePlatform;
    @JSONField(name = "applyActivityTypeList")
    private String applyActivityTypeList;
    @JSONField(name = "CHECKOUT_MODE")
    private String checkoutMode;
    @JSONField(name = "freeShipShowFlg")
    private String freeShipShowFlg;
    @JSONField(name = "notRealizeMerchantFunction")
    private String notRealizeMerchantFunction;
    @JSONField(name = "overlimitRule")
    private String overlimitRule;
    @JSONField(name = "packagePromType")
    private String packagePromType;
    @JSONField(name = "priorityShowFlg")
    private String priorityShowFlg;
    @JSONField(name = "promTypeList")
    private String promTypeList;
    @JSONField(name = "publishShowFlg")
    private String publishShowFlg;
    @JSONField(name = "resetCommission")
    private String resetCommission;
    @JSONField(name = "singlePromType")
    private String singlePromType;
    @JSONField(name = "themeShowFlag")
    private String themeShowFlag;
    @JSONField(name = "userScope")
    private String userScope;
}
