package com.fcb.coupon.common.log;

import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.exception.CommonErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * @author 唐陆军
 * @Description 入参出参拦截
 * @createTime 2021年06月16日 10:46:00
 */
@Slf4j
@Aspect
public class ApiParamInterceptor extends AbstractParamInterceptor {

    @Around("(@within(org.springframework.web.bind.annotation.RestController) ||@within(org.springframework.stereotype.Controller))&& (@annotation(org.springframework.web.bind.annotation.RequestMapping) || @annotation(org.springframework.web.bind.annotation.GetMapping)|| @annotation(org.springframework.web.bind.annotation.PostMapping)|| @annotation(org.springframework.web.bind.annotation.DeleteMapping)|| @annotation(org.springframework.web.bind.annotation.PutMapping)|| @annotation(org.springframework.web.bind.annotation.PatchMapping))")
    public Object execute(ProceedingJoinPoint pjp) throws Throwable {
        ParamEntity paramEntity = new ParamEntity();
        paramEntity.setStartTime(System.currentTimeMillis());
        paramEntity.setMethodStr(pjp.getSignature().toString());
        printRequestParam(pjp, paramEntity);
        Object obj = null;
        try {
            obj = pjp.proceed();
        } catch (BusinessException ex) {
            obj = buildExceptionRestResult(ex);
            throw ex;
        } catch (Exception ex) {
            BusinessException businessException = new BusinessException(CommonErrorCode.SYSTEM_ERROR);
            obj = buildExceptionRestResult(businessException);
            throw ex;
        } finally {
            paramEntity.setEndTime(System.currentTimeMillis());
            printResponseParam(paramEntity, obj);
        }
        return obj;
    }


}