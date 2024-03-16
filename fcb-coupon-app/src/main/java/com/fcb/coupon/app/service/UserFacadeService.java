package com.fcb.coupon.app.service;

import com.fcb.coupon.app.model.dto.RealUserInfoDto;

/**
 * 用户中心用户查询接口代理服务
 *
 * @Author WeiHaiQi
 * @Date 2021-08-16 17:12
 **/
public interface UserFacadeService {

    /**
     * 根据unionId获取用户中心的用户信息
     *
     * @param clientType    终端: B_USER C_USER
     * @param unionId       unionId
     * @return
     */
    RealUserInfoDto getRealUserInfo(String clientType, String unionId);

    /**
     * 根据unionId获取缓存中用户中心的用户信息
     *
     * @param clientType    终端: B_USER C_USER
     * @param unionId       unionId
     * @return
     */
    RealUserInfoDto getCacheRealUserInfo(String clientType, String unionId);
}
