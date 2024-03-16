package com.fcb.coupon.app.model.param.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fcb.coupon.app.serializer.DateToLongSerializer;
import com.fcb.coupon.common.enums.YesNoEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author mashiqiong
 * @date 2021-8-17 15:30
 */
@Data
public class CouponThemeResponse implements Serializable {
    
    @ApiModelProperty(value = "优惠券活动ID")
    private Long id;

    @ApiModelProperty(value = "活动名称")
    private String activityName;

    @ApiModelProperty(value = "优惠券名称")
    private String themeTitle;

    @ApiModelProperty(value = "活动开始时间")
    @JsonSerialize(using = DateToLongSerializer.class)
    private Date startTime;

    @ApiModelProperty(value = "活动结束时间")
    @JsonSerialize(using = DateToLongSerializer.class)
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

    @ApiModelProperty(value = "使用人群成翻译成中文形式")
    private String crowdScopedStr;

    @ApiModelProperty(value = "发行总张数")
    private Integer totalLimit;

    @ApiModelProperty(value = "已经生成的张数")
    private Integer drawedCoupons;

    @ApiModelProperty(value = "还可生成的张数")
    private Integer availableCoupons;

    @ApiModelProperty(value = "已领取的张数")
    private Integer sendedCouopns;

    @ApiModelProperty(value = "已使用的张数")
    private Integer usedCouopns;

    @ApiModelProperty(value = "可领取的张数")
    private Integer canSendAmount;

    @ApiModelProperty(value = "券面额")
    private BigDecimal couponAmount;

    @ApiModelProperty(value = "使用条件")
    private String themeDesc;

    @ApiModelProperty(value = "规则类型 1:券固定有效期规则; 2:券有效期规则; 4:券折扣; 5:券金额; 11:是否可赠送; 12:是否可转让")
    private Integer ruleType;

    @ApiModelProperty(value = "是否可赠送,true可以,false不可以")
    private Boolean canDonationBool = Boolean.FALSE;

    @ApiModelProperty(value = "折扣上限")
    private BigDecimal useUpLimit;

    @ApiModelProperty(value = "优惠方式单位")
    private String couponUnit;

    @ApiModelProperty(value = "扩展金额字段一。保留字段")
    private BigDecimal couponAmountExt1;

    @ApiModelProperty(value = "费用归属组织id")
    private Long belongingOrgId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @ApiModelProperty(value = "当前记录所属的组织ID集合")
    private List<Long> orgIds;

    @ApiModelProperty(value = "当前记录所属的组织名称集合")
    private List<String> orgNames;

    @ApiModelProperty(value = "券优惠类型 金额时 ?元")
    private BigDecimal discountAmount;

    @ApiModelProperty(value = "券优惠类型 折扣时 ?折 乘于100后的值")
    private Integer discountValue;

    @ApiModelProperty(value = "券折扣 7折为70")
    private Integer couponDiscount;

    @ApiModelProperty(value = "当前券活动个人可领取券数")
    private Integer individualLimit;

    @ApiModelProperty(value = "key:0-5 value:地区 商家 类目")
    private Map<Integer, List<Long>> ruleTypeMap;

    @ApiModelProperty(value = "使用优惠券最小金额")
    private BigDecimal useLimit;

    @ApiModelProperty(value = "单个订单限用券数")
    private Integer orderUseLimit;

    @ApiModelProperty(value = "个人每日限领张数")
    private Integer everyDayLimit;

    @ApiModelProperty(value = "个人每月限领张数")
    private Integer everyMonthLimit;

    @ApiModelProperty(value = "审核备注")
    private String remark;

    @ApiModelProperty(value = "是否可转让 1可以 0不可")
    private Integer canTransfer;

    @ApiModelProperty(value = "有效期计算方式  1：固定有效期，2：从领用开始计算")
    private Integer effDateCalcMethod;

    @ApiModelProperty(value = "固定有效期开始时间")
    private Date effDateStartTime;

    @ApiModelProperty(value = "固定有效期结束时间")
    private Date effDateEndTime;

    @ApiModelProperty(value = "自用户领取几天后失效")
    private Integer effDateDays;

    @ApiModelProperty(value = "券图片地址")
    private String couponPicUrl;

    @ApiModelProperty(value = "是否可赠送 1可以 0不可")
    private Integer canDonation;

    @ApiModelProperty(value = "活动状态中文名称")
    private String statusStr;

    @ApiModelProperty(value = "前端活动状态 0 未审核 1 待审核 2 未开始 3 审核不通过 4 进行中 5 已过期 6 已关闭")
    private Integer frontStatus;

    @ApiModelProperty(value = "前端活动状态中文名称")
    private String frontStatusStr;

    @ApiModelProperty(value = "当前用户已领取数量")
    private Integer receiveNumCurrentUser;

    @ApiModelProperty(value = "使用规则")
    private String useRuleRemark;

}
