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
 * 劵表
 * </p>
 *
 * @author 自动生成
 * @since 2021-06-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("coupon")
@ApiModel(value = "CouponEntity对象", description = "劵表")
public class CouponEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "券ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "券活动id")
    private Long couponThemeId;

    @ApiModelProperty(value = "券优惠类型 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券")
    private Integer couponDiscountType;

    @ApiModelProperty(value = "折扣时?折 乘于100后的值 金额时?元")
    private BigDecimal couponValue;

    @ApiModelProperty(value = "券码")
    private String couponCode;

    @ApiModelProperty(value = "券活动名称")
    private String themeTitle;

    @ApiModelProperty(value = "券类型 0自动生成 3第三方券码")
    private Integer couponType;

    @ApiModelProperty(value = "生效时间")
    private Date startTime;

    @ApiModelProperty(value = "失效时间")
    private Date endTime;

    @ApiModelProperty(value = "券状态 0已发行 1.可使用  2已使用 3已作废 4已失效 5已赠送 10：已转让 11：已冻结")
    private Integer status;

    @ApiModelProperty(value = "创建来源 0数据迁移 1指定用户发放 2注册自动发放 3交易完后发放 4活动发券（用户自己领) 5他人赠送 6红包 7线下发券 8 生日券 9 全场券 10 首次登录发放 11 抽奖券 17 主动营销券 18 等级权益券 21第三方导入 22 活动规则发放 23 他人转让")
    private Integer source;

    @ApiModelProperty(value = "创建来源id：转赠-会员手机号机构账号、前台领券-楼盘id、直播领券-直播间id、媒体广告领券-广告批次号、主动营销-任务id、活动赠券-活动id、营销活动页领券-页面id")
    private String sourceId;

    @ApiModelProperty(value = "用户类型,0是会员,1是机构经纪人,2是C端用户")
    private Integer userType;

    @ApiModelProperty(value = "绑定用户id")
    private String userId;

    @ApiModelProperty(value = "设备号")
    private String deviceMac;

    private Long createUserid;

    private String createUsername;

    @ApiModelProperty(value = "创建时间")
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
