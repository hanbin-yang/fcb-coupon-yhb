package com.fcb.coupon.app.model.bo;

import lombok.Data;

/**
 * @author mashiqiong
 * @date 2021-8-19 10:01
 */
@Data
public class CaptchasBo {

    /**
     * 设备号
     */
    private String deviceId;

    /**
     * 验证码类型，语音验证码标识： voice，普通短信标识:sms
     */
    private String captchaType;

    /**
     * 短信业务类型
     */
    private Integer businessType;

    /**
     * 加密手机号
     */
    private String cipherMobile;

    /**
     * 未加密的手机号
     */
    private String mobile;

    /**
     * 验证码
     */
    private String captcha;

    /**
     * 用户IP地址
     */
    private String userIp;

    /**
     * 图片验证码
     */
    private String checkImageCode;

    /**
     * 用户名
     */
    private String username;

    /**
     * 证件名称
     */
    private String identityCardName;

    /**
     * B端用户:B_USER，C端用户:C_USER，sass、机构端用户:J_USER
     */
    private String clientType;

    /**
     * user token
     */
    private String userToken;

    /**
     * 用户id
     */
    private String userId;
}
