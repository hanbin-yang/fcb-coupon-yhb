package com.fcb.coupon.backend.infra.pageinfo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author YangHanBin
 * @date 2021-06-18 10:44
 */
@ConfigurationProperties(prefix="promotion.coupon-page-config")
@Configuration(value = "COUPON_PAGE_CONFIG")
@Getter
@Setter
public class CouponPageConfigProperties {
    @JSONField(name = "EVERY_DAY_LIMIT_GIVE_RULE")
    private String everyDayLimitGiveRule;

    @JSONField(name = "COUPON_USE_FIRST_PAYMENTS")
    private String couponUseFirstPayments;

    @JSONField(name = "COUPON_USER_SCOPE")
    private String couponUserScope;

    @JSONField(name = "COUPON_TYPE")
    private String couponType;

    @JSONField(name = "COUPON_MERCHANT_ISOLATION")
    private String couponMerchantIsolation;

    @JSONField(name = "COUPON_MEMBER_TYPE_CONFIG")
    private String couponMemberTypeConfig;

    @JSONField(name = "COUPON_MEMBER_LEVEL_CONFIG")
    private String couponMemberLevelConfig;

    @JSONField(name = "COUPON_GIVE_RULE_ALL")
    private String couponGiveRuleAll;

    @JSONField(name = "COUPON_DISCOUNT_TYPE")
    private String coupon_Discount_Type;

    @JSONField(name = "COUPON_DEPARTMENT")
    private String couponDepartment;

    @JSONField(name = "CAN_COUPON_REGION_SHOW")
    private String canCouponRegionShow;

    @JSONField(name = "COUPON_DEDUCTION_TYPE")
    private String couponDeductionType;

    @JSONField(name = "COUPON_RECEPTION_DIRECTLY_PAGE")
    private String couponReceptionDirectlyPage;

    @JSONField(name = "COUPON_PUSH_PLATFORM_CONFIG")
    private String couponPushPlatformConfig;

    @JSONField(name = "COUPON_TYPE_HOUSE")
    private String couponTypeHouse;

    @JSONField(name = "COUPON_DISCOUNT_TYPE_HOUSE")
    private String couponDiscountTypeHouse;

    @JSONField(name = "COUPON_GIVE_RULE_ALL_HOUSE")
    private String couponGiveRuleAllHouse;

    @JSONField(name = "COUPON_CROWD_SCOPE")
    private String couponCrowdScope;
}

