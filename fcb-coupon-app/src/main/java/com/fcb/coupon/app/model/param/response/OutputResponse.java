package com.fcb.coupon.app.model.param.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * SOA通用传出dto(重构旧SOA接口用)
 *
 * @Author Weihaiqi
 * @Date 2021-06-16 20:13
 **/
@Data
public class OutputResponse<T> implements Serializable {

    private static final long serialVersionUID = 6420026114722163067L;
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
    private List<String> errorStackTrace;
    private List<String> warningInfoLs;
}
