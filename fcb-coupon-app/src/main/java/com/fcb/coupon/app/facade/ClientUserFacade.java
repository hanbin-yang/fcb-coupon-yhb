package com.fcb.coupon.app.facade;

import com.fcb.coupon.app.infra.inteceptor.AppUserInfo;
import com.fcb.coupon.app.remote.dto.output.BrokerInfoSimpleDto;
import com.fcb.coupon.app.remote.dto.output.CustomerInfoSimpleOutput;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author YangHanBin
 * @date 2021-08-23 8:32
 */
public interface ClientUserFacade {


    AppUserInfo getSaasInfoByToken(String token);

    /**
     * 根据unionId获取B端用户信息，带缓存
     *
     * @param unionId unionId
     * @return AppUserInfo
     */
    AppUserInfo getMemberInfoByUnionId(String unionId);

    /**
     * 根据unionId获取B端用户信息
     *
     * @param unionId       unionId
     * @param cacheUserInfo 是否缓存用户信息
     * @return
     */
    AppUserInfo getMemberInfoByUnionId(String unionId, boolean cacheUserInfo);

    /**
     * 根据unionId获取C端用户信息，带缓存
     *
     * @param unionId unionId
     * @return AppUserInfo
     */
    AppUserInfo getCustomerInfoByUnionId(String unionId);

    /**
     * 根据unionId获取C端用户信息
     *
     * @param unionId       unionId
     * @param cacheUserInfo 是否缓存用户信息
     * @return
     */
    AppUserInfo getCustomerInfoByUnionId(String unionId, boolean cacheUserInfo);

    /**
     * 校验C端用户登录
     *
     * @param request request
     */
    Boolean validateCustomerLogin(HttpServletRequest request);

    /**
     * 校验B端用户登录
     *
     * @param request request
     */
    Boolean validateMemberLogin(HttpServletRequest request);

    /**
     * 校验Saas端用户登录
     *
     * @param request request
     */
    Boolean validateSaasLogin(HttpServletRequest request);

    /**
     * 根据hdToken获取C端用户信息
     *
     * @param hdToken      hdToken
     * @param terminalType terminalType
     * @return
     */
    AppUserInfo getCustomerInfoByHdTokenAndTerminalType(String hdToken, String terminalType);

    /**
     * 根据hdToken获取B端用户信息
     *
     * @param hdToken      hdToken
     * @param terminalType terminalType
     * @return
     */
    AppUserInfo getMemberInfoByHdTokenAndTerminalType(String hdToken, String terminalType);

    /**
     * 根据手机号获取C端用户信息 ，带缓存
     *
     * @param phone phone
     * @return
     */
    AppUserInfo getCustomerInfoByPhone(String phone);

    /**
     * 根据手机号获取B端用户信息，带缓存
     *
     * @param phone phone
     * @return
     */
    AppUserInfo getMemberInfoByPhone(String phone);

    /**
     * 根据手机号获取B端用户信息
     *
     * @param phones phones
     * @return
     */
    List<BrokerInfoSimpleDto> listMemberInfoByPhones(List<String> phones);

    /**
     * 根据手机号获取C端用户信息
     *
     * @param phones phones
     * @return
     */
    List<CustomerInfoSimpleOutput> listCustomerInfoByPhones(List<String> phones);
}
