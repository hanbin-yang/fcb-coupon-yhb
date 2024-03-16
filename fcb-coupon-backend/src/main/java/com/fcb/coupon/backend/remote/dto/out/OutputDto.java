package com.fcb.coupon.backend.remote.dto.out;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 通用传出dto
 *
 * @Author Weihaiqi
 * @Date 2021-06-16 20:13
 **/
public class OutputDto<T> implements Serializable {
    private static final long serialVersionUID = -6487750026490963884L;
    private boolean exceptionTransfer = false;

    /** 接口调用成功还是失败 **/
    private boolean flag;
    /** 返回失败消息 **/
    private String errorMessage;
    /** 返回成功消息 **/
    private String successMsg;
    /** 返回数据 **/
    private T data;
    /** 错误编码 **/
    private String code;
    private List<String> errorStackTrace = new ArrayList();
    private List<String> warningInfoLs = new ArrayList();

    public OutputDto() {
    }

    @Deprecated
    public boolean isFlag() {
        return this.flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getSuccessMsg() {
        return this.successMsg;
    }

    public void setSuccessMsg(String successMsg) {
        this.successMsg = successMsg;
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setExceptionTransfer(boolean exceptionTransfer) {
        this.exceptionTransfer = exceptionTransfer;
    }

    public void addErrorStack(Throwable trw) {
        if (this.exceptionTransfer) {
            if (trw != null) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(bos);
                trw.printStackTrace(ps);
                ps.close();
                String errMsg = bos.toString();
                int len = Math.min(1500, errMsg.length());
                this.errorStackTrace.add(errMsg.substring(0, len));
            }
        }
    }

    public void addErrorInfo(String errorInfo) {
        this.errorStackTrace.add(errorInfo);
    }

    public void addErrorInfoLst(List<String> errorLS) {
        this.errorStackTrace.addAll(errorLS);
    }

    public void addErrorInfo(String code, Object... args) {
    }

    public String getFullErrStackTraceStr() {
        StringBuilder fsts = new StringBuilder("");
        Iterator var2 = this.errorStackTrace.iterator();

        while(var2.hasNext()) {
            String err = (String)var2.next();
            fsts.append(err);
            fsts.append("\n");
        }

        return fsts.toString();
    }

    public List<String> getErrStatckTrace() {
        return this.errorStackTrace;
    }

    public boolean isServiceSucceed() {
        return this.flag;
    }

    public void addWarningCode(String warningCode, Object... args) {
    }

    @Deprecated
    public void addWarningInfo(String warn) {
        this.warningInfoLs.add(warn);
    }
}
