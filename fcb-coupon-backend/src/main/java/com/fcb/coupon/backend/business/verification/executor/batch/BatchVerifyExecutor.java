package com.fcb.coupon.backend.business.verification.executor.batch;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fcb.coupon.backend.business.verification.context.BatchVerifyContext;
import com.fcb.coupon.backend.business.verification.context.VerifyServiceContext;
import com.fcb.coupon.backend.elasticsearch.CouponEsDoc;
import com.fcb.coupon.backend.exception.CouponVerificationErrorCode;
import com.fcb.coupon.backend.model.dto.CouponImportVerifyResultDto;
import com.fcb.coupon.backend.model.entity.CouponEntity;
import com.fcb.coupon.backend.model.param.request.CouponVerifyImportRequest;
import com.fcb.coupon.common.constant.CouponConstant;
import com.fcb.coupon.common.constant.InfraConstant;
import com.fcb.coupon.common.excel.bean.RowParseResult;
import com.fcb.coupon.common.util.AESPromotionUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.MDC;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 批量核销执行入口
 * @author YangHanBin
 * @date 2021-09-09 15:13
 */
public class BatchVerifyExecutor extends AbstractBatchVerifyExecutor {
    // 核销券主键
    private final List<Long> couponIds = new ArrayList<>();

    public BatchVerifyExecutor(BatchVerifyContext verifyContext, VerifyServiceContext serviceContext) {
        super(verifyContext, serviceContext);
    }

    private void setCouponCodeEsDocsMap() {
        // 遍历收集券码
        List<String> encryptCouponCodeList = getVerifyContext().getRowParseResults().stream().map(bean -> (CouponVerifyImportRequest) bean.getRowBean()).filter(bean -> StringUtils.isNotBlank(bean.getCouponCode())).map(item -> AESPromotionUtil.encrypt(item.getCouponCode())).distinct().collect(Collectors.toList());
        try {
            // 根据券码 查询elasticsearch获取coupon表主键
            setCouponCodeEsDocsMap(getCouponEsDocMapByCouponCodes(encryptCouponCodeList));
        } catch (Exception e) {
            log.error("BatchVerifyExecutor执行失败：taskId={}", getVerifyContext().getAsyncTaskId(), e);
            executeFail();
            throw e;
        }
    }

    private Map<String, List<CouponEsDoc>> getCouponEsDocMapByCouponCodes(List<String> couponCodeList) {
        List<CouponEsDoc> couponEsDocList = Collections.synchronizedList(new ArrayList<>(couponCodeList.size()));
        String traceId = MDC.get(InfraConstant.TRACE_ID);
        Lists.partition(couponCodeList, PAGE_SIZE).parallelStream().forEach(subList -> {
            try {
                MDC.put(InfraConstant.TRACE_ID, traceId);
                BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
                boolQueryBuilder.filter(QueryBuilders.termsQuery(CouponEsDoc.COUPON_CODE, subList));
                List<CouponEsDoc> subEsDocList = getCouponEsDocList(boolQueryBuilder);
                couponEsDocList.addAll(subEsDocList);
            } finally {
                MDC.remove(InfraConstant.TRACE_ID);
            }
        });
        return couponEsDocList.stream().collect(Collectors.groupingBy(CouponEsDoc::getCouponCode));
    }

    @Override
    protected void before() {
        setOriginalDataSize(getVerifyContext().getRowParseResults().size());
        setCouponCodeEsDocsMap();
    }

    @Override
    public void doExecute() {
        validate();
    }

    @Override
    public void after() {
        if (hasResult()) {
            return;
        }
        try {
            setDbCouponCodeMap();
        } catch (Exception e) {
            log.error("BatchVerifyExecutor执行失败：taskId={}", getVerifyContext().getAsyncTaskId(), e);
            executeFail();
            throw e;
        }
        CouponBatchVerifyExecutor delegate = new CouponBatchVerifyExecutor(getVerifyContext(), getServiceContext());
        delegate.setCouponCodeEsDocsMap(this.getCouponCodeEsDocsMap());
        delegate.setDbCouponCodeMap(this.getDbCouponCodeMap());
        delegate.execute();
    }

    private void validate() {
        Iterator<RowParseResult> importDataIterator = getImportDataIterator();
        while (importDataIterator.hasNext()) {
            RowParseResult bean = importDataIterator.next();
            CouponVerifyImportRequest rowBean = (CouponVerifyImportRequest)bean.getRowBean();
            Integer rowNum = bean.getRowNum();
            String couponCode = AESPromotionUtil.encrypt(rowBean.getCouponCode());
            String subscribeCode = rowBean.getSubscribeCode();
            String verifyPhone = rowBean.getVerifyPhone();
            String buildCode = rowBean.getBuildCode();

            List<CouponEsDoc> couponEsDocList = getCouponCodeEsDocsMap().get(couponCode);
            CouponVerificationErrorCode errorCode = null;
            if (StringUtils.isBlank(couponCode)) {
                errorCode = CouponVerificationErrorCode.COUPON_CODE_NULL;
            } else if (StringUtils.isBlank(subscribeCode)) {
                errorCode = CouponVerificationErrorCode.SUBSCRIBE_CODE_NULL;
            } else if (!subscribeCode.matches("[G|g][0-9]{7}")) {
                errorCode = CouponVerificationErrorCode.SUBSCRIBE_CODE_FORMAT_ERROR;
            }
            else if (StringUtils.isBlank(verifyPhone)) {
                errorCode = CouponVerificationErrorCode.VERIFY_PHONE_NULL;
            }
            else if (StringUtils.isBlank(buildCode)) {
                errorCode = CouponVerificationErrorCode.BUILD_CODE_NULL;
            }
            else if (CollectionUtils.isEmpty(couponEsDocList)) {
                String errorMessage = String.format(CouponVerificationErrorCode.COUPON_CODE_NOT_EXIST.getMessage(), rowBean.getCouponCode());
                errorCode = CouponVerificationErrorCode.COUPON_CODE_NOT_EXIST;
                errorCode.setMessage(errorMessage);
            }
            else if (couponEsDocList.size() != 1) {
                errorCode = CouponVerificationErrorCode.COUPON_CODE_NOT_UNIQUE;
            }
            if (errorCode != null) {
                CouponImportVerifyResultDto resultDto = prepareImportVerifyErrorCodeResultBean(CouponConstant.FAIL_MESSAGE, rowBean, errorCode);
                resultDto.setRowNum(rowNum);
                getVerifyContext().getVerifyResultMap().put(rowNum, resultDto);
                importDataIterator.remove();
                continue;
            }
            couponIds.add(couponEsDocList.iterator().next().getId());
        }
    }

    private void setDbCouponCodeMap() {
        List<CouponEntity> dbCoupons = Collections.synchronizedList(new ArrayList<>(couponIds.size()));
        String traceId = MDC.get(InfraConstant.TRACE_ID);
        Lists.partition(couponIds, PAGE_SIZE).parallelStream().forEach(subList -> {
            try {
                MDC.put(InfraConstant.TRACE_ID, traceId);
                LambdaQueryWrapper<CouponEntity> couponQueryWrapper = Wrappers.lambdaQuery(CouponEntity.class);
                couponQueryWrapper
                        .select(
                                CouponEntity::getId,
                                CouponEntity::getCouponThemeId,
                                CouponEntity::getCouponDiscountType,
                                CouponEntity::getCouponValue,
                                CouponEntity::getCouponCode,
                                CouponEntity::getUserId,
                                CouponEntity::getUserType,
                                CouponEntity::getStatus,
                                CouponEntity::getStartTime,
                                CouponEntity::getCreateTime,
                                CouponEntity::getEndTime,
                                CouponEntity::getVersionNo)
                        .in(CouponEntity::getId, subList);
                List<CouponEntity> subDbCoupons = getServiceContext().getCouponService().getBaseMapper().selectList(couponQueryWrapper);
                dbCoupons.addAll(subDbCoupons);
            } finally {
                MDC.remove(InfraConstant.TRACE_ID);
            }
        });

       setDbCouponCodeMap(dbCoupons.stream().collect(Collectors.toMap(CouponEntity::getCouponCode, Function.identity())));
    }
}
