package com.fcb.coupon.backend.remote.dto.out;

import lombok.Data;

import java.io.Serializable;

/**
 * 组织信息返回
 *
 * @Author Weihaiqi
 * @Date 2021-06-16 20:09
 **/
@Data
public class OrgOutDto implements Serializable {
    private static final long serialVersionUID = -1244646005023775734L;

    /** 组织id **/
    private Long orgId;
    /** 组织代码 **/
    private String orgCode;
    /** 组织名称 **/
    private String orgName;
}
