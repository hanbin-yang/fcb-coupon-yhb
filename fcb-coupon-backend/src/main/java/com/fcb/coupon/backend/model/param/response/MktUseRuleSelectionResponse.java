package com.fcb.coupon.backend.model.param.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 营销中心->优惠券管理->优惠券活动列表 出参
 * @author mashiqiong
 * @date 2021-08-03 09:54
 */
@ApiModel(value="规则设置表",description="规则设置表")
@Data
public class MktUseRuleSelectionResponse implements Serializable {
    private static final long serialVersionUID = -1698868954971347739L;

    @ApiModelProperty(value = "id", dataType = "Long")
    private Long id;
    @ApiModelProperty(value = "ruleConfigId", dataType = "Long")
    private Long ruleConfigId;
    @ApiModelProperty(value = " 0 有效  1：排除", dataType = "Integer")
    private Integer limitType;
    @ApiModelProperty(value = "券活动主键id", dataType = "Long")
    private Long themeRef;
    @ApiModelProperty(value = "规则类型：0：券规则；1：卡规则；2：促销规则", dataType = "Integer")
    private Integer refType;
    @ApiModelProperty(value = "规则类型：0:地区限制 1：商家限制 2: 商品限制 按类目 3：商品限制 按品牌 4 ：商品限制 按产品 5：商品限制：按商品", dataType = "Integer")
    private Integer RuleType;
    @ApiModelProperty(value = "地区主键 或者商家主键", dataType = "Long")
    private Long limitRef;
    @ApiModelProperty(value = "限制名称", dataType = "String")
    private String refDescription;
    @ApiModelProperty(value = "限制编码", dataType = "String")
    private String extendRef;
    @ApiModelProperty(value = "产品id", dataType = "Long")
    private Long productId;
    @ApiModelProperty(value = "商品条码", dataType = "String")
    private String barcode;
    @ApiModelProperty(value = "商家名称", dataType = "String")
    private String merchantName;
    @ApiModelProperty(value = "原价", dataType = "BigDecimal")
    private BigDecimal salePrice;
    @ApiModelProperty(value = "单位名称", dataType = "String")
    private String mainUnitName;
}
