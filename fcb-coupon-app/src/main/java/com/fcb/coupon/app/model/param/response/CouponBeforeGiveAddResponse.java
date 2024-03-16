package com.fcb.coupon.app.model.param.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

@Data
@ApiModel(description = "添加转增前优惠券信息 出参")
public class CouponBeforeGiveAddResponse implements Serializable {
	/**
     * 主键ID
     */
	@ApiModelProperty(value = "劵赠送的主键id")
    private String id;

    /**
     * 券活动id
     */
	@ApiModelProperty(value = "券活动id")
    private Long couponThemeId;

    /**
     * 券ID
     */
	@ApiModelProperty(value = "券ID")
    private Long couponId;

    /**
     * 失效时间
     */
	@ApiModelProperty(value = "失效时间")
    private Date expireTime;

    /**
     * 转赠类型 1短信赠送 2面对面赠送 3微信好友分享
     */
	@ApiModelProperty(value = "转赠类型 1短信赠送 2面对面赠送 3微信好友分享")
    private Integer giveType;

    /**
     * 赠送者userid
     */
	@ApiModelProperty(value = "赠送者userid")
    private String giveUserid;

    /**
     * 赠送者unionId
     */
	@ApiModelProperty(value = "赠送者unionId")
    private String giveUnionid;

    /**
     * 赠送者头像地址
     */
	@ApiModelProperty(value = "赠送者头像地址")
    private String giveAvatar;

    /**
     * 赠送者呢称
     */
	@ApiModelProperty(value = "赠送者呢称")
    private String giveNickname;

    /**
     * 0 B端、1 saas端、2 C端、3旧机构
     */
	@ApiModelProperty(value = "0 B端、1 saas端、2 C端、3旧机构")
    private Integer giveCrowdScopeId;

    /**
     * 取值为android/ios/web/miniapp  app就是传的android、ios
     */
	@ApiModelProperty(value = "取值为android/ios/web/miniapp  app就是传的android、ios")
    private String terminalType;

    /**
     * 接受者手机号
     */
	@ApiModelProperty(value = "接受者手机号")
    private String receiveUserMobile;

    /**
     * 创建时间
     */
	@ApiModelProperty(value = "创建时间")
    private Date createTime;

	@ApiModelProperty(value = "参数")
	private Map<String, String> parameter;
}
