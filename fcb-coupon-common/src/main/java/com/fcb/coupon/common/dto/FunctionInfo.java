package com.fcb.coupon.common.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 *
 * @Author WeiHaiQi
 * @Date 2021-06-17 17:33
 **/
public class FunctionInfo implements Serializable {

    private static final long serialVersionUID = -6986621773497717241L;
    private String functionCodes;
    private Set<String> functionPaths;
    private List<RoleDTO> roles;
    private List<FunctionPathTreeDTO> functionPathTree;

    public FunctionInfo() {
    }

    public List<RoleDTO> getRoles() {
        return this.roles;
    }

    public void setRoles(List<RoleDTO> roles) {
        this.roles = roles;
    }

    public String getFunctionCodes() {
        return this.functionCodes;
    }

    public void setFunctionCodes(String functionCodes) {
        this.functionCodes = functionCodes;
    }

    public Set<String> getFunctionPaths() {
        return this.functionPaths;
    }

    public void setFunctionPaths(Set<String> functionPaths) {
        this.functionPaths = functionPaths;
    }

    public List<FunctionPathTreeDTO> getFunctionPathTree() {
        return this.functionPathTree;
    }

    public void setFunctionPathTree(List<FunctionPathTreeDTO> functionPathTree) {
        this.functionPathTree = functionPathTree;
    }
}
