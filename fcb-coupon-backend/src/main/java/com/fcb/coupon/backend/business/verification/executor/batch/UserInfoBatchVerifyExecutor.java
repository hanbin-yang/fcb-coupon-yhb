package com.fcb.coupon.backend.business.verification.executor.batch;

import com.alibaba.fastjson.JSONArray;
import com.fcb.coupon.backend.business.verification.context.BatchVerifyContext;
import com.fcb.coupon.backend.business.verification.context.VerifyServiceContext;
import com.fcb.coupon.backend.model.cache.CouponThemeCache;
import com.fcb.coupon.backend.model.dto.CouponImportVerifyResultDto;
import com.fcb.coupon.backend.model.dto.VerifyUserInfoDto;
import com.fcb.coupon.backend.model.entity.CouponEntity;
import com.fcb.coupon.backend.model.param.request.CouponVerifyImportRequest;
import com.fcb.coupon.backend.remote.dto.out.AgencyInfoOutputDto;
import com.fcb.coupon.backend.remote.dto.out.BrokerInfoSimpleDto;
import com.fcb.coupon.backend.remote.dto.out.CustomerInfoSimpleOutput;
import com.fcb.coupon.common.constant.CouponConstant;
import com.fcb.coupon.common.constant.InfraConstant;
import com.fcb.coupon.common.enums.CouponGiveRuleEnum;
import com.fcb.coupon.common.enums.UserTypeEnum;
import com.fcb.coupon.common.excel.bean.RowParseResult;
import com.fcb.coupon.common.exception.ResponseErrorCode;
import com.fcb.coupon.common.util.AESPromotionUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.MDC;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 用户校验相关执行器
 * @author YangHanBin
 * @date 2021-09-09 16:11
 */
public class UserInfoBatchVerifyExecutor extends AbstractBatchVerifyExecutor {
    private Map<String, String> couponCodeVerifyPhoneMap;

    public UserInfoBatchVerifyExecutor(BatchVerifyContext verifyContext, VerifyServiceContext serviceContext) {
        super(verifyContext, serviceContext);
    }

    @Override
    protected void before() {
        try {
            // 收集手机号或账号
            couponCodeVerifyPhoneMap = getVerifyContext().getRowParseResults().stream()
                    .map(bean -> (CouponVerifyImportRequest) bean.getRowBean())
                    .collect(Collectors.toMap(item -> AESPromotionUtil.encrypt(item.getCouponCode()), CouponVerifyImportRequest::getVerifyPhone));
            setUserTypeInfoMap();
        } catch (Exception e) {
            log.error("UserInfoBatchVerifyExecutor执行失败：taskId={}", getVerifyContext().getAsyncTaskId(), e);
            executeFail();
        }
    }

    @Override
    protected void doExecute() {
        validate();
    }

    @Override
    protected void after() {
        if (hasResult()) {
            return;
        }
        StoreInfoBatchVerifyExecutor delegate = new StoreInfoBatchVerifyExecutor(getVerifyContext(), getServiceContext());
        delegate.setCouponCodeEsDocsMap(getCouponCodeEsDocsMap());
        delegate.setDbCouponCodeMap(getDbCouponCodeMap());
        delegate.setCouponThemeIdCacheMap(getCouponThemeIdCacheMap());
        delegate.setUserTypeInfoMap(getUserTypeInfoMap());
        delegate.execute();
    }

    private void validate() {
        Iterator<RowParseResult> importDataIterator = getImportDataIterator();
        while (importDataIterator.hasNext()) {
            RowParseResult bean = importDataIterator.next();
            CouponVerifyImportRequest rowBean = (CouponVerifyImportRequest)bean.getRowBean();
            Integer rowNum = bean.getRowNum();
            String verifyPhone = rowBean.getVerifyPhone();
            String couponCode = AESPromotionUtil.encrypt(rowBean.getCouponCode());
            CouponEntity couponEntity = getDbCouponCodeMap().get(couponCode);
            Integer userType = couponEntity.getUserType();
            Long couponThemeId = couponEntity.getCouponThemeId();

            CouponThemeCache couponThemeCache = getCouponThemeIdCacheMap().get(couponThemeId);

            if (shouldSkipValidateUserInfo(couponThemeCache.getCouponGiveRule())) {
                // 认为是C端
                couponEntity.setUserType(UserTypeEnum.C.getUserType());
                continue;
            }

            Map<String, VerifyUserInfoDto> verifyUserInfoDtoMap = getUserTypeInfoMap().get(userType);
            VerifyUserInfoDto verifyUserInfoDto = verifyUserInfoDtoMap.get(verifyPhone);
            String crowdScopeIds = couponThemeCache.getApplicableUserTypes();
            JSONArray couponThemePubPorts = getCouponThemeApplicableUserTypes(crowdScopeIds);
            String dbUserId = couponEntity.getUserId();

            if (Objects.isNull(verifyUserInfoDto)) {
                verifyUserInfoDto = new VerifyUserInfoDto();
            }
            verifyUserInfoDto.setVerifyPhone(verifyPhone);
            verifyUserInfoDto.setApplicableUserTypes(couponThemePubPorts);
            verifyUserInfoDto.setDbUserId(dbUserId);
            verifyUserInfoDto.setDbBindTel(getCouponCodeEsDocsMap().get(couponCode).get(0).getBindTel());
            verifyUserInfoDto.setUserType(userType);
            // 批量核销标志
            verifyUserInfoDto.setVerifyFlag(CouponConstant.YES);
            // 用户信息校验
            ResponseErrorCode errorCode = validateUserInfo4Single(verifyUserInfoDto);
            if (errorCode != null) {
                CouponImportVerifyResultDto resultDto = prepareImportVerifyErrorCodeResultBean(CouponConstant.FAIL_MESSAGE, rowBean, errorCode);
                resultDto.setRowNum(rowNum);
                getVerifyContext().getVerifyResultMap().put(rowNum, resultDto);
                importDataIterator.remove();
            }
        }
    }

    private void setUserTypeInfoMap() {
        Map<Integer, Set<String>> userTypePhoneMap = new HashMap<>();
        couponCodeVerifyPhoneMap.forEach((k, v) -> {
            CouponEntity couponEntity = getDbCouponCodeMap().get(k);
            CouponThemeCache couponThemeCache = getCouponThemeIdCacheMap().get(couponEntity.getCouponThemeId());
            if (!CouponGiveRuleEnum.COUPON_GIVE_RULE_OFFLINE_PREFABRICATED.ifSame(couponThemeCache.getCouponGiveRule())) {
                Integer userType = couponEntity.getUserType();
                Set<String> phoneSet = userTypePhoneMap.computeIfAbsent(userType, bean -> new HashSet<>());
                phoneSet.add(v);
            }
        });

        Map<Integer, Map<String, VerifyUserInfoDto>> userTypeInfoMap = new HashMap<>();

        for (Map.Entry<Integer, Set<String>> entry : userTypePhoneMap.entrySet()) {
            Integer userType = entry.getKey();
            Set<String> phones = entry.getValue();
            switch (UserTypeEnum.of(userType)) {
                case C:
                    // C端
                    Map<String, VerifyUserInfoDto> cUserInfoMap = queryCPortInfoForVerifyBatch(phones);
                    userTypeInfoMap.put(userType, cUserInfoMap);
                    break;
                case B:
                    // B端
                    Map<String, VerifyUserInfoDto> memberUserInfoMap = queryMemberInfoForVerifyBatch(phones);
                    userTypeInfoMap.put(userType, memberUserInfoMap);
                    break;
                case SAAS:
                    // SAAS端
                    Map<String, VerifyUserInfoDto> agencyUserInfoMap = querySaasInfoForVerifyBatch(phones);
                    userTypeInfoMap.put(userType, agencyUserInfoMap);
                    break;
                default:
                    break;
            }
        }

        setUserTypeInfoMap(userTypeInfoMap);
    }

    private Map<String, VerifyUserInfoDto> queryCPortInfoForVerifyBatch(Set<String> phones) {
        Map<String, VerifyUserInfoDto> phoneUserInfoMap = new ConcurrentHashMap<>(phones.size());
        String traceId = MDC.get(InfraConstant.TRACE_ID);
        Lists.partition(new ArrayList<>(phones), PAGE_SIZE).parallelStream().forEach(subList -> {
            try {
                MDC.put(InfraConstant.TRACE_ID, traceId);
                List<CustomerInfoSimpleOutput> customerInfoList = getCustomerInfoBatchByPhones(subList);
                if (CollectionUtils.isNotEmpty(customerInfoList)) {
                    customerInfoList.forEach(bean -> {
                        VerifyUserInfoDto verifyUserInfoDto = new VerifyUserInfoDto();
                        verifyUserInfoDto.setVerifyUserId(bean.getCustomerId());
                        verifyUserInfoDto.setVerifyUnionId(bean.getUnionId());
                        phoneUserInfoMap.put(bean.getPhoneNo(), verifyUserInfoDto);
                    });
                }
            } finally {
                MDC.remove(InfraConstant.TRACE_ID);
            }
        });

        return phoneUserInfoMap;
    }

    private Map<String, VerifyUserInfoDto> queryMemberInfoForVerifyBatch(Set<String> phones) {
        Map<String, VerifyUserInfoDto> phoneUserInfoMap = new ConcurrentHashMap<>(phones.size());
        String traceId = MDC.get(InfraConstant.TRACE_ID);
        Lists.partition(new ArrayList<>(phones), PAGE_SIZE).parallelStream().forEach(subList -> {
            try {
                MDC.put(InfraConstant.TRACE_ID, traceId);
                List<BrokerInfoSimpleDto> subMemberList = getMemberInfoBatchByPhones(subList);
                if (CollectionUtils.isNotEmpty(subMemberList)) {
                    for (BrokerInfoSimpleDto dto : subMemberList) {
                        VerifyUserInfoDto verifyUserInfoDto = new VerifyUserInfoDto();
                        verifyUserInfoDto.setVerifyUserId(dto.getBrokerId());
                        verifyUserInfoDto.setIsDisabled(dto.getIsDisabled());
                        verifyUserInfoDto.setVerifyUnionId(dto.getUnionId());
                        phoneUserInfoMap.put(dto.getPhoneNo(), verifyUserInfoDto);
                    }
                }
            } finally {
                MDC.remove(InfraConstant.TRACE_ID);
            }
        });
        return phoneUserInfoMap;
    }

    private Map<String, VerifyUserInfoDto> querySaasInfoForVerifyBatch(Set<String> accounts) {
        Map<String, VerifyUserInfoDto> accountUserInfoMap = new ConcurrentHashMap<>(accounts.size());
        String traceId = MDC.get(InfraConstant.TRACE_ID);
        Lists.partition(new ArrayList<>(accounts), PAGE_SIZE).parallelStream().forEach(subList -> {
            MDC.put(InfraConstant.TRACE_ID, traceId);
            List<AgencyInfoOutputDto> agencyInfoList = getSaasInfoBatchByAccounts(subList);
            if (CollectionUtils.isNotEmpty(agencyInfoList)) {
                agencyInfoList.forEach(dto -> {
                    VerifyUserInfoDto verifyUserInfoDto = new VerifyUserInfoDto();
                    verifyUserInfoDto.setVerifyUserId(dto.getBrokerId());
                    verifyUserInfoDto.setIsDisabled(dto.getIsDisabled());
                    verifyUserInfoDto.setVerifyUnionId(dto.getUnionId());
                    accountUserInfoMap.put(dto.getOrgAccount(), verifyUserInfoDto);
                });
            }
        });
        return accountUserInfoMap;
    }
}
