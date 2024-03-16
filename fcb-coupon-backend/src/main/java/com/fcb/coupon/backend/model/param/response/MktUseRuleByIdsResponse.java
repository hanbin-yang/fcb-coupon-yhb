package com.fcb.coupon.backend.model.param.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 营销中心->优惠券管理->优惠券活动列表 出参
 * @author mashiqiong
 * @date 2021-08-02 20:59
 */
@ApiModel(value="优惠券列表",description="优惠券列表")
@Data
public class MktUseRuleByIdsResponse implements Serializable {
    private static final long serialVersionUID = -1698868954971347739L;

    @ApiModelProperty(value = "类型", dataType = "String")
    private String type;

    @ApiModelProperty(value = "主键ID", dataType = "Long")
    private Long id;

    @ApiModelProperty(value = "商家名称", dataType = "String")
    private String name;

    @ApiModelProperty(value = "区域名称", dataType = "String")
    private String regionName;

    @ApiModelProperty(value = "主键ID", dataType = "Long")
    private Long keyId;

    @ApiModelProperty(value = "省id", dataType = "Long")
    private Long provinceCode;

    @ApiModelProperty(value = "市id", dataType = "Long")
    private Long cityCode;

    @ApiModelProperty(value = "商家ID", dataType = "Long")
    private Long shopId;

    @ApiModelProperty(value = "商家编号", dataType = "String")
    private String code;

    @ApiModelProperty(value = "适用范围ID", dataType = "Integer")
    private Integer merchantType;

    @ApiModelProperty(value = "楼盘编码，类型为店铺时", dataType = "String")
    private String buildCode;

    @ApiModelProperty(value = "所属集团名称（针对商家，店铺）", dataType = "String")
    private String ownedGroupName;

    @ApiModelProperty(value = "所属商家名称（针对店铺）", dataType = "String")
    private String ownedMerchantName;

    @ApiModelProperty(value = "B端上线状态，0未上线 1已上线", dataType = "String")
    private Integer buildOnlineStatus;

    @ApiModelProperty(value = "C端上线状态，0未上线 1已上线", dataType = "String")
    private Integer cpointBuildOnlineStatus;

    @ApiModelProperty(value = "机构端上线状态，0未上线 1已上线", dataType = "String")
    private Integer orgPointBuildOnlineStatus;
}
