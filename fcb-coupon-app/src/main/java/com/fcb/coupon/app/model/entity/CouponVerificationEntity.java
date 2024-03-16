package com.fcb.coupon.app.model.entity;

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
 * 劵使用表
 * </p>
 *
 * @author 自动生成
 * @since 2021-06-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("coupon_verification")
@ApiModel(value="CouponVerificationEntity对象", description="劵使用表")
public class CouponVerificationEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "券ID")
    private Long couponId;

    @ApiModelProperty(value = "券活动id")
    private Long couponThemeId;

    @ApiModelProperty(value = "券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券")
    private Integer couponDiscountType;

    @ApiModelProperty(value = "折扣时?折 乘于100后的值 金额时?元")
    private BigDecimal couponValue;

    @ApiModelProperty(value = "使用券的订单号")
    private String orderCode;

    @ApiModelProperty(value = "使用券的明源认购书编号")
    private String subscribeCode;

    @ApiModelProperty(value = "券核销时间")
    private Date usedTime;

    @ApiModelProperty(value = "核销渠道 0后台手动核销 1明源核销")
    private Integer usedChannel;

    @ApiModelProperty(value = "核销店铺id --迁移数据，后续程序也可使用")
    private Long usedStoreId;

    @ApiModelProperty(value = "核销店铺编码 --迁移数据，后续程序也可使用")
    private String usedStoreCode;

    @ApiModelProperty(value = "核销店铺名称 --迁移数据，后续程序也可使用")
    private String usedStoreName;

    @ApiModelProperty(value = "核销房源guid --迁移数据，后续程序也可使用")
    private String usedRoomGuid;

    @ApiModelProperty(value = "核销商品名称")
    private String productName;

    @ApiModelProperty(value = "核销商品编码")
    private String productCode;

    @ApiModelProperty(value = "核销商品金额")
    private BigDecimal productAmount;

    @ApiModelProperty(value = "券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结")
    private Integer status;
    /**
     * 优惠券名称
     */
    private String themeTitle;
    /**
     * 券码
     */
    private String couponCode;
    /**
     * 绑定用户id
     */
    private String bindUserId;
    /**
     * 绑定手机号
     */
    private String bindTel;
    /**
     * 用户类型
     */
    private Integer userType;
    /**
     * 优惠券生券时间
     */
    private Date couponCreateTime;
    /**
     * 生效时间
     */
    private Date startTime;
    /**
     * 失效时间
     */
    private Date endTime;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    private Long createUserid;

    private String createUsername;
    /**
     * 核销人userId
     */
    private Long verifyUserid;
    /**
     * 核销人名称
     */
    private String verifyUsername;

    @Version
    @TableField(fill = FieldFill.INSERT)
    private Integer versionNo;

    @ApiModelProperty(value = "逻辑删除字段 0 正常 1 已删除")
    @TableLogic
    private Integer isDeleted;


}
