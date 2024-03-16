package com.fcb.coupon.app.model.bo;

import lombok.Data;

/**
 * Created by mashiqiong on 7/1/21.
 */
@Data
public class CouponBeforeGiveAddBo {

	/**
	 * 赠送人手机
	 */
	private String giveMobile;
	/**
	 * 赠送人用户Id
	 */
	private String giveUserId;
	/**
	 *  赠送人用户名
	 */
	private String giveUserName;
	/**
	 * 券Id
	 */
	private Long couponId;

	/**
	 * 接受人手机
	 */
	private String receiverMobile;
	/**
	 * 接受人unionId
	 */
	private String receiverUnionId;

	/**
	 * 机构人类型
	 */
	private String brokerType;

	/**
	 * 操作类型 0赠送 1转让
	 */
	private String oprationType;

	/**
	 * 平台类型，B_USER B端用户、C_USER C端用户、SAAS_USER SAAS端用户
	 */
	private String clientType;

	/**
	 * 设备号
	 */
    private String deviceId;
    
    /**
     * 验证码类型，语音验证码标识： voice，普通短信标识:sms
     */
    private String captchaType;
    
    /**
     * 加密手机号
     */
    private String cipherMobile;
    
    /**
     * 验证码
     */
    private String captcha;
    
    /**
     * 转赠类型 1短信赠送 2面对面赠送 3微信好友分享
     */
    private Integer giveType;
    
    /**
     * 取值为android/ios/web/miniapp  app就是传的android、ios
     */
    private String terminalType;
    
    private String userToken;
    
    /**
     * 赠送者头像地址
     */
    private String giveAvatar;

    /**
     * 赠送者呢称
     */
    private String giveNickname;

	public static void main(String[] args) {

		Long id = 2109150000000009L;
		String i =  String.format("%03d", (id % 32) + 1);
		System.out.println(i);
	}
}
