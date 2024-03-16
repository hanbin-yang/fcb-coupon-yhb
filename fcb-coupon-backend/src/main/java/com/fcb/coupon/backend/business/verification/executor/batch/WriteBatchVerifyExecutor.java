package com.fcb.coupon.backend.business.verification.executor.batch;

import com.alibaba.fastjson.JSON;
import com.fcb.coupon.backend.business.verification.context.BatchVerifyContext;
import com.fcb.coupon.backend.business.verification.context.VerifyServiceContext;
import com.fcb.coupon.backend.elasticsearch.CouponEsDoc;
import com.fcb.coupon.backend.exception.CouponVerificationErrorCode;
import com.fcb.coupon.backend.listener.event.MinCouponEvent;
import com.fcb.coupon.backend.model.bo.CouponSingleVerifyBo;
import com.fcb.coupon.backend.model.cache.CouponThemeCache;
import com.fcb.coupon.backend.model.dto.*;
import com.fcb.coupon.backend.model.entity.CouponEntity;
import com.fcb.coupon.backend.model.param.request.CouponVerifyImportRequest;
import com.fcb.coupon.common.constant.CouponConstant;
import com.fcb.coupon.common.constant.InfraConstant;
import com.fcb.coupon.common.constant.RedisLockKeyConstant;
import com.fcb.coupon.common.dto.RedisLockResult;
import com.fcb.coupon.common.enums.*;
import com.fcb.coupon.common.excel.bean.RowParseResult;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.exception.CommonErrorCode;
import com.fcb.coupon.common.util.AESPromotionUtil;
import com.fcb.coupon.common.util.RedisUtil;
import com.google.common.collect.Lists;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 正式写入核销执行器
 * @author YangHanBin
 * @date 2021-09-09 14:37
 */
public class WriteBatchVerifyExecutor extends AbstractBatchVerifyExecutor {
    public WriteBatchVerifyExecutor(BatchVerifyContext verifyContext, VerifyServiceContext serviceContext) {
        super(verifyContext, serviceContext);
    }

    @Override
    protected void doExecute() {
        operateDatabase();
    }

    @Override
    protected void after() {
        exportVerifyResult(getVerifyContext().getAsyncTaskId(), getVerifyContext().getVerifyResultMap());
    }

    private void operateDatabase() {
        List<CouponEsDoc> updateDocList = new ArrayList<>();
        List<OprLogDo> oprLogDoList = new ArrayList<>();
        List<String> minPhoneList = new ArrayList<>();
        List<CouponGrowingDto> dtoList = new ArrayList<>();

        Iterator<RowParseResult> importDataIterator = getImportDataIterator();
        while (importDataIterator.hasNext()) {
            RowParseResult bean = importDataIterator.next();
            CouponVerifyImportRequest rowBean = (CouponVerifyImportRequest)bean.getRowBean();
            Integer rowNum = bean.getRowNum();
            String couponCode = AESPromotionUtil.encrypt(rowBean.getCouponCode());
            String buildCode = rowBean.getBuildCode();
            String verifyPhone = rowBean.getVerifyPhone();
            String subscribeCode = rowBean.getSubscribeCode();
            CouponEntity couponEntity = getDbCouponCodeMap().get(couponCode);
            StoreInfoOutDto verifyStoreInfo = getBuildCodeStoreInfoMap().get(buildCode);

            CouponThemeCache couponThemeCache = getCouponThemeIdCacheMap().get(couponEntity.getCouponThemeId());

            CouponSingleVerifyBo couponSingleVerifyBo = prepareSingleVerifyBoBean(verifyPhone, subscribeCode, couponEntity, verifyStoreInfo, couponThemeCache);
            try {
                String keyName = String.format("%s%s", RedisLockKeyConstant.SINGLE_COUPON_VERIFICATION, rowBean.getCouponCode());
                // 正式单个核销
                RedisLockResult<Void> redisLockResult = RedisUtil.executeTryLock(keyName, YesNoEnum.NO.getValue(), () -> doSingleVerify(couponSingleVerifyBo));
                //获取不到锁
                if(redisLockResult.isFailure()) {
                    throw new BusinessException(CouponVerificationErrorCode.REPEAT_VERIFY);
                }
                CouponImportVerifyResultDto successResultBean = prepareImportVerifyErrorCodeResultBean(CouponConstant.SUCCESS_MESSAGE, rowBean, null);
                getVerifyContext().getVerifyResultMap().put(rowNum, successResultBean);
                importDataIterator.remove();
            } catch (Exception e) {
                String errorMessage;
                if (e instanceof BusinessException) {
                    errorMessage = e.getMessage();
                    log.error("批量核销业务异常：taskId={}, rowBean={}, code={}, message={}",getVerifyContext().getAsyncTaskId(), JSON.toJSON(rowBean), ((BusinessException) e).getCode(), e.getMessage());
                } else {
                    log.error("批量核销异常：taskId={}, rowBean={}, message={}", getVerifyContext().getAsyncTaskId(), JSON.toJSON(rowBean), e.getMessage(), e);
                    errorMessage = CommonErrorCode.SYSTEM_ERROR.getMessage();
                }

                CouponImportVerifyResultDto resultDto = prepareImportVerifyResultBean(CouponConstant.FAIL_MESSAGE, rowBean, errorMessage);
                resultDto.setRowNum(rowNum);
                getVerifyContext().getVerifyResultMap().put(rowNum, resultDto);
                importDataIterator.remove();
                continue;
            }

            // 成功的 展示信息
            CouponImportVerifyResultDto resultDto = prepareImportVerifyResultBean(CouponConstant.SUCCESS_MESSAGE, rowBean, null);
            resultDto.setRowNum(rowNum);
            getVerifyContext().getVerifyResultMap().put(rowNum, resultDto);

            // 更新es用
            CouponEsDoc updateDoc = new CouponEsDoc();
            updateDoc.setId(couponEntity.getId());
            updateDoc.setStatus(CouponStatusEnum.STATUS_USED.getStatus());
            updateDoc.setBindTel(verifyPhone);
            updateDocList.add(updateDoc);

            // 收集手机号 发保底券用
            if (!CouponGiveRuleEnum.COUPON_GIVE_RULE_OFFLINE_PREFABRICATED.ifSame(couponThemeCache.getCouponGiveRule())) {
                minPhoneList.add(verifyPhone);
            }

            // 写操作日志用
            OprLogDo oprLogDo = OprLogDo.builder()
                    .oprUserId(getVerifyContext().getVerifyUserId())
                    .oprUserName(getVerifyContext().getVerifyUsername())
                    .oprContent("后台导入核销")
                    .refId(couponEntity.getId())
                    .oprThemeType(LogOprThemeType.COUPON)
                    .oprType(LogOprType.VERIFICATION)
                    .build();
            oprLogDoList.add(oprLogDo);

            //收集埋点信息
            Map<String, VerifyUserInfoDto> stringVerifyUserInfoDtoMap = getUserTypeInfoMap().get(couponSingleVerifyBo.getUserType());
            CouponGrowingDto dto = prepareCouponGrowingDtoBean(couponSingleVerifyBo);
            dto.setUnionId(stringVerifyUserInfoDtoMap.get(verifyPhone).getVerifyUnionId());
            dto.setUserId(stringVerifyUserInfoDtoMap.get(verifyPhone).getVerifyUserId());
            dtoList.add(dto);
        }

        String traceId = MDC.get(InfraConstant.TRACE_ID);
        getServiceContext().getCouponVerificationExecutor().execute(() -> {
            try {
                MDC.put(InfraConstant.TRACE_ID, traceId);
                // 同步es
                Lists.partition(updateDocList, PAGE_SIZE).forEach(subList -> getServiceContext().getCouponEsDocService().updateBatch(subList));
                // 埋点
                this.sendGrowingMessage(dtoList);
                // 保底券
                Lists.partition(minPhoneList, PAGE_SIZE).forEach(subList -> getServiceContext().getPublisher().publishEvent(new MinCouponEvent(subList)));
                //记录操作日志
                Lists.partition(oprLogDoList, PAGE_SIZE).forEach(getServiceContext().getCouponOprLogService()::saveOprLogBatch);
            } finally {
                MDC.remove(InfraConstant.TRACE_ID);
            }
        });
    }
}
