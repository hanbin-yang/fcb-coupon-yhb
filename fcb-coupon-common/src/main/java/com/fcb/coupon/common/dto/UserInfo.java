package com.fcb.coupon.common.dto;

import java.io.Serializable;

/**
 * @author YangHanBin
 * @date 2021-06-11 0:41
 */
public class UserInfo implements Serializable {
    private Long id;
    private Long userId;
    private String username;
    private String mobile;
    private String nickname;
    private String headPicUrl;
    private String identityCardName;
    private String ut;
    private Integer loginPlatformId;
    private String unionId;
    private String token;
    private Long merchantId;
    private String merchantCode;
    private String merchantName;
    private String orgLevelCode;
    private Long zbMerchantId;
    private String zbMerchantCode;
    private String zbMerchantName;

    public String getZbMerchantCode() {
        return this.zbMerchantCode;
    }

    public void setZbMerchantCode(String zbMerchantCode) {
        this.zbMerchantCode = zbMerchantCode;
    }

    public String getMerchantCode() {
        return this.merchantCode;
    }

    public void setMerchantCode(String merchantCode) {
        this.merchantCode = merchantCode;
    }

    public Long getMerchantId() {
        return this.merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantName() {
        return this.merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getOrgLevelCode() {
        return this.orgLevelCode;
    }

    public void setOrgLevelCode(String orgLevelCode) {
        this.orgLevelCode = orgLevelCode;
    }

    public Long getZbMerchantId() {
        return this.zbMerchantId;
    }

    public void setZbMerchantId(Long zbMerchantId) {
        this.zbMerchantId = zbMerchantId;
    }

    public String getZbMerchantName() {
        return this.zbMerchantName;
    }

    public void setZbMerchantName(String zbMerchantName) {
        this.zbMerchantName = zbMerchantName;
    }

    public UserInfo() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMobile() {
        return this.mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getNickname() {
        return this.nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getHeadPicUrl() {
        return this.headPicUrl;
    }

    public void setHeadPicUrl(String headPicUrl) {
        this.headPicUrl = headPicUrl;
    }

    public String getIdentityCardName() {
        return this.identityCardName;
    }

    public void setIdentityCardName(String identityCardName) {
        this.identityCardName = identityCardName;
    }

    public void setUt(String ut) {
        this.ut = ut;
    }

    public String getUt() {
        return this.ut;
    }

    public Integer getLoginPlatformId() {
        return this.loginPlatformId;
    }

    public void setLoginPlatformId(Integer loginPlatformId) {
        this.loginPlatformId = loginPlatformId;
    }

    public String getUnionId() {
        return this.unionId;
    }

    public void setUnionId(String unionId) {
        this.unionId = unionId;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

