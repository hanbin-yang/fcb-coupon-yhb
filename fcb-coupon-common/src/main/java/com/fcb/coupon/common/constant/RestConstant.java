package com.fcb.coupon.common.constant;

/**
 * http调用外部接口地址
 * @author mashiqiong
 * @date 2021-6-18 18:23
 */
public class RestConstant {
    /**
     * ouser-web基址
     */
    public static final String OUSER_BASE_URL = "/ouser-web";
    /**
     * omc基址
     */
    public static final String OMC_BASE_URL = "/omc";

    /**
     * SAAS基址
     */
    public static final String SAAS_BASE_URL = "/saasapigateway";

    /**
     * fcb-activity服务基址
     */
    public static final String FCB_ACT_BASE_URL = "/backapi/activity";

    /**
     * 查询权限组织树url
     */
    public static final String QUERY_MERCHANT_TREE_URL = OUSER_BASE_URL + "/api/merchant/queryMerchantTree.do";
    /**
     * 批量根据unionId获取房车宝客户中心用户信息
     */
    public static final String GET_CUSTOMER_INFO_BY_UNIONID_LIST_URL = OMC_BASE_URL + "/api/customer/data/v1/getCustomerInfoByUnionIdList";
    /**
     * 通过orgAccount获取经纪人信息
     */
    public static final String GET_BROKER_LIST_BY_ACCOUNTS_URL = OMC_BASE_URL + "/api/broker/data/v1/getBrokerListByAccounts";
    /**
     * 根据手机号列表查询用户信息
     */
    public static final String GET_BROKER_INFO_BY_PHONE_URL = OMC_BASE_URL + "/api/broker/data/v1/getRegisterUserInfoList";
    /**
     * 根据电话号码批量获取C端注册用户信息
     */
    public static final String GET_REGISTER_USER_INFO_LIST_URL = OMC_BASE_URL + "/api/customer/data/v1/getRegisterUserInfoList";
    /**
     * 根据unionId获取C端用户信息
     */
    public static final String GET_CUSTOMER_INFO_BY_UNIONID_URL = OMC_BASE_URL + "/api/customer/data/v1/getCustomerInfoByUnionId";

    /**
     * 根据customerId获取C端用户信息
     */
    public static final String GET_CUSTOMER_INFO_BY_CUSTOMERID_URL = OMC_BASE_URL + "/api/customer/data/v1/getCustomerInfoByCustomerId";

    /**
     * 根据unionId查询用户信息
     */
    public static final String GET_BROKER_INFO_URL = OMC_BASE_URL + "/api/broker/data/v1/getBrokerInfoByUnionId";
    /**
     * 根据unionId查询用户信息
     */
    public static final String GET_BROKER_INFO_BY_UNION_ID_URL = OMC_BASE_URL + "/api/broker/data/v1/findBrokerInfoByUnionId";

    /**
     * 根据borkerid列表查询用户信息
     */
    public static final String GET_BROKER_INFO_BY_BROKERID_URL = OMC_BASE_URL + "/api/broker/data/v1/brokerInfo/list";

    /**
     * 刷新登录用户组织缓存
     */
    public static final String REFRESH_AUTHORITY = OUSER_BASE_URL + "/api/merchant/refreshAuthority.do";
    /**
     * 根据机构Ids获取名称
     */
    public static final String ORG_INFO_BY_IDS_URL = OUSER_BASE_URL + "/cloud/orgInfoNameService/findByOrgId";
    /**
     * 根据ut获取C端登录信息
     */
    public static final String C_LOGIN_BY_UT = "/appapi/customer/uucas/v1/judgeVirtualHallLogin";
    /**
     * 根据ut获取B端登录信息
     */
    public static final String B_LOGIN_BY_UT = "/appapi/broker/uucas/v1/judgeVirtualHallLogin";

    /**
     * C端校验是否登录
     */
    public static final String C_TOKEN_VALIDATE = "/appapi/customer/uucas/v1/checkAccessToken";
    /**
     * B端校验是否登录
     */
    public static final String B_TOKEN_VALIDATE = "/appapi/broker/uucas/v1/checkAccessToken";

    /**
     * 发语音验证码
     */
    public static final String SMS_VOICE_SEND = OMC_BASE_URL + "/sms/api/sms/voice/v1/send";

    /**
     * 刷新登录用户组织缓存
     */
    public static final String GET_BROKER_PROFILE = "/appapi/broker/member/v1/getProfile";

    /**
     * 移动端短链转发接口
     */
    public static final String GET_URL_SHORT = "/g/";

    /**
     * saas端登录校验
     */
    public static final String SASS_UER_LOGIN_CHECK =SAAS_BASE_URL+ "/api/thirdplat/params?apiNumber=001_checkTokenValid";

    /**
     * 查询促销活动 是否叠加优惠券
     */
    public static final String QUERY_ACTIVITY_INFO_BY_CHECK = "/innerapi/v1/promotion/activity/check";
    /**
     * 根据明源项目id查询底下楼盘
     */
    public static final String QUERY_PRODUCT_INFO = "/productInfo/queryProductInfoOutPut";

}
