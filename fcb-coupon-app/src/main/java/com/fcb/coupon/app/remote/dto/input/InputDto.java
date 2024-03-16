package com.fcb.coupon.app.remote.dto.input;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用传入dto
 *
 * @Author Weihaiqi
 * @Date 2021-06-16 20:16
 **/
@Data
public class InputDto<T> implements Serializable {

    private static final long serialVersionUID = 5856461938299882881L;
    /** 操作人ID **/
    private Long userid;
    /** 操作人姓名 **/
    private String userName;
    /** 操作人IP **/
    private String userIp;
    /** 请求链路Ticket **/
    private String traceTicket;
    /** 操作人MAC地址 **/
    private String mac;
    /** 服务器ip **/
    private String serverIp;
    /** soa客户端ip **/
    private String clientIp;
    /** 核心调用数据 **/
    private T data;
    /** 公司id **/
    private Long companyId;
    /** 商家id **/
    private Long merchantId;
    /** 商家权限id集合 **/
    private List<Long> merchantIds = new ArrayList();
    /** 用户类型 **/
    private Integer userType;
    /** 个性化定位参数 **/
    private Map<String, String> sArgs = new HashMap();
}
