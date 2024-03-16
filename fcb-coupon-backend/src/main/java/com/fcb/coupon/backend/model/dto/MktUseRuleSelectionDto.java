package com.fcb.coupon.backend.model.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author mashiqiong
 * @date 2021-08-03 09:54
 */
@Data
@ApiModel(description = "管理后台->优惠券活动列表->添加后条件查询适用组织 入参")
public class MktUseRuleSelectionDto {

    /**
     * 券活动ID
     */
    private Long themeRef;
    /**
     * 规则类型：0：券规则；1：卡规则；2：促销规则
     */
    private Integer refType;
    /**
     * 规则类型：0:地区限制 1：商家限制 2: 商品限制 按类目 3：商品限制 按品牌 4 ：商品限制 按产品 5：商品限制：按商品
     */
    private Integer RuleType;
    /**
     * 地区主键 或者商家主键
     */
    private Long limitRef;
    /**
     * 限制名称
     */
    private String refDescription;
    /**
     * 限制编码
     */
    private String extendRef;

    /**
     * 当前页码
     */
    private Integer startItem;

    /**
     * 页面pageSize
     */
    private Integer itemsPerPage;

}
