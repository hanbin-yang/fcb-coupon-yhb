package com.fcb.coupon.common.log;

import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.exception.CommonErrorCode;
import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

/**
 * @author 唐陆军
 * @Description feign入参出参处理
 * @createTime 2021年06月17日 10:10:00
 */
@Slf4j
@Aspect
public class FeignParamInterceptor extends AbstractParamInterceptor {

    @Around("@within(org.springframework.cloud.openfeign.FeignClient)")
    public Object execute(ProceedingJoinPoint pjp) throws Throwable {
        Signature signature = pjp.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Class clientInterface = methodSignature.getDeclaringType();

        ParamEntity paramEntity = new ParamEntity();
        paramEntity.setStartTime(System.currentTimeMillis());
        paramEntity.setMethodStr(signature.toString());
        printRequestParam(pjp, paramEntity);

        Object obj = null;
        try {
            obj = pjp.proceed();
            Method targetMethod = methodSignature.getMethod();
            String apiName = clientInterface.getSimpleName() + "." + targetMethod.getName();
            if (obj == null) {
                handleNull(obj, apiName);
            } else if (obj instanceof feign.Response) {
                handleResponse(obj, apiName);
            }else{
                paramEntity.setEndTime(System.currentTimeMillis());
                printResponseParam(paramEntity, obj);
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("feign call api error", ex);
            throw new BusinessException(CommonErrorCode.API_CALL_ERROR);
        }
        return obj;
    }


    public void handleNull(Object returnObject, String apiName) throws BusinessException {
        log.warn("feign call api returned null，api={}", apiName);
        throw new BusinessException(CommonErrorCode.API_CALL_ERROR);
    }

    public void handleResponse(Object returnObject, String apiName) throws BusinessException {
        Response response = (Response) returnObject;
        if (HttpStatus.OK.value() != response.status()) {
            String str = null;
            StringBuilder messageBuffer = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().asInputStream(), "UTF-8"))) {
                while ((str = reader.readLine()) != null) {
                    messageBuffer.append(str);
                }
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                throw new BusinessException(CommonErrorCode.SYSTEM_ERROR);
            }
            log.error("feign call api error, api={},status={},message={}", apiName, Integer.valueOf(response.status()), messageBuffer.toString());
            throw new BusinessException(CommonErrorCode.API_CALL_ERROR);
        }
    }
}