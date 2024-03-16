package com.fcb.coupon.backend.business.verification.executor.batch;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.fcb.coupon.backend.business.verification.context.BatchVerifyContext;
import com.fcb.coupon.backend.business.verification.context.VerifyServiceContext;
import com.fcb.coupon.backend.model.cache.CouponThemeCache;
import com.fcb.coupon.backend.model.dto.CouponImportVerifyResultDto;
import com.fcb.coupon.backend.model.dto.StoreInfoInputDto;
import com.fcb.coupon.backend.model.dto.StoreInfoOutDto;
import com.fcb.coupon.backend.model.dto.ValidateStoreInfoDto;
import com.fcb.coupon.backend.model.entity.CouponEntity;
import com.fcb.coupon.backend.model.param.request.CouponVerifyImportRequest;
import com.fcb.coupon.common.constant.CouponConstant;
import com.fcb.coupon.common.constant.InfraConstant;
import com.fcb.coupon.common.excel.bean.RowParseResult;
import com.fcb.coupon.common.exception.CommonErrorCode;
import com.fcb.coupon.common.exception.ResponseErrorCode;
import com.fcb.coupon.common.util.AESPromotionUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.MDC;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 楼盘校验相关执行器
 * @author YangHanBin
 * @date 2021-09-09 15:27
 */
public class StoreInfoBatchVerifyExecutor extends AbstractBatchVerifyExecutor {
    public StoreInfoBatchVerifyExecutor(BatchVerifyContext verifyContext, VerifyServiceContext serviceContext) {
        super(verifyContext, serviceContext);
    }

    @Override
    protected void before() {
        try {
            // 根据楼盘编码 查询需要核销的店铺详情 包含了楼盘上下架信息
            setBuildCodeStoreInfoMap();
        } catch (Exception e) {
            log.error("StoreInfoBatchVerifyExecutor执行失败：taskId={}", getVerifyContext().getAsyncTaskId(), e);
            executeFail();
            throw e;
        }

        getBuildCodeStoreInfoMap();
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
        WriteBatchVerifyExecutor delegate = new WriteBatchVerifyExecutor(getVerifyContext(), getServiceContext());
        delegate.setCouponCodeEsDocsMap(getCouponCodeEsDocsMap());
        delegate.setDbCouponCodeMap(getDbCouponCodeMap());
        delegate.setCouponThemeIdCacheMap(getCouponThemeIdCacheMap());
        delegate.setUserTypeInfoMap(getUserTypeInfoMap());
        delegate.setBuildCodeStoreInfoMap(getBuildCodeStoreInfoMap());
        delegate.execute();
    }

    private void setBuildCodeStoreInfoMap() {
        List<String> buildCodes = getVerifyContext().getRowParseResults().stream().map(bean -> (CouponVerifyImportRequest) bean.getRowBean()).map(CouponVerifyImportRequest::getBuildCode).distinct().collect(Collectors.toList());

        List<StoreInfoOutDto> storeInfoList = Collections.synchronizedList(new ArrayList<>());
        String traceId = MDC.get(InfraConstant.TRACE_ID);
        Lists.partition(buildCodes, PAGE_SIZE).parallelStream().forEach(subList -> {
            try {
                MDC.put(InfraConstant.TRACE_ID, traceId);
                StoreInfoInputDto subStoreInfoInputDto = new StoreInfoInputDto();
                subStoreInfoInputDto.setBuildCodes(subList);
                List<StoreInfoOutDto> subStoreInfoList = queryVerifyStoreInfoBatch(subStoreInfoInputDto);
                if (CollectionUtils.isEmpty(subStoreInfoList)) {
                    log.error("根据storeIds或buildCodes查询单个楼盘详情返回 null! subStoreInfoInputDto={}", JSON.toJSONString(subStoreInfoInputDto));
                } else {
                    storeInfoList.addAll(subStoreInfoList);
                }
            } finally {
                MDC.remove(InfraConstant.TRACE_ID);
            }
        });
        Map<String, StoreInfoOutDto> map = storeInfoList.stream().collect(Collectors.toMap(StoreInfoOutDto::getBuildCode, Function.identity()));
        setBuildCodeStoreInfoMap(map);
    }

    private void validate() {
        Iterator<RowParseResult> iterator = getImportDataIterator();
        while (iterator.hasNext()) {
            RowParseResult bean = iterator.next();
            CouponVerifyImportRequest rowBean = (CouponVerifyImportRequest)bean.getRowBean();
            Integer rowNum = bean.getRowNum();
            try {
                String couponCode = AESPromotionUtil.encrypt(rowBean.getCouponCode());
                String buildCode = rowBean.getBuildCode();
                CouponEntity couponEntity = getDbCouponCodeMap().get(couponCode);
                Long couponThemeId = couponEntity.getCouponThemeId();
                CouponThemeCache couponThemeCache = getCouponThemeIdCacheMap().get(couponThemeId);

                JSONArray couponThemePubPorts = getCouponThemeApplicableUserTypes(couponThemeCache.getApplicableUserTypes());
                Integer userType = couponEntity.getUserType();

                StoreInfoOutDto verifyStoreInfo = getBuildCodeStoreInfoMap().get(buildCode);
                ValidateStoreInfoDto dto = new ValidateStoreInfoDto();
                BeanUtil.copyProperties(verifyStoreInfo, dto);
                dto.setCouponThemeId(couponThemeCache.getId());
                dto.setThemeType(couponThemeCache.getThemeType());
                dto.setCouponThemePubPorts(couponThemePubPorts);
                dto.setUserType(userType);
                // 校验入口
                ResponseErrorCode errorCode = validateStore4Single(dto);
                if (errorCode != null) {
                    CouponImportVerifyResultDto resultDto = prepareImportVerifyErrorCodeResultBean(CouponConstant.FAIL_MESSAGE, rowBean, errorCode);
                    resultDto.setRowNum(rowNum);
                    getVerifyContext().getVerifyResultMap().put(rowNum, resultDto);
                    iterator.remove();
                }
            } catch (Exception e) {
                log.error("批量核销异常：taskId={}, rowBean={}, message={}", getVerifyContext().getAsyncTaskId(), JSON.toJSON(rowBean), e.getMessage(), e);
                CouponImportVerifyResultDto resultDto = prepareImportVerifyErrorCodeResultBean(CouponConstant.FAIL_MESSAGE, rowBean, CommonErrorCode.SYSTEM_ERROR);
                resultDto.setRowNum(rowNum);
                getVerifyContext().getVerifyResultMap().put(rowNum, resultDto);
                iterator.remove();
            }
        }
    }
}
