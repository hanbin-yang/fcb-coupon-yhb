package com.fcb.coupon.backend.remote.dto.input;

import com.fcb.coupon.backend.remote.dto.Pagination;

import java.util.List;

/**
 * 查询组织请求入参
 *
 * @Author Weihaiqi
 * @Date 2021-06-16 20:23
 **/
public class OrgInfoDto extends Pagination {

    private List<Long> ids;
    //公司ID
    private Long companyId;

    private String orgName;

    private Integer orgInfoType;

    private String orgCode;

    private List<Integer> orgInfoTypes;

    //是否查询全部楼盘包括未上线的，默认只是查询上线的
    private Boolean queryIsAll;

    /**
     * 楼盘上线类型  B端: BUILD_ONLINE_STATUS
     * C端: CPOINT_BUILD_ONLINE_STATUS
     * 机构端: ORG_POINT_BUILD_ONLINE_STATUS
     * 传入多个是并且关系只取并集查询上线的楼盘数据
     */
    private List<String> storePortTypes;

    public List<String> getStorePortTypes() {
        return storePortTypes;
    }

    public void setStorePortTypes(List<String> storePortTypes) {
        this.storePortTypes = storePortTypes;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public List<Integer> getOrgInfoTypes() {
        return orgInfoTypes;
    }

    public void setOrgInfoTypes(List<Integer> orgInfoTypes) {
        this.orgInfoTypes = orgInfoTypes;
    }

    public Integer getOrgInfoType() {
        return orgInfoType;
    }

    public void setOrgInfoType(Integer orgInfoType) {
        this.orgInfoType = orgInfoType;
    }

    public Boolean getQueryIsAll() {
        return queryIsAll;
    }

    public void setQueryIsAll(Boolean queryIsAll) {
        this.queryIsAll = queryIsAll;
    }
}
