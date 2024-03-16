package com.fcb.coupon.backend.model.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * @author mashiqiong
 * @date 2021-08-02 16:30
 */
@Data
@ApiModel(description = "管理后台->优惠券活动列表->添加后条件查询适用组织 入参")
public class MktUseRuleByIdsDto {

    /**
     * 商家ID
     */
    private Long merchantId;
    /**
     * 集团ID
     */
    private Long groupId;
    /**
     * 商家名称
     */
    private String merchantName;
    /**
     * 券活动ID
     */
    private Long themeRef;
    /**
     * 类型
     */
    private Integer refType;
    /**
     * 适用范围ID
     */
    private List<Integer> ruleTypeList;
    /**
     * 所属店铺ids（针对店铺）
     */
    private List<Long> storeIds;
    /**
     * 需要查询的范围限制ids
     */
    private List<Long> merchantIds;
    /**
     * 店铺编码
     */
    private String buildCode;
    /**
     * 商家代码
     */
    private String merchantCode;

    /**
     * 当前页码
     */
    private Integer startItem;

    /**
     * 页面pageSize
     */
    private Integer itemsPerPage;

}
