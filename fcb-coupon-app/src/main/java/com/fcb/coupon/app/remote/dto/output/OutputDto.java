package com.fcb.coupon.app.remote.dto.output;

import lombok.Data;

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
@Data
public class OutputDto<T> implements Serializable {

    private static final long serialVersionUID = 8442663315194667513L;

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

}
