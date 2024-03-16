package com.fcb.coupon.app.infra.inteceptor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author YangHanBin
 * @date 2021-08-19 11:22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppAuthorityHolder {
    public static ThreadLocal<AppAuthorityHolder> AuthorityThreadLocal = new ThreadLocal<>();
    private AppUserInfo userInfo;
}
