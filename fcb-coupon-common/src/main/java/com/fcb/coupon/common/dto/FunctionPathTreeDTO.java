package com.fcb.coupon.common.dto;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @Author WeiHaiQi
 * @Date 2021-06-17 17:35
 **/
public class FunctionPathTreeDTO implements Serializable {

    private static final long serialVersionUID = 5280790004679949691L;
    private List<String> paths;
    private String parentCode;

    public FunctionPathTreeDTO() {
    }

    public String getParentCode() {
        return this.parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }

    public List<String> getPaths() {
        return this.paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }
}
