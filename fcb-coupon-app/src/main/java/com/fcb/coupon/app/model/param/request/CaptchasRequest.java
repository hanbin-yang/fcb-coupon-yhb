package com.fcb.coupon.app.model.param.request;

import cn.hutool.core.bean.BeanUtil;
import com.fcb.coupon.app.model.bo.CaptchasBo;
import com.fcb.coupon.common.dto.AbstractBaseConvertor;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by mashiqiong on 2021/08/19.
 */
@Data
public class CaptchasRequest  extends AbstractBaseConvertor<CaptchasBo> implements Serializable {

	@ApiModelProperty(value="设备号", required=true, dataType = "String")
    private String deviceId;
    
	@ApiModelProperty(value="验证码类型，语音验证码标识： voice，普通短信标识:sms", required=true, dataType = "String")
    private String captchaType;
    
	@ApiModelProperty(value="短信业务类型", required=false, dataType = "Integer")
    private Integer businessType;
    
	@ApiModelProperty(value="加密手机号", required=false, dataType = "String")
    private String cipherMobile;
    
	@ApiModelProperty(value="未加密的手机号", required=true, dataType = "String")
    private String mobile;
    
	@ApiModelProperty(value="验证码", required=true, dataType = "String")
    private String captcha;
    
	@ApiModelProperty(value="用户IP地址", required=true, dataType = "String")
    private String userIp;
    
	@ApiModelProperty(value="图片验证码", required=false, dataType = "String")
    private String checkImageCode;
    
	@ApiModelProperty(value="用户名", required=true, dataType = "String")
    private String username;
    
	@ApiModelProperty(value="证件名称", required=true, dataType = "String")
    private String identityCardName;
    
	@ApiModelProperty(value="B端用户:B_USER，C端用户:C_USER，sass、机构端用户:J_USER", required=true, dataType = "String")
    private String clientType;
    
    @ApiModelProperty(value = "user token", dataType = "String")
    private String userToken;
    
    @ApiModelProperty(value = "userId", dataType = "String")
    private String userId;

    @Override
    public CaptchasBo convert() {
        CaptchasBo bo = new CaptchasBo();
        BeanUtil.copyProperties(this, bo);

        return bo;
    }
}
