package com.fcb.coupon.backend.business.couponSend.handler;

import com.alibaba.fastjson.JSON;
import com.fcb.coupon.backend.business.couponSend.CouponSendHandler;
import com.fcb.coupon.backend.business.couponSend.CouponSendPostProcessor;
import com.fcb.coupon.backend.business.couponSend.CouponSendStrategy;
import com.fcb.coupon.backend.exception.CouponThemeErrorCode;
import com.fcb.coupon.backend.model.bo.CouponBatchSendBo;
import com.fcb.coupon.backend.model.bo.CouponSendUserBo;
import com.fcb.coupon.backend.model.dto.CouponSendContext;
import com.fcb.coupon.backend.model.dto.CouponSendResult;
import com.fcb.coupon.backend.model.dto.CouponThemeCrowdScopeIdDto;
import com.fcb.coupon.backend.model.entity.CouponThemeEntity;
import com.fcb.coupon.backend.model.entity.CouponThemeStatisticEntity;
import com.fcb.coupon.backend.service.CouponThemeStatisticService;
import com.fcb.coupon.common.constant.InfraConstant;
import com.fcb.coupon.common.enums.AsyncStatusEnum;
import com.fcb.coupon.common.enums.CouponThemeStatus;
import com.fcb.coupon.common.exception.BusinessException;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 121272100
 */
@Slf4j
@Component
public abstract class AbstractCouponSendHandler implements CouponSendHandler {

    @Autowired
    private List<CouponSendStrategy> couponSendStrategyList;
    @Autowired
    private List<CouponSendPostProcessor> couponSendPostProcessors;
    @Autowired
    private CouponThemeStatisticService couponThemeStatisticService;

    protected CouponSendStrategy getSendStrategy(CouponThemeEntity couponTheme) {
        for (CouponSendStrategy couponSendStrategy : couponSendStrategyList) {
            if (couponSendStrategy.supports(couponTheme.getCouponType())) {
                return couponSendStrategy;
            }
        }
        throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_COUPON_TYPE_NOT_SUPPORT);
    }

    protected CouponSendPostProcessor getPostProcessor(Integer source) {
        for (CouponSendPostProcessor couponSendPostProcessor : couponSendPostProcessors) {
            if (couponSendPostProcessor.supports(source)) {
                return couponSendPostProcessor;
            }
        }
        throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_COUPON_SOURCE_NOT_SUPPORT);
    }


    @Override
    public void validate(CouponBatchSendBo bo, CouponThemeEntity couponTheme) {
        //校验活动状态
        validateCouponTheme(couponTheme);
        //验证发送类型
        validateSendType(bo, couponTheme);
        //验证券总库存
        validateSendCount(bo, couponTheme);
    }

    @Override
    public CouponSendResult batchSend(CouponBatchSendBo bo, CouponThemeEntity couponTheme) {
        if (CollectionUtils.isEmpty(bo.getSendUserList())) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_SEND_USER_EMPTY);
        }
        List<CouponSendContext> sendContexts = buildSendCouponContext(bo, couponTheme);
        return parallelBatchSend(sendContexts, bo.getSource(), couponTheme);
    }

    private CouponSendResult parallelBatchSend(List<CouponSendContext> sendContexts, Integer source, CouponThemeEntity couponTheme) {
        Integer totalCount = sendContexts.size();
        //开始分批发券
        List<List<CouponSendContext>> partitionSendContexts = Lists.partition(sendContexts, CouponSendHandler.BATCH_SIZE);
        //并行发送优惠券
        String traceId = MDC.get(InfraConstant.TRACE_ID);
        long startTime = System.currentTimeMillis();
        try {
            partitionSendContexts.parallelStream().forEach((partSendContexts) -> {
                MDC.put(InfraConstant.TRACE_ID, traceId);
                try {
                    //先填充用户信息
                    populateSendContext(partSendContexts);
                    //发送优惠券
                    doBatchSend(source, partSendContexts, couponTheme);
                    long failCount = partSendContexts.stream().filter(m -> m.getIsFailure()).count();
                    log.info("发券子任务完成,成功数量={},失败数量={}", partSendContexts.size() - failCount, failCount);
                } catch (BusinessException ex) {
                    log.error("发券子任务异常,失败数量=" + partSendContexts.size(), ex);
                    partSendContexts.forEach(m -> {
                        m.error(false, ex.getMessage());
                    });
                } catch (Exception ex) {
                    log.error("发券子任务异常,失败数量=" + partSendContexts.size(), ex);
                    partSendContexts.forEach(m -> {
                        m.error(true, "发券异常");
                    });
                } finally {
                    MDC.remove(InfraConstant.TRACE_ID);
                }
            });
            long endTime = System.currentTimeMillis();
            int totalFailCount = (int) sendContexts.stream().filter(m -> m.getIsFailure()).count();
            int totalSuccessCount = totalCount - totalFailCount;
            log.info("发券完成，发券数量={},成功数量={},失败数量={},发券耗时={}ms", totalCount, totalSuccessCount, totalFailCount, endTime - startTime);
            return CouponSendResult.builder()
                    .status(AsyncStatusEnum.FINISHED)
                    .totalCount(totalCount)
                    .successCount(totalSuccessCount)
                    .errorCount(totalFailCount)
                    .sendContexts(sendContexts)
                    .build();
        } catch (Exception ex) {
            log.error("发券异常", ex);
            return CouponSendResult.builder()
                    .status(AsyncStatusEnum.FAIL)
                    .totalCount(totalCount)
                    .successCount(0)
                    .errorCount(totalCount)
                    .sendContexts(sendContexts)
                    .build();
        }
    }

    /*
    填充发送上下文
     */
    protected abstract void populateSendContext(List<CouponSendContext> sendCouponContexts);

    private void doBatchSend(Integer source, List<CouponSendContext> sendCouponContexts, CouponThemeEntity couponTheme) {
        //获取返回前、后处理钩子
        CouponSendPostProcessor postProcessor = getPostProcessor(source);

        //过滤能发券的
        List<CouponSendContext> canSendCouponContexts = sendCouponContexts.stream().filter(m -> !m.getIsFailure()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(canSendCouponContexts)) {
            return;
        }

        //发券前置处理
        postProcessor.postProcessBeforeSend(sendCouponContexts, couponTheme);

        //再次过滤能发券的
        canSendCouponContexts = sendCouponContexts.stream().filter(m -> !m.getIsFailure()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(canSendCouponContexts)) {
            return;
        }

        //发券
        CouponSendStrategy couponSendStrategy = getSendStrategy(couponTheme);
        couponSendStrategy.batchSend(canSendCouponContexts, couponTheme);

        //发券后置处理
        postProcessor.postProcessAfterSend(sendCouponContexts, couponTheme);
    }


    private List<CouponSendContext> buildSendCouponContext(CouponBatchSendBo bo, CouponThemeEntity couponTheme) {
        List<CouponSendContext> couponSendContexts = new ArrayList<>();
        for (CouponSendUserBo sendUserBo : bo.getSendUserList()) {
            for (int count = 0; count < sendUserBo.getCount(); count++) {
                CouponSendContext.CouponSendContextBuilder builder = CouponSendContext.builder();
                builder.couponThemeId(couponTheme.getId())
                        .userType(bo.getSendUserType())
                        .source(bo.getSource())
                        .sourceId(bo.getSourceId())
                        .transactionId(sendUserBo.getTransactionId())
                        .isFailure(false)
                        .canRetry(false)
                        .userId(sendUserBo.getUserId())
                        .unionId(sendUserBo.getUnionId())
                        .bindTel(sendUserBo.getPhone());
                couponSendContexts.add(builder.build());
            }
        }
        return couponSendContexts;
    }


    protected void validateCouponTheme(CouponThemeEntity couponTheme) {
        if (!CouponThemeStatus.EFFECTIVE.getStatus().equals(couponTheme.getStatus())) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_NOT_EFFECTIVE);
        }
        Date nowTime = new Date();
        if (!(couponTheme.getStartTime().before(nowTime) && nowTime.before(couponTheme.getEndTime()))) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_NOT_IN_START_END_TIME);
        }
    }

    protected void validateSendType(CouponBatchSendBo bo, CouponThemeEntity couponTheme) {
        Set<Integer> crowdScopeIdSet = getCrowdScopeIds(couponTheme.getApplicableUserTypes());
        if (CollectionUtils.isEmpty(crowdScopeIdSet)) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_NOT_CONFIG_SEND_USER);
        }
        if (!crowdScopeIdSet.contains(bo.getSendUserType())) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_SEND_USER_NOT_MATCH);
        }
    }

    protected void validateSendCount(CouponBatchSendBo bo, CouponThemeEntity couponTheme) {
        //验证总数
        int totalSendCount = bo.getSendUserList().stream().mapToInt(m -> m.getCount()).sum();
        //验证总库存
        CouponThemeStatisticEntity statisticEntity = couponThemeStatisticService.getById(couponTheme.getId());
        if (statisticEntity == null) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_CAN_SEND_COUNT_ERROR);
        }
        int canSendCount = statisticEntity.getCreatedCount() - statisticEntity.getSendedCount();
        if (totalSendCount > canSendCount) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_CAN_SEND_COUNT_ERROR);
        }
    }

    protected Set<Integer> getCrowdScopeIds(String crowdScopeIdStr) {
        if (StringUtils.isBlank(crowdScopeIdStr)) {
            return Collections.emptySet();
        }
        CouponThemeCrowdScopeIdDto dto = JSON.parseObject(crowdScopeIdStr, CouponThemeCrowdScopeIdDto.class);
        return new HashSet<>(dto.getIds());
    }

}
