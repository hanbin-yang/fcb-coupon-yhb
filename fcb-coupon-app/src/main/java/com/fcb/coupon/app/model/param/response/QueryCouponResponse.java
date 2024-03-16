package com.fcb.coupon.app.model.param.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fcb.coupon.app.remote.dto.output.StoreOrgInfoOutDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * TODO
 *
 * @Author WeiHaiQi
 * @Date 2021-08-16 19:29
 **/
@Data
public class QueryCouponResponse implements Serializable {

    private static final long serialVersionUID = 2123611139637461718L;

    @ApiModelProperty(value="用户Id")
    private Long userId;
    @ApiModelProperty(value="优惠券Id")
    private Long couponId;
    @ApiModelProperty(value="优惠券code")
    private String couponCode;
    @ApiModelProperty(value="优惠券主题Id")
    private Long themeId;
    @ApiModelProperty(value="优惠券名称")
    private String themeTitle;
    @ApiModelProperty(value="优惠券生效时间")
    private Date startTime;
    @ApiModelProperty(value="优惠券最迟使用时间")
    private Date endTime;
    @ApiModelProperty(value="优惠券绑定时间")
    private Date bindTime;
    @ApiModelProperty(value="优惠券使用状态")
    private Integer status;
    @ApiModelProperty(value="券优惠类型 0 按金额 1 按折扣")
    private Integer couponDiscountType;
    @ApiModelProperty(value="券金额、折扣")
    private BigDecimal couponValue;
    @ApiModelProperty(value="券使用限制，0：无限制 其他：限制金额")
    private BigDecimal useLimit;
    @ApiModelProperty(value="券使用最高限额（折扣）")
    private BigDecimal useUpLimit;
    @ApiModelProperty(value="当前活动每个人可领取券数")
    private Integer individualLimit;

//    @ApiModelProperty(value="适用商家")
//    /** 适用商家*/
//    private List<MerchantOutput> merchantList;

    @ApiModelProperty(value="活动描述")
    private String themeDesc;
    @ApiModelProperty(value= "绑定人手机号")
    private String bindTel;

    @ApiModelProperty(value= "转赠类型 1赠送 2转让")
    private Integer giveType;

    @ApiModelProperty(value= "转赠人名称")
    private String giveUserName;
    @ApiModelProperty(value= "转赠人手机号")
    private String giveUserMobile;

    @ApiModelProperty(value= "转赠时间")
    private Date giveTime;

    @ApiModelProperty(value= "接受人手机号")
    private String receiveUserMobile;

    @ApiModelProperty(value= "券使用时间")
    private Date usedTime;

    @JsonFormat( pattern = "yyyy-MM-dd ", timezone = "GMT+8" )
    @ApiModelProperty(value= "券使用时间")
    private Date usedTimeStr;

    @ApiModelProperty(value= "是否可赠送")
    private Boolean canDonation;

    @ApiModelProperty(value= "是否可转让")
    private Boolean canAssign;

    @ApiModelProperty(value= "适用楼盘")
    private List<StoreOrgInfoOutDto> limitMerchants;

    @ApiModelProperty(value= "券来源")
    private Integer source;
    @ApiModelProperty(value= "券来源描述")
    private String sourceStr;

    private String couponPicUrl;


    @ApiModelProperty(value= "使用规则")
    private String useRuleRemark;

    @ApiModelProperty(value= "绑定账号类型")
    private String bindUserType;

    @ApiModelProperty(value= "赠送账号类型")
    private String receiveUserType;

    @ApiModelProperty(value= "使用人群")
    private List<Integer> crowdScopeIds;

    @ApiModelProperty(value = "优惠券主题类型")
    private Integer themeType;
    @ApiModelProperty(value = "优惠券创建日期")
    private Date createTime;
    @ApiModelProperty(value = "优惠券创建日期")
    private Date updateTime;
    @ApiModelProperty(value = "券活动状态")
    private Integer couponStatus;

    @ApiModelProperty(value = "券活动开始时间")
    private String couponStartTimeStr;

    @ApiModelProperty(value = "券活动结束时间")
    private String couponEndTimeStr;
    @ApiModelProperty(value = "优惠券开始时间")
    private String startTimeStr;
    @ApiModelProperty("优惠券结束时间")
    private String endTimeStr;
    @ApiModelProperty(value = "优惠券类型")
    private Integer couponType;
    @ApiModelProperty(value = "绑定时间")
    private String bindTimeStr;
    @ApiModelProperty(value = "转赠时间")
    private Date donateTime;


    /**
     * 生效时间
     */
    @ApiModelProperty(value = "生效时间")
    private String effectiveTimeStr;
    /**
     * expireTimeStr
     */
    @ApiModelProperty(value = "到期时间")
    private String expireTimeStr;

    /**
     * 背景图片
     */
    @ApiModelProperty(value = "背景图片")
    private String backGroundImageUrl;

    /**
     * 发送转赠通知短信次数
     */
    @ApiModelProperty(value = "发送转赠通知短信次数")
    private Integer sendMsgTimes;

    /**
     * 是否可以发送转赠通知短信
     */
    @ApiModelProperty(value = "是否可以发送转赠通知短信")
    private Boolean canSendMsg;

    /**
     * 券来源对象名称
     */
    @ApiModelProperty(value = "券来源对象名称")
    private String getObjName;

    /**
     * 转赠时间
     */
    @JsonFormat(
            pattern = "yyyy-MM-dd HH:mm",
            timezone = "GMT+8"
    )
    @ApiModelProperty(value = "转赠时间")
    private Date donateTimeStr;

    /**
     * 转赠人
     */
    @ApiModelProperty(value = "转赠人")
    private String donateObjName;

    /**
     * 第三方优惠券码
     */
    @ApiModelProperty(value = "第三方优惠券码")
    private String thirdCouponCode;
    /**
     * 第三方优惠券密码(解码)
     */
    @ApiModelProperty(value = "第三方优惠券密码")
    private String thirdCouponPassword;
}
