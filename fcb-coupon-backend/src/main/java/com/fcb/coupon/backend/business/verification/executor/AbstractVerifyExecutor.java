package com.fcb.coupon.backend.business.verification.executor;

import cn.hutool.core.lang.Validator;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fcb.coupon.backend.business.verification.context.VerifyServiceContext;
import com.fcb.coupon.backend.elasticsearch.CouponEsDoc;
import com.fcb.coupon.backend.exception.CouponVerificationErrorCode;
import com.fcb.coupon.backend.exception.MktUseRuleErrorCode;
import com.fcb.coupon.backend.model.bo.CouponSingleVerifyBo;
import com.fcb.coupon.backend.model.cache.CouponThemeCache;
import com.fcb.coupon.backend.model.dto.*;
import com.fcb.coupon.backend.model.entity.CouponEntity;
import com.fcb.coupon.backend.model.entity.CouponVerificationEntity;
import com.fcb.coupon.backend.model.entity.MktUseRuleEntity;
import com.fcb.coupon.backend.model.param.response.PageResponse;
import com.fcb.coupon.backend.remote.dto.input.BrokerInfoSimpleInputDto;
import com.fcb.coupon.backend.remote.dto.input.CustomerInfoSimpleInput;
import com.fcb.coupon.backend.remote.dto.input.InputDto;
import com.fcb.coupon.backend.remote.dto.input.OrgInfoDto;
import com.fcb.coupon.backend.remote.dto.out.AgencyInfoOutputDto;
import com.fcb.coupon.backend.remote.dto.out.BrokerInfoSimpleDto;
import com.fcb.coupon.backend.remote.dto.out.CustomerInfoSimpleOutput;
import com.fcb.coupon.backend.remote.dto.out.OutputDto;
import com.fcb.coupon.common.constant.CouponConstant;
import com.fcb.coupon.common.constant.InfraConstant;
import com.fcb.coupon.common.constant.VerificationChannel;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.enums.*;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.exception.ResponseErrorCode;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 抽象执行器
 * @author YangHanBin
 * @date 2021-09-09 9:54
 */
public abstract class AbstractVerifyExecutor implements VerifyExecutor {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    // 分页参数
    protected int PAGE_SIZE = 200;
    // 核销需要的Service服务类
    @Getter
    private final VerifyServiceContext serviceContext;
    // 核销人id
    @Getter
    private final Long verifyUserId;
    // 核销人name
    @Getter
    private final String verifyUsername;

    public AbstractVerifyExecutor(VerifyServiceContext serviceContext, Long verifyUserId, String verifyUsername) {
        this.serviceContext = serviceContext;
        this.verifyUserId = verifyUserId;
        this.verifyUsername = verifyUsername;
    }

    protected ResponseErrorCode validateCouponDb4Single(CouponEntity dbCoupon) {
        // 已经是核销状态
        if(Objects.equals(dbCoupon.getStatus(), CouponStatusEnum.STATUS_USED.getStatus())) {
            return CouponVerificationErrorCode.COUPON_CODE_USED;
        }
        // 校验 券有效期
        return validateEffDate4Single(dbCoupon.getStartTime(), dbCoupon.getEndTime());
    }

    private ResponseErrorCode validateEffDate4Single(Date couponStartTime, Date couponEndTime) {
        Date nowDate = new Date();
        // 比较有效期是否开始
        if(couponStartTime!=null && couponStartTime.after(nowDate)){
            return CouponVerificationErrorCode.EFFECT_DATA_NOT_START;
        }
        // 比较时间是否过期
        if (couponEndTime != null && couponEndTime.before(nowDate)) {
            return CouponVerificationErrorCode.EFFECT_DATA_ENDED;
        }
        return null;
    }

    protected List<CustomerInfoSimpleOutput> getCustomerInfoBatchByPhones(List<String> phones) {
        CustomerInfoSimpleInput input = new CustomerInfoSimpleInput();
        input.setPhoneNoList(phones);
        ResponseDto<List<CustomerInfoSimpleOutput>> listResponseDto = serviceContext.getCustomerClient().listCustomerInfoByPhones(input);
        if (listResponseDto == null || CollectionUtils.isEmpty(listResponseDto.getData())) {
            log.error("根据电话号码批量查询C端用户返回空, out={}, phones={}", JSON.toJSONString(listResponseDto), phones.toString());
            return Collections.emptyList();
        }

        return listResponseDto.getData();
    }

    protected List<AgencyInfoOutputDto> getSaasInfoBatchByAccounts(List<String> mobiles) {
        throw new UnsupportedOperationException("SAAS端核销暂不支持");
    }

    protected List<BrokerInfoSimpleDto> getMemberInfoBatchByPhones(List<String> mobiles) {
        BrokerInfoSimpleInputDto param = new BrokerInfoSimpleInputDto();
        param.setPhoneNoList(mobiles);
        ResponseDto<List<BrokerInfoSimpleDto>> out = serviceContext.getBrokerClient().getBrokerInfoListByPhones(param);
        if (out == null || CollectionUtils.isEmpty(out.getData())) {
            log.error("根据电话号码批量查询B端用户返回空, out={}, accounts={}", JSON.toJSONString(out), mobiles.toString());
            return Collections.emptyList();
        }
        return out.getData();
    }

    /**
     * 根据楼盘编码ids查询楼盘详情
     * @param storeInfoInputDto
     * @return
     */
    protected List<StoreInfoOutDto> queryVerifyStoreInfoBatch(StoreInfoInputDto storeInfoInputDto) {
        InputDto<StoreInfoInputDto> inputDto = new InputDto<>();
        inputDto.setData(storeInfoInputDto);
        OutputDto<PageResponse<StoreInfoOutDto>> out = getServiceContext().getOuserWebFeignClient().queryStoreOrgPageByParams(inputDto);

        if (Objects.isNull(out) || Objects.isNull(out.getData())) {
            log.error("根据storeIds或buildCodes查询楼盘详情 error: out={}, storeIds={}", JSONUtil.toJsonStr(out), JSON.toJSONString(inputDto));
            return null;
        } else {
            return out.getData().getListObj();
        }
    }

    protected JSONArray getCouponThemeApplicableUserTypes(String crowdScopeIds) {
        JSONObject jsonObject = JSON.parseObject(crowdScopeIds);
        return jsonObject.getJSONArray("ids");
    }

    protected ResponseErrorCode validateStore4Single(ValidateStoreInfoDto dto) {
        // 平台券只校验上下架
        if (OrgLevelEnum.PLATFORM.getThemeType().equals(dto.getThemeType())) {
            // 只校验上下架
            return validateStoreOnline4Single(dto);
        } else {
            // 先校验上下架
            ResponseErrorCode errorCode = validateStoreOnline4Single(dto);
            if (errorCode != null) {
                return errorCode;
            }
            // 再校验适用楼盘
            errorCode = validateApplicableStore4Single(dto.getCouponThemeId(), dto.getStoreId());
            return errorCode;
        }
    }

    private ResponseErrorCode validateApplicableStore4Single(Long couponThemeId, Long verifyStoreId) {
        Set<Long> applicableStoreIds = getApplicableStoreIds(couponThemeId);
        if (!applicableStoreIds.contains(verifyStoreId)) {
            return CouponVerificationErrorCode.STORE_NOT_IN_RANGE;
        }
        return null;
    }

    private Set<Long> getApplicableStoreIds(Long couponThemeId) {
        Set<Long> storeIdSet = getServiceContext().getApplicableStoreCache().getIfPresent(couponThemeId);
        if (CollectionUtils.isEmpty(storeIdSet)) {
            synchronized (getServiceContext().getCouponThemeIdIntern().intern(couponThemeId)) {
                storeIdSet = getServiceContext().getApplicableStoreCache().getIfPresent(couponThemeId);
                if (CollectionUtils.isEmpty(storeIdSet)) {
                    storeIdSet = doGetApplicableStoreIds(couponThemeId);
                    getServiceContext().getApplicableStoreCache().put(couponThemeId, storeIdSet);
                }
            }
        }
        return storeIdSet;
    }

    private Set<Long> doGetApplicableStoreIds(Long couponThemeId) {
        LambdaQueryWrapper<MktUseRuleEntity> queryWrapper = Wrappers.lambdaQuery(MktUseRuleEntity.class);
        queryWrapper.select(MktUseRuleEntity::getLimitRef)
                .eq(MktUseRuleEntity::getThemeRef, couponThemeId);
        // 获取适用组织
        List<MktUseRuleEntity> applicableOrgList = serviceContext.getMktUseRuleService().getBaseMapper().selectList(queryWrapper);
        if (CollectionUtils.isEmpty(applicableOrgList)) {
            log.error("根据couponThemId查询适用组织，券活动没有配置任何组织，couponThemeId={}", couponThemeId);
            throw new BusinessException(CouponVerificationErrorCode.NO_ORG_IN_COUPON_THEME);
        }
        List<Long> applicableOrgIds = applicableOrgList.stream().map(MktUseRuleEntity::getLimitRef).collect(Collectors.toList());

        // 适用楼盘ids
        String traceId = MDC.get(InfraConstant.TRACE_ID);
        Set<Long> applicableStoreIdSet = Collections.newSetFromMap(new ConcurrentHashMap<>(512));
        Lists.partition(applicableOrgIds, PAGE_SIZE).parallelStream().forEach(subList -> {
            try {
                MDC.put(InfraConstant.TRACE_ID, traceId);
                // 根据适用组织获取适用楼盘
                List<StoreInfoOutDto> storeInfoOutDtoList = queryChildStores(subList);
                if (storeInfoOutDtoList != null && !storeInfoOutDtoList.isEmpty()) {
                    Set<Long> storeIds = storeInfoOutDtoList.stream().map(StoreInfoOutDto::getStoreId).collect(Collectors.toSet());
                    // 装载适用楼盘ids
                    applicableStoreIdSet.addAll(storeIds);
                }
            } finally {
                MDC.remove(InfraConstant.TRACE_ID);
            }
        });

        return applicableStoreIdSet;
    }

    /**
     * SOA 根据组织ids查询底下所有关联楼盘
     * @param orgList 入参
     * @return 楼盘详情列表
     */
    private List<StoreInfoOutDto> queryChildStores(List<Long> orgList) {
        OrgInfoDto orgInfoDto = new OrgInfoDto();
        orgInfoDto.setIds(orgList);

        InputDto<OrgInfoDto> inputDto = new InputDto<>();
        inputDto.setData(orgInfoDto);
        OutputDto<List<StoreInfoOutDto>> outputDto = serviceContext.getOuserWebFeignClient().queryAllStore(inputDto);
        if (Objects.isNull(outputDto)) {
            log.error("根据适用组织查询适用楼盘 error: outputDto null, inputDto={}", JSONUtil.toJsonStr(inputDto));
            return null;
        } else {
            return outputDto.getData();
        }
    }

    private ResponseErrorCode validateStoreOnline4Single(ValidateStoreInfoDto dto) {
        switch (UserTypeEnum.of(dto.getUserType())) {
            case B:
                // 不在B端上线，抛出
                if (!Objects.equals(dto.getBuildOnlineStatus(), 1)) {
                    return MktUseRuleErrorCode.STORE_OFFLINE;
                    //throw new BusinessException(MktUseRuleErrorCode.STORE_OFFLINE);
                }
                break;
            case C:
                // 不在C端上线，抛出
                if (!Objects.equals(dto.getCpointBuildOnlineStatus(), 1)) {
                    return MktUseRuleErrorCode.STORE_OFFLINE;
                }
                break;
            case SAAS:
                // 不在机构上线，抛出
                if(!Objects.equals(dto.getOrgPointBuildOnlineStatus(), 1)) {
                    return MktUseRuleErrorCode.STORE_OFFLINE;
                }
                // 券活动配置了会员适用人群，不在会员端上线，抛出
                else if (dto.getCouponThemePubPorts().contains(UserTypeEnum.B.getUserType()) && !Objects.equals(dto.getBuildOnlineStatus(), 1)) {
                    return MktUseRuleErrorCode.STORE_OFFLINE;
                }
                // 券活动配置了C端适用人群，不在C端上线，抛出
                else if (dto.getCouponThemePubPorts().contains(UserTypeEnum.C.getUserType()) && !Objects.equals(dto.getCpointBuildOnlineStatus(), 1)) {
                    return MktUseRuleErrorCode.STORE_OFFLINE;
                }
                break;
            default:
                break;
        }
        return null;
    }

    protected boolean shouldSkipValidateUserInfo(Integer couponGiveRule) {
        // 线下预制券不校验用户信息
        return CouponGiveRuleEnum.COUPON_GIVE_RULE_OFFLINE_PREFABRICATED.getType().equals(couponGiveRule);
    }

    protected ResponseErrorCode validateUserInfo4Single(VerifyUserInfoDto dto) {
        Integer userType = dto.getUserType();
        String verifyPhone = dto.getVerifyPhone();
        if (!dto.getApplicableUserTypes().contains(userType)) {
            throw new BusinessException(CouponVerificationErrorCode.CROWD_SCOPE_NOT_MATCH.getCode(), String.format(CouponVerificationErrorCode.CROWD_SCOPE_NOT_MATCH.getMessage(), UserTypeEnum.getStrByUserType(userType)));
        }

        switch (UserTypeEnum.of(dto.getUserType())) {
            case C:
                if (!isTelPhone(dto.getVerifyPhone())) {
                    return CouponVerificationErrorCode.IS_NO_PHONE_PATTERN;
                }

                // 适应单个核销
                if (Objects.equals(CouponConstant.NO, dto.getVerifyFlag())) {
                    List<CustomerInfoSimpleOutput> customerInfoList = getCustomerInfoBatchByPhones(Collections.singletonList(verifyPhone));
                    if (CollectionUtils.isNotEmpty(customerInfoList)) {
                        CustomerInfoSimpleOutput customerInfo = customerInfoList.get(0);
                        dto.setVerifyUserId(customerInfo.getCustomerId());
                        dto.setVerifyUnionId(customerInfo.getUnionId());
                    }
                }

                if (StringUtils.isBlank(dto.getVerifyUserId())) {
                    // 用户信息查不到，就降级比较下核销手机号和绑定手机号
                    if (StringUtils.isNotBlank(dto.getDbBindTel()) && !StringUtils.equals(dto.getDbBindTel(), verifyPhone)) {
                        return CouponVerificationErrorCode.PHONE_NOT_MATCH_COUPON;
                    }
                    log.warn("warn validateUserInfoForSingleVerify: dto={}", JSON.toJSONString(dto));
                    return CouponVerificationErrorCode.PHONE_NOT_REGISTER;
                }
                break;
            case B:
                if (!isTelPhone(dto.getVerifyPhone())) {
                    return CouponVerificationErrorCode.IS_NO_PHONE_PATTERN;
                }

                if (Objects.equals(CouponConstant.NO, dto.getVerifyFlag())) {
                    List<BrokerInfoSimpleDto> memberInfoList = getMemberInfoBatchByPhones(Collections.singletonList(verifyPhone));
                    if (CollectionUtils.isNotEmpty(memberInfoList)) {
                        BrokerInfoSimpleDto brokerInfo = memberInfoList.get(0);
                        dto.setVerifyUserId(brokerInfo.getBrokerId());
                        dto.setIsDisabled(brokerInfo.getIsDisabled());
                        dto.setVerifyUnionId(brokerInfo.getUnionId());
                    }
                }
                if (StringUtils.isBlank(dto.getVerifyUserId())) {
                    // 用户信息查不到，就降级比较下核销手机号和绑定手机号
                    if (StringUtils.isNotBlank(dto.getDbBindTel()) && !StringUtils.equals(dto.getDbBindTel(), verifyPhone)) {
                        return CouponVerificationErrorCode.PHONE_NOT_MATCH_COUPON;
                    }
                    log.warn("warn validateUserInfoForSingleVerify: dto={}", JSON.toJSONString(dto));
                    return CouponVerificationErrorCode.PHONE_NOT_REGISTER;
                }
                if (Objects.equals(dto.getIsDisabled(), CouponConstant.YES)) {
                    return CouponVerificationErrorCode.ACCOUNT_DISABLE;
                }
                break;
            case SAAS:
                // 机构账号不应该是手机号格式的
                if (isTelPhone(verifyPhone)) {
                    return CouponVerificationErrorCode.IS_NO_AGENCY;
                }

                if (Objects.equals(CouponConstant.NO, dto.getVerifyFlag())) {
                    // 机构校验
                    List<AgencyInfoOutputDto> agencyInfoList = getSaasInfoBatchByAccounts(Collections.singletonList(verifyPhone));
                    if (CollectionUtils.isNotEmpty(agencyInfoList)) {
                        AgencyInfoOutputDto agencyInfo = agencyInfoList.get(0);
                        dto.setVerifyUserId(agencyInfo.getBrokerId());
                        dto.setIsDisabled(agencyInfo.getIsDisabled());
                        dto.setVerifyUnionId(agencyInfo.getUnionId());
                    }
                }

                if (StringUtils.isBlank(dto.getVerifyUserId())) {
                    // 用户信息查不到，就降级比较下核销手机号和绑定手机号
                    if (StringUtils.isNotBlank(dto.getDbBindTel()) && !StringUtils.equals(dto.getDbBindTel(), verifyPhone)) {
                        return CouponVerificationErrorCode.PHONE_NOT_MATCH_COUPON;
                    }
                    log.warn("warn validateUserInfoForSingleVerify: dto={}", JSON.toJSONString(dto));
                    return CouponVerificationErrorCode.PHONE_NOT_REGISTER;
                }
                if (Objects.equals(dto.getIsDisabled(), CouponConstant.YES)) {
                    return CouponVerificationErrorCode.ACCOUNT_DISABLE;
                }
                break;
        }

        if (!StringUtils.equals(dto.getVerifyUserId(), dto.getDbUserId())) {
            return CouponVerificationErrorCode.PHONE_NOT_MATCH_COUPON;
        }

        return null;
    }

    protected List<CouponEsDoc> getCouponEsDocList(BoolQueryBuilder boolQueryBuilder) {
        // 查询已发行 可使用 已使用 3种状态的
        List<Integer> statusList = new ArrayList<>();
        statusList.add(CouponStatusEnum.STATUS_USED.getStatus());
        statusList.add(CouponStatusEnum.STATUS_USE.getStatus());
        statusList.add(CouponStatusEnum.STATUS_ISSUE.getStatus());
        boolQueryBuilder.filter(QueryBuilders.termsQuery(CouponEsDoc.STATUS, statusList));

        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
        Page<CouponEsDoc> resultList = getServiceContext().getCouponEsDocService().searchPage(nativeSearchQueryBuilder);
        return resultList.getContent();
    }

    private boolean isTelPhone(String phone) {
        return Validator.isMobile(phone);
    }


    protected CouponGrowingDto prepareCouponGrowingDtoBean(CouponSingleVerifyBo bo) {
        CouponGrowingDto dto = new CouponGrowingDto();
        dto.setUsedTime(bo.getUsedTime());
        dto.setCouponThemeId(bo.getCouponThemeId());
        dto.setThemeTitle(bo.getThemeTitle());
        dto.setUsedStoreId(bo.getUsedStoreId());
        dto.setUsedStoreName(bo.getUsedStoreName());
        dto.setUserType(bo.getUserType());
        return dto;
    }

    protected CouponSingleVerifyBo prepareSingleVerifyBoBean(String verifyPhone, String subscribeCode, CouponEntity couponEntity, StoreInfoOutDto verifyStoreInfo, CouponThemeCache couponThemeCache) {
        CouponSingleVerifyBo couponSingleVerifyBo = new CouponSingleVerifyBo();
        couponSingleVerifyBo.setCouponId(couponEntity.getId());
        couponSingleVerifyBo.setCouponThemeId(couponEntity.getCouponThemeId());
        couponSingleVerifyBo.setCouponDiscountType(couponEntity.getCouponDiscountType());
        couponSingleVerifyBo.setCouponValue(couponEntity.getCouponValue());
        couponSingleVerifyBo.setCouponCode(couponEntity.getCouponCode());
        couponSingleVerifyBo.setBindTel(verifyPhone);
        couponSingleVerifyBo.setSubscribeCode(subscribeCode);
        couponSingleVerifyBo.setCouponOldVersionNo(couponEntity.getVersionNo());

        couponSingleVerifyBo.setThemeTitle(couponThemeCache.getThemeTitle());
        couponSingleVerifyBo.setStartTime(couponEntity.getStartTime());
        couponSingleVerifyBo.setEndTime(couponEntity.getEndTime());
        couponSingleVerifyBo.setBindUserId(couponEntity.getUserId());
        couponSingleVerifyBo.setUserType(couponEntity.getUserType());
        couponSingleVerifyBo.setCouponCreateTime(couponEntity.getCreateTime());

        couponSingleVerifyBo.setUsedStoreId(verifyStoreInfo.getStoreId());
        couponSingleVerifyBo.setUsedStoreCode(verifyStoreInfo.getBuildCode());
        couponSingleVerifyBo.setUsedStoreName(verifyStoreInfo.getStoreName());

        couponSingleVerifyBo.setUserId(getVerifyUserId());
        couponSingleVerifyBo.setUsername(getVerifyUsername());
        couponSingleVerifyBo.setUsedTime(new Date());

        if (CouponGiveRuleEnum.COUPON_GIVE_RULE_OFFLINE_PREFABRICATED.ifSame(couponThemeCache.getCouponGiveRule())) {
            couponSingleVerifyBo.setOfflineCouponFlag(Boolean.TRUE);
        }
        return couponSingleVerifyBo;
    }

    protected void doSingleVerify(CouponSingleVerifyBo bo) {
        // 准备coupon_verification表数据
        CouponVerificationEntity entity = prepareCouponVerificationInsertBean(bo);
        // 开启事务
        getServiceContext().getCouponVerificationService().couponVerifyWithTx(entity, bo.getCouponOldVersionNo(), bo.getOfflineCouponFlag());
    }

    protected CouponVerificationEntity prepareCouponVerificationInsertBean(CouponSingleVerifyBo bo) {
        CouponVerificationEntity entity = new CouponVerificationEntity();
        entity.setCouponId(bo.getCouponId());
        entity.setCouponThemeId(bo.getCouponThemeId());
        entity.setCouponDiscountType(bo.getCouponDiscountType());
        entity.setCouponValue(bo.getCouponValue());
        entity.setCouponCode(bo.getCouponCode());

        entity.setStatus(CouponStatusEnum.STATUS_USED.getStatus());
        // 后台核销 现在订单号和明源认购书编号一致，但是明源认购书编号不唯一
        // 所以现在后台核销没法做单笔订单限制校验
        entity.setSubscribeCode(bo.getSubscribeCode());
        entity.setOrderCode(bo.getSubscribeCode());
        // 核销店铺信息
        entity.setUsedStoreId(bo.getUsedStoreId());
        entity.setUsedStoreCode(bo.getUsedStoreCode());
        entity.setUsedStoreName(bo.getUsedStoreName());

        entity.setCreateUserid(bo.getUserId());
        entity.setCreateUsername(bo.getUsername());

        entity.setVerifyUserid(bo.getUserId());
        entity.setVerifyUsername(bo.getUsername());
        entity.setUsedChannel(VerificationChannel.BACKEND);

        entity.setThemeTitle(bo.getThemeTitle());
        entity.setBindUserId(bo.getBindUserId());
        entity.setUserType(bo.getUserType());
        entity.setUsedTime(bo.getUsedTime());
        entity.setCouponCreateTime(bo.getCouponCreateTime());
        entity.setVerifyUserid(bo.getUserId());
        entity.setVerifyUsername(bo.getUsername());
        entity.setStartTime(bo.getStartTime());
        entity.setEndTime(bo.getEndTime());
        entity.setBindTel(bo.getBindTel());
        entity.setIsDeleted(YesNoEnum.NO.getValue());
        entity.setCreateTime(bo.getUsedTime());
        entity.setVersionNo(YesNoEnum.NO.getValue());

        return entity;
    }

    /**
     * 埋点
     * @param dtoList
     */
    protected void sendGrowingMessage(List<CouponGrowingDto> dtoList) {
        try {
            String jsonString = JSON.toJSONString(dtoList);
            log.info("核销埋点发kafa消息data:" + jsonString);
            getServiceContext().getKafkaTemplate().send("GROWING_COUPON_VERIFICATION_TOPIC", jsonString);
            log.info("核销埋点发kafa消息data end");
        } catch (Exception ex) {
            log.error("核销埋点发kafa消息发送出错:", ex);
        }
    }
}
