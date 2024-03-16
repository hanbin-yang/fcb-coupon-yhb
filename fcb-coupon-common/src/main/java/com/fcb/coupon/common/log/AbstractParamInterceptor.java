package com.fcb.coupon.common.log;

import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.exception.BusinessException;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月17日 10:58:00
 */
public abstract class AbstractParamInterceptor {


    protected ResponseDto buildExceptionRestResult(BusinessException businessException) {
        return new ResponseDto(businessException.getCode(), businessException.getMessage());
    }

    protected void printRequestParam(ProceedingJoinPoint pjp, ParamEntity paramEntity) {
        RequestParamPrinter requestParamPrinter = new RequestParamPrinter(paramEntity, pjp);
        requestParamPrinter.call();
    }

    protected void printResponseParam(ParamEntity paramEntity, Object returnValue) {
        ResponseParamPrinter outParameterPrinter = new ResponseParamPrinter(paramEntity, returnValue);
        outParameterPrinter.call();
    }
}
