package com.fcb.coupon.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 *
 * @Author WeiHaiQi
 * @Date 2021-06-17 17:34
 **/
@JsonIgnoreProperties(
        ignoreUnknown = true
)
public class RoleDTO implements Serializable {

    private static final long serialVersionUID = -8321517890574491739L;
    private Long roleId;
    private String roleCode;
    private String roleName;
    private Long entityId;
    private Integer entityType;
    private Long platformId;

    public RoleDTO() {
    }

    public Long getEntityId() {
        return this.entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public Integer getEntityType() {
        return this.entityType;
    }

    public void setEntityType(Integer entityType) {
        this.entityType = entityType;
    }

    public Long getPlatformId() {
        return this.platformId;
    }

    public void setPlatformId(Long platformId) {
        this.platformId = platformId;
    }

    public Long getRoleId() {
        return this.roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getRoleCode() {
        return this.roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getRoleName() {
        return this.roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
