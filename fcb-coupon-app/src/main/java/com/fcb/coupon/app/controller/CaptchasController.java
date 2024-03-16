package com.fcb.coupon.app.controller;

import com.fcb.coupon.app.model.bo.CaptchasBo;
import com.fcb.coupon.app.model.param.request.CaptchasRequest;
import com.fcb.coupon.app.service.CaptchasService;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.enums.BusinessTypeEnum;
import com.fcb.coupon.common.util.AESPromotionUtil;
import com.fcb.coupon.common.util.CommonResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mashiqiong
 * @date 2021/08/19 20:23:00
 */
@Api(value = "验证码相关接口")
@RestController
@RequestMapping(value = "/api/promotion/captchas")
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@Slf4j
public class CaptchasController {

    private final CaptchasService captchasService;

    /**
     * 发送验证码
     */
    @ApiOperation(value = "发送验证码")
    @PostMapping(value = "/sendCaptchasCode.do")
    public ResponseDto<Map<String, Boolean>> sendCaptchasCode(@RequestBody CaptchasRequest in,
															  @RequestHeader(name = "clientType") String clientType) {
        try {
			CaptchasBo bo = in.convert();
			if (StringUtils.isBlank(bo.getClientType())) {
        		bo.setClientType(clientType);
            }
        	
            bo.setBusinessType(BusinessTypeEnum.COUPON_GIVE.getType());
	    	
	    	if(StringUtils.isNotBlank(bo.getMobile())) {
	    		bo.setCipherMobile(AESPromotionUtil.encrypt(bo.getMobile()));
	    	}
	    	
        	captchasService.sendCaptchasWithTx(bo);
        	
			Map<String, Boolean> result = new HashMap<>();
        	result.put("result", true);
        	
            return CommonResponseUtil.newResult("0","操作成功",result);
        } catch (Exception e) {
    		log.error("发送验证码异常", e);
    		
			Map<String, Boolean> result = new HashMap<>();
        	result.put("result", false);
        	
    		return CommonResponseUtil.newResult("0","出错了，"+e.getMessage(), result);
		}
    }

    /**
     *  校验验证码是否有效
     */
    @ApiOperation(value = "校验验证码")
    @PostMapping(value = "/verifyCaptchasCode.do")
    public ResponseDto<Map<String, Boolean>> verifyCaptchasCode(@RequestBody CaptchasRequest in,
																@RequestHeader(name = "clientType") String clientType) {
    	try {
			CaptchasBo bo = in.convert();
			if (StringUtils.isBlank(bo.getClientType())) {
        		bo.setClientType(clientType);
            }
    		
	    	bo.setBusinessType(BusinessTypeEnum.COUPON_GIVE.getType());
	    	
	    	if(StringUtils.isNotBlank(bo.getMobile())) {
	    		bo.setCipherMobile(AESPromotionUtil.encrypt(bo.getMobile()));
	    	}
	    	
	    	//校验验证码是否有效
	        final Boolean isExpire = true;
    		captchasService.verifyCaptchas(bo);
    		
			Map<String, Boolean> result = new HashMap<>();
        	result.put("result", isExpire);
        	
	        return CommonResponseUtil.newResult("0","操作成功",result);
    	} catch (Exception e) {
    		log.error("CaptchasApiAction verifyCaptchasCode error", e);
    		
			Map<String, Boolean> result = new HashMap<>();
        	result.put("result", false);
        	
    		return CommonResponseUtil.newResult("0","出错了，"+e.getMessage(), result);
		}
    }
    
    /**
     *  查询验证码
     */
    @ApiOperation(value = " 查询验证码")
    @PostMapping(value = "/queryCaptchasCode.do")
    public ResponseDto<Map<String, Boolean>> queryCaptchasCode(@RequestBody CaptchasRequest in,
															   @RequestHeader(name = "clientType") String clientType) {
    	try {
			CaptchasBo bo = in.convert();
			if (StringUtils.isBlank(bo.getClientType())) {
        		bo.setClientType(clientType);
            }
    		
	    	bo.setBusinessType(BusinessTypeEnum.COUPON_GIVE.getType());
	    	
	    	if(StringUtils.isNotBlank(bo.getMobile())) {
	    		bo.setCipherMobile(AESPromotionUtil.encrypt(bo.getMobile()));
	    	}
	    	
	    	//查询是否过期
	       Boolean isExpire = captchasService.queryCaptchas(bo);
	        
			Map<String, Boolean> result = new HashMap<>();
        	result.put("result", isExpire);
        	
	        return CommonResponseUtil.newResult("0","操作成功",result);
    	} catch (Exception e) {
    		log.error("CaptchasApiAction queryCaptchasCode error", e);
    		
			Map<String, Boolean> result = new HashMap<>();
        	result.put("result", false);
        	
    		return CommonResponseUtil.newResult("0","出错了，"+e.getMessage(), result);
		}
    }
}
