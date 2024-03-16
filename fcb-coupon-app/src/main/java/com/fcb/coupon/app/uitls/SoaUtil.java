package com.fcb.coupon.app.uitls;

import com.fcb.coupon.app.model.param.response.OutputResponse;

/**
 * Soa接口专用响应结果工具类（旧系统SOA接口重构专用）
 *
 * @Author WeiHaiQi
 * @Date 2021-08-13 18:39
 **/
public class SoaUtil {

    public SoaUtil() {
    }

    public static <T> OutputResponse<T> resultError(String errorMessage) {
        OutputResponse<T> outputDTO = new OutputResponse();
        outputDTO.setFlag(false);
        outputDTO.setCode("1");
        outputDTO.setErrorMessage(errorMessage);
        return outputDTO;
    }

    public static <T> OutputResponse<T> resultError(String errorMessage, String code) {
        OutputResponse<T> outputDTO = new OutputResponse();
        outputDTO.setFlag(false);
        outputDTO.setErrorMessage(errorMessage);
        outputDTO.setCode(code);
        return outputDTO;
    }

    public static <T> OutputResponse<T> resultError(String errorMessage, String code, T data) {
        OutputResponse<T> outputDTO = new OutputResponse();
        outputDTO.setFlag(false);
        outputDTO.setCode(code);
        outputDTO.setData(data);
        outputDTO.setErrorMessage(errorMessage);
        return outputDTO;
    }

    public static <T> OutputResponse<T> resultSucess(T data) {
        OutputResponse<T> outputDTO = new OutputResponse();
        outputDTO.setFlag(true);
        outputDTO.setData(data);
        outputDTO.setCode("0");
        outputDTO.setSuccessMsg("处理成功!");
        return outputDTO;
    }
}
