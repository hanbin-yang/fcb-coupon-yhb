package com.fcb.coupon.backend.model.param.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fcb.coupon.common.excel.annotation.XCell;
import com.fcb.coupon.common.excel.annotation.XSheet;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 管理后台->营销中心->优惠券管理->优惠券活动列表->导出Excel 出参
 * @author mashiqiong
 * @date 2021-06-16 20:59
 */
@XSheet(name = "券活动列表1")
@ApiModel(value="优惠券列表",description="优惠券列表")
@Data
public class CouponThemeExportResponse implements Serializable {
    private static final long serialVersionUID = -1698868954971347739L;

    private Long id;
    @XCell(name = "优惠券活动ID")
    private String couponId;
    @XCell(name = "优惠券名称")
    private String themeTitle;
    @XCell(name = "发券类型")
    private String couponGiveRuleName;
    @XCell(name = "优惠方式")
    private String couponDiscountTypeName;
    @XCell(name = "券码生成方式")
    private String couponTypeName;
    @XCell(name = "面值（元）")
    private String couponAdmountName;
    @XCell(name = "券活动日期")
    private String startEndTime;
    @XCell(name = "总限额（张）")
    private Integer totalLimit;
    @XCell(name = "每人限领（张）")
    private Integer individualLimit;
    @XCell(name = "使用条件")
    private String useCondition;
    @XCell(name = "使用人群")
    private String crowdScopedStr;
    @XCell(name = "所属商家")
    private String orgNameStr;
    @XCell(name = "创建时间")
    private Date createTime;
    @XCell(name = "发行量")
    private Integer drawedCoupons;
    @XCell(name = "已领取")
    private Integer sendedCouopns;
    @XCell(name = "已使用")
    private Integer usedCouopns;
    @XCell(name = "状态")
    private String statusName;

    @ApiModelProperty(value = "活动名称")
    private String activityName;



    @ApiModelProperty(value = "活动开始时间")
    private Date startTime;

    @ApiModelProperty(value = "活动结束时间")
    private Date endTime;

    @ApiModelProperty(value = "活动状态 0 未审核 1 待审核 2 未开始 3 审核不通过 4 进行中 5 已过期 6 已关闭")
    private Integer status;

    @ApiModelProperty(value = "活动类型 0：平台券  11：商家券 5：集团券 21：店铺券")
    private Integer themeType;

    @ApiModelProperty(value = "券类型 0电子券 1实体券/预制券 2红包券 3：第三方券码")
    private Integer couponType;

    @ApiModelProperty(value = "发券类型(1:活动规则券,19:线下预制券,4:前台领券,17:主动营销券,18:权益优惠券,19:线下预制券,20:媒体广告券,21:直播券,22:营销活动页券)")
    private Integer couponGiveRule;

    @ApiModelProperty(value = "使用人群ids,0是会员,1是机构经纪人,2是C端用户")
    private List<Integer> crowdScopeIds;

    @ApiModelProperty(value = "券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券")
    private Integer couponDiscountType;




    @ApiModelProperty(value = "还可生成的张数")
    private Integer availableCoupons;


    @ApiModelProperty(value = "可领取的张数")
    private Integer canSendAmount;

    @ApiModelProperty(value = "券面额")
    private BigDecimal couponAmount;


    @ApiModelProperty(value = "规则类型 1:券固定有效期规则; 2:券有效期规则; 4:券折扣; 5:券金额; 11:是否可赠送; 12:是否可转让")
    private Integer ruleType;

    @ApiModelProperty(value = "是否可赠送,true可以,false不可以")
    private Boolean canDonation = Boolean.FALSE;

    @ApiModelProperty(value = "折扣上限")
    private BigDecimal useUpLimit;

    @ApiModelProperty(value = "优惠方式单位")
    private String couponUnit;

    @ApiModelProperty(value = "扩展金额字段一。保留字段")
    private BigDecimal couponAmountExt1;

    @ApiModelProperty(value = "当前记录所属的组织ID集合")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private List<Long> orgIds;

    @ApiModelProperty(value = "当前记录所属的组织名称集合")
    private List<String> orgNames;


}
