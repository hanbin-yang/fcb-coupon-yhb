package com.fcb.coupon.backend.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * YHB
 * @param <T>
 */
@ApiModel(value = "ouser通用传出dto")
@Data
public class OuserOutputDto<T> implements Serializable {

    private static final long serialVersionUID = 3327805803045663195L;

    private boolean exceptionTransfer = false;

    private static final int MSG_SHOW_MAX_LENGTH = 1500;

    private static final int START_INDEX = 0;


    @ApiModelProperty(value = "接口调用成功还是失败")
    private boolean flag;

    @ApiModelProperty(value = "返回失败消息")
    private String errorMessage;

    @ApiModelProperty(value = "返回成功消息")
    private String successMsg;

    @ApiModelProperty(value = "返回数据")
    private T data;

    @ApiModelProperty(value = "错误编码")
    private String code;

    /**
     * 设置异常信息
     */
    private List<String> errorStackTrace = new ArrayList<>();

    /**
     * 获取异常的全列表信息
     */
    public void addErrorStack(Throwable trw) {
        //如果不传递堆栈错误信息，直接返回
        if (!exceptionTransfer) {
            return;
        }
        //如果异常对象为空，返回
        if (trw == null) {
            return;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bos);
        trw.printStackTrace(ps);
        ps.close();
        String errMsg = bos.toString();
        //对于STACK信息，最多显示控制长度字节
        int len = Math.min(MSG_SHOW_MAX_LENGTH, errMsg.length());
        errorStackTrace.add(errMsg.substring(START_INDEX, len));
    }

    /**
     * @param errorInfo
     */
    public void addErrorInfo(String errorInfo) {
        errorStackTrace.add(errorInfo);
    }

    /**
     * 添加错误列表
     *
     * @param errorLS
     */
    public void addErrorInfoLst(List<String> errorLS) {
        errorStackTrace.addAll(errorLS);
    }


    /**
     * 获取异常错误全列表信息
     * 如果在调用链中有多个异常，则会包包含的多条进行合并
     *
     * @return
     */
    public String getFullErrStackTraceStr() {
        StringBuilder fsts = new StringBuilder("");
        for (String err : errorStackTrace) {
            fsts.append(err);
            fsts.append("\n");
        }
        return fsts.toString();
    }

    /**
     * 处理验证信息
     */
    private List<String> warningInfoLs = new ArrayList<>();

}