package com.fcb.coupon.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author YangHanBin
 * @date 2021-06-11 10:14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorityHolder {
    public static ThreadLocal<AuthorityHolder> AuthorityThreadLocal = new ThreadLocal<>();
    private UserInfo userInfo;
    private FunctionInfo functionInfo;
}
