package com.fcb.coupon.backend.infra.pageinfo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author YangHanBin
 * @date 2021-06-18 11:05
 */
@Getter
@Setter
@ConfigurationProperties(prefix="promotion.page-config-not-same")
@Configuration(value = "PAGE_CONFIG_NOT_SAME")
public class PageConfigNotSameProperties {
    @JSONField(name = "CAN_ORDER_MULTIPLE_SHOW")
    private String canOrderMultipleShow;
    @JSONField(name = "CAN_ORDER_START_NUM_SHOW")
    private String canOrderStartNumShow;
    @JSONField(name = "CAN_SHARE_COUPON_CONFIG")
    private String canShareCouponConfig;
    @JSONField(name = "merchantType")
    private String merchantType;
    @JSONField(name = "NEED_MEMBER_LEVEL_CONFIG")
    private String needMemberLevelConfig;
    @JSONField(name = "NEED_MEMBER_TYPE_CONFIG")
    private String needMemberTypeConfig;
    @JSONField(name = "NEED_PLATFORM_CONFIG")
    private String needPlatformConfig;
    @JSONField(name = "NEED_USER_TYPE_CONFIG")
    private String needUserTypeConfig;
    @JSONField(name = "patchGroupModel")
    private String patchGroupModel;
    @JSONField(name = "promotionCreateType")
    private String promotionCreateType;
    @JSONField(name = "SELECTION_NEED_BRAND_CONFIG")
    private String selectionNeedBrandConfig;
}
