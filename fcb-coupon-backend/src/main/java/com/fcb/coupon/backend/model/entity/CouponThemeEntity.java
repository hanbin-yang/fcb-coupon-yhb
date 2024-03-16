package com.fcb.coupon.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 劵活动表
 * </p>
 *
 * @author 自动生成
 * @since 2021-06-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("coupon_theme")
@ApiModel(value="CouponThemeEntity对象", description="劵活动表")
public class CouponThemeEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "活动名称")
    private String activityName;

    @ApiModelProperty(value = "优惠券名称")
    private String themeTitle;

    @ApiModelProperty(value = "活动开始时间")
    private Date startTime;

    @ApiModelProperty(value = "活动结束时间")
    private Date endTime;

    @ApiModelProperty(value = "活动描述")
    private String themeDesc;

    @ApiModelProperty(value = "活动状态 0 未审核 1 待审核 2 未开始 3 审核不通过 4 进行中 5 已过期 6 已关闭")
    private Integer status;

    @ApiModelProperty(value = "活动类型 0：平台券  11：商家券 5：集团券 21：店铺券")
    private Integer themeType;

    @ApiModelProperty(value = "券类型 0电子券 1实体券/预制券 2红包券 3：第三方券码")
    private Integer couponType;

    @ApiModelProperty(value = "有效期计算方式  1：固定有效期，2：从领用开始计算")
    private Integer effDateCalcMethod;

    @ApiModelProperty(value = "固定有效期开始时间")
    private Date effDateStartTime;

    @ApiModelProperty(value = "固定有效期结束时间")
    private Date effDateEndTime;

    @ApiModelProperty(value = "自用户领取几天后失效")
    private Integer effDateDays;

    @ApiModelProperty(value = "发券类型(1:活动规则券,19:线下预制券,4:前台领券,17:主动营销券,18:权益优惠券,19:线下预制券,20:媒体广告券,21:直播券,22:营销活动页券)")
    private Integer couponGiveRule;

    @ApiModelProperty(value = "使用限制  0：无限制， 其他：最小金额限制")
    private BigDecimal useLimit;

    @ApiModelProperty(value = "使用人群ids,0是会员,1是机构经纪人,2是C端用户")
    private String applicableUserTypes;

    @ApiModelProperty(value = "单个订单该类型券使用张数限制")
    private Integer orderUseLimit;

    @ApiModelProperty(value = "券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券")
    private Integer couponDiscountType;

    @ApiModelProperty(value = "券优惠类型金额时?元 折扣时折扣上限价格?元")
    private BigDecimal discountAmount;

    @ApiModelProperty(value = "券优惠类型 折扣时 ?折 乘于100后的值")
    private Integer discountValue;

    @ApiModelProperty(value = "券图片地址")
    private String couponPicUrl;

    @ApiModelProperty(value = "当前券活动个人可领取券数")
    private Integer individualLimit;

    @ApiModelProperty(value = "个人每日限领张数")
    private Integer everyDayLimit;

    @ApiModelProperty(value = "个人每月限领张数")
    private Integer everyMonthLimit;

    @ApiModelProperty(value = "审核备注")
    private String remark;

    @ApiModelProperty(value = "费用归属组织id")
    private Long belongingOrgId;

    @ApiModelProperty(value = "是否可赠送 1可以 0不可")
    private Integer canDonation;

    @ApiModelProperty(value = "是否可转让 1可以 0不可")
    private Integer canTransfer;

    private Long createUserid;

    private String createUsername;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    private Long updateUserid;

    private String updateUsername;

    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;

    @Version
    @TableField(fill = FieldFill.INSERT)
    private Integer versionNo;

    @ApiModelProperty(value = "逻辑删除字段 0 正常 1 已删除")
    @TableLogic
    private Integer isDeleted;
}
