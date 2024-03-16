package com.fcb.coupon.backend.business.verification.executor.batch;

import com.fcb.coupon.backend.business.verification.context.BatchVerifyContext;
import com.fcb.coupon.backend.business.verification.context.VerifyServiceContext;
import com.fcb.coupon.backend.model.cache.CouponThemeCache;
import com.fcb.coupon.backend.model.dto.CouponImportVerifyResultDto;
import com.fcb.coupon.backend.model.entity.CouponEntity;
import com.fcb.coupon.backend.model.param.request.CouponVerifyImportRequest;
import com.fcb.coupon.common.constant.CouponConstant;
import com.fcb.coupon.common.excel.bean.RowParseResult;
import com.fcb.coupon.common.exception.ResponseErrorCode;
import com.fcb.coupon.common.util.AESPromotionUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 券校验相关执行器
 * @author YangHanBin
 * @date 2021-09-09 16:05
 */
public class CouponBatchVerifyExecutor extends AbstractBatchVerifyExecutor {
    public CouponBatchVerifyExecutor(BatchVerifyContext verifyContext, VerifyServiceContext serviceContext) {
        super(verifyContext, serviceContext);
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
        setCouponThemeIdCacheMap();
        UserInfoBatchVerifyExecutor delegate = new UserInfoBatchVerifyExecutor(getVerifyContext(), getServiceContext());
        delegate.setCouponCodeEsDocsMap(getCouponCodeEsDocsMap());
        delegate.setDbCouponCodeMap(getDbCouponCodeMap());
        delegate.setCouponThemeIdCacheMap(getCouponThemeIdCacheMap());
        delegate.execute();
    }

    private void setCouponThemeIdCacheMap() {
        try {
            Set<Long> couponThemeIds = getDbCouponCodeMap().values().stream().map(CouponEntity::getCouponThemeId).collect(Collectors.toSet());

            Map<Long, CouponThemeCache> map = new HashMap<>();
            for (Long id : couponThemeIds) {
                // 查询单个redis couponTheme缓存
                CouponThemeCache couponThemeCache = getServiceContext().getCouponThemeCacheService().getById(id);
                map.put(id, couponThemeCache);
            }
            setCouponThemeIdCacheMap(map);
        } catch (Exception e) {
            log.error("CouponThemeBatchVerifyExecutor执行失败：taskId={}", getVerifyContext().getAsyncTaskId(), e);
            executeFail();
            throw e;
        }
    }

    private void validate() {
        Iterator<RowParseResult> importDataIterator = getImportDataIterator();
        while (importDataIterator.hasNext()) {
            RowParseResult bean = importDataIterator.next();
            CouponVerifyImportRequest rowBean = (CouponVerifyImportRequest)bean.getRowBean();
            Integer rowNum = bean.getRowNum();
            String couponCode = AESPromotionUtil.encrypt(rowBean.getCouponCode());
            CouponEntity dbCoupon = getDbCouponCodeMap().get(couponCode);

            ResponseErrorCode errorCode = validateCouponDb4Single(dbCoupon);
            if (errorCode != null) {
                CouponImportVerifyResultDto resultDto = prepareImportVerifyErrorCodeResultBean(CouponConstant.FAIL_MESSAGE, rowBean, errorCode);
                resultDto.setRowNum(rowNum);
                getVerifyContext().getVerifyResultMap().put(rowNum, resultDto);
                importDataIterator.remove();
            }
        }
    }
}
