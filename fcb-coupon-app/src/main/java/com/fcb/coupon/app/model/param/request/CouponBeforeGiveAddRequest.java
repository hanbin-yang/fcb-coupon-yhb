package com.fcb.coupon.app.model.param.request;

import com.fcb.coupon.app.model.bo.CouponBeforeGiveAddBo;
import com.fcb.coupon.common.dto.AbstractBaseConvertor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;

/**
 * Created by mashiqiong on 8/20/21.
 */
@ApiModel(description = "转赠前的优惠券信息 入参")
@Data
public class CouponBeforeGiveAddRequest extends AbstractBaseConvertor<CouponBeforeGiveAddBo>  implements Serializable {
	private static final long serialVersionUID = 2959600300068900871L;

	@ApiModelProperty(value = "赠送人手机")
	private String giveMobile;
	
	@ApiModelProperty(value = "赠送人用户Id")
	private String giveUserId;
	
	@ApiModelProperty(value = "赠送人用户名")
	private String giveUserName;
	
	@ApiModelProperty(value = "券id")
	private Long couponId;
	
	@ApiModelProperty(value = "接受人手机号")
	private String receiverMobile;
	
	@ApiModelProperty(value = "接受人unionId")
	private String receiverUnionId;
	
	@ApiModelProperty(value = "机构人类型")
	private String brokerType;
	
	@ApiModelProperty(value = "操作类型  0赠送 1转让")
	private Integer oprationType;
	
	@ApiModelProperty(value = "平台类型，B_USER B端用户、C_USER C端用户、SAAS_USER SAAS端用户")
	private String clientType;
	
	@ApiModelProperty(value = "设备号")
    private String deviceId;
	
    @ApiModelProperty(value = "验证码类型，语音验证码标识： voice，普通短信标识:sms")
    private String captchaType;
    
    @ApiModelProperty(value = "验证码")
    private String captcha;
    
    @ApiModelProperty(value = "转赠类型 1短信赠送 2面对面赠送 3微信好友分享")
    private Integer giveType;
    
    @ApiModelProperty(value = "终端类型 取值为android/ios/web/miniapp  app就是传的android、ios")
    private String terminalType;

    @ApiModelProperty(value = "user token")
    private String userToken;
    
    @ApiModelProperty(value = "赠送者头像地址")
    private String giveAvatar;

    @ApiModelProperty(value = "赠送者呢称")
    private String giveNickname;

    @Override
	public CouponBeforeGiveAddBo convert() {
		CouponBeforeGiveAddBo bo = new CouponBeforeGiveAddBo();
        BeanUtils.copyProperties(this, bo);

        return bo;
    }
}
