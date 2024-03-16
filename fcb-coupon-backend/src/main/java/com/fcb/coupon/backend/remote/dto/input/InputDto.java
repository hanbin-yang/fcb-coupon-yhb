package com.fcb.coupon.backend.remote.dto.input;

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
public class InputDto<T> implements Serializable {

    private static final long serialVersionUID = -5531070343125295136L;
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

    public InputDto() {
//        this.setTraceTicket(OdySession.getTraceTicket());
//        this.setClientIp(SystemUtil.getLocalhostIp());
    }

    public void setsArgs(Map<String, String> sArgs) {
        this.sArgs = sArgs;
    }

    public void setBusinessCode(String businessCode) {
        this.sArgs.put("business_code", businessCode);
    }

    public String getBusinessCode() {
        return (String)this.sArgs.get("business_code");
    }

    public void setSubBusinessCode(String subBusinessCode) {
        this.sArgs.put("sub_business_code", subBusinessCode);
    }

    public String getSubBusinessCode() {
        return (String)this.sArgs.get("sub_business_code");
    }

    public void setBusinessType(String businessType) {
        this.sArgs.put("business_type", businessType);
    }

    public String getBusinessType() {
        return (String)this.sArgs.get("business_type");
    }

    public void setProcessChainCode(String processChainCode) {
        this.sArgs.put("p2p_chain_code", processChainCode);
    }

    public String getProcessChainCode() {
        return (String)this.sArgs.get("p2p_chain_code");
    }

    public void setAdmitNode(String admitNode) {
        this.sArgs.put("admit_node", admitNode);
    }

    public Map<String, String> getsArgs() {
        return this.sArgs;
    }

    public Long getUserid() {
        return this.userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserIp() {
        return this.userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }

    public String getMac() {
        return this.mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getServerIp() {
        return this.serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public String getClientIp() {
        return this.clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Long getCompanyId() {
        return this.companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
        this.sArgs.put("company_id", String.valueOf(companyId));
    }

    public Long getMerchantId() {
        return this.merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public List<Long> getMerchantIds() {
        return this.merchantIds;
    }

    public void setMerchantIds(List<Long> merchantIds) {
        this.merchantIds = merchantIds;
    }

    public Integer getUserType() {
        return this.userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public String getTraceTicket() {
        return this.traceTicket;
    }

    public void setTraceTicket(String traceTicket) {
        this.traceTicket = traceTicket;
    }

    @Override
    public String toString() {
        return "InputDTO{userid=" + this.userid + ", userName='" + this.userName + '\'' + ", userIp='" + this.userIp + '\'' + ", mac='" + this.mac + '\'' + ", serverIp='" + this.serverIp + '\'' + ", data=" + this.data + ", companyId=" + this.companyId + ", merchantId=" + this.merchantId + ", merchantIds=" + this.merchantIds + ", userType=" + this.userType + ", traceTicket=" + this.traceTicket + '}';
    }
}
