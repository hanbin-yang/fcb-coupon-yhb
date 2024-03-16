package com.fcb.coupon.app.exception;

import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.exception.CommonErrorCode;
import com.fcb.coupon.app.uitls.AppResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

/**
 * @author YangHanBin
 * @date 2021-06-11 15:25
 */
@RestControllerAdvice
@Slf4j
public class ExceptionHandlers {
    @ExceptionHandler(BusinessException.class)
    public ResponseDto<Void> handleBusinessException(BusinessException e) {
        log.error("business error: code={}, message={}", e.getCode(), e.getMessage(), e);
        return AppResponseUtil.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler({NoHandlerFoundException.class})
    @ResponseBody
    public ResponseDto handleNoHandlerFoundException(Exception ex) {
        BusinessException businessException = new BusinessException(CommonErrorCode.NO_FOUND);
        log.warn("request error: code={}, message={}", businessException.getCode(), businessException.getMessage(), ex);
        return AppResponseUtil.fail(businessException.getCode(), businessException.getMessage());
    }

    @ExceptionHandler({HttpMediaTypeNotSupportedException.class, HttpMediaTypeNotAcceptableException.class, HttpRequestMethodNotSupportedException.class})
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ResponseBody
    public ResponseDto handleNotSupportedException(Exception ex) {
        BusinessException businessException = new BusinessException(CommonErrorCode.METHOD_NOT_ALLOWED);
        log.warn("request error: code={}, message={}", businessException.getCode(), businessException.getMessage(), ex);
        return AppResponseUtil.fail(businessException.getCode(), businessException.getMessage());
    }

    @ExceptionHandler({Exception.class})
    @ResponseBody
    public ResponseDto handleException(Exception ex) {
        BusinessException businessException = new BusinessException(CommonErrorCode.SYSTEM_ERROR);
        log.error("system error: code={}, message={}", businessException.getCode(), businessException.getMessage(), ex);
        return AppResponseUtil.fail(businessException.getCode(), businessException.getMessage());
    }

    @ExceptionHandler({MissingServletRequestParameterException.class})
    @ResponseBody
    public ResponseDto handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        log.warn("请求参数错误：" + ex.getMessage());
        String errorMesssage = "参数" + ex.getParameterName() + "不能为空";
        return AppResponseUtil.fail(CommonErrorCode.PARAMS_ERROR.getCode(), errorMesssage);
    }
    @ExceptionHandler({MissingServletRequestPartException.class})
    @ResponseBody
    public ResponseDto handleMissingServletRequestPartException(MissingServletRequestPartException ex) {
        log.warn("请求参数错误：" + ex.getMessage());
        String errorMesssage = "参数" + ex.getRequestPartName() + "不能为空";
        return AppResponseUtil.fail(CommonErrorCode.PARAMS_ERROR.getCode(), errorMesssage);
    }


    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseBody
    public ResponseDto handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.warn("请求参数校验异常：" + ex.getMessage());
        String errorMesssage = "";
        List<ObjectError> errors = ex.getBindingResult().getAllErrors();
        if (!CollectionUtils.isEmpty(errors)) {
            for (ObjectError objectError : errors) {
                errorMesssage = errorMesssage + objectError.getDefaultMessage() + "\n";
            }
        }
        return AppResponseUtil.fail(CommonErrorCode.PARAMS_ERROR.getCode(), errorMesssage);
    }

    @ExceptionHandler({BindException.class})
    @ResponseBody
    public ResponseDto handleBindException(BindException ex) {
        log.warn("请求参数绑定异常：" + ex.getMessage());
        String errorMesssage = "";
        List<ObjectError> errors = ex.getBindingResult().getAllErrors();
        if (!CollectionUtils.isEmpty(errors)) {
            for (ObjectError objectError : errors) {
                errorMesssage = errorMesssage + objectError.getDefaultMessage() + "\n";
            }
        }
        return AppResponseUtil.fail(CommonErrorCode.PARAMS_ERROR.getCode(), errorMesssage);
    }
}
