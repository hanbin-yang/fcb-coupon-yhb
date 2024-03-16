package com.fcb.coupon.backend.business.verification.impl;

import com.fcb.coupon.backend.business.verification.CouponVerificationBusiness;
import com.fcb.coupon.backend.business.verification.context.BatchVerifyContext;
import com.fcb.coupon.backend.business.verification.context.SingleVerifyContext;
import com.fcb.coupon.backend.business.verification.context.VerifyServiceContext;
import com.fcb.coupon.backend.business.verification.executor.VerifyExecutor;
import com.fcb.coupon.backend.business.verification.executor.batch.BatchVerifyExecutor;
import com.fcb.coupon.backend.business.verification.executor.single.SingleVerifyExecutor;
import com.fcb.coupon.backend.model.bo.BatchVerifyBo;
import com.fcb.coupon.backend.model.bo.SingleVerifyBo;
import com.fcb.coupon.common.excel.bean.RowParseResult;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

/**
 * @author YangHanBin
 * @date 2021-09-09 9:52
 */
@Service
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@Slf4j
public class CouponVerificationBusinessImpl implements CouponVerificationBusiness {
    private final VerifyServiceContext serviceContext;

    @Override
    public void singleVerify(SingleVerifyBo bo) {
        SingleVerifyContext verifyContext = buildSingleVerifyContext(bo);
        VerifyExecutor executor = new SingleVerifyExecutor(verifyContext, serviceContext);
        executor.execute();
    }

    @Override
    public void batchVerify(BatchVerifyBo bo) {
        BatchVerifyContext verifyContext = buildBatchVerifyContext(bo);
        VerifyExecutor executor = new BatchVerifyExecutor(verifyContext, serviceContext);
        executor.execute();
    }

    private SingleVerifyContext buildSingleVerifyContext(SingleVerifyBo bo) {
        SingleVerifyContext context = new SingleVerifyContext();
        context.setUsedStoreId(bo.getUsedStoreId());
        context.setSubscribeCode(bo.getSubscribeCode());
        context.setBindTel(bo.getBindTel());
        context.setCouponCode(bo.getCouponCode());
        context.setVerifyUserId(bo.getVerifyUserId());
        context.setVerifyUsername(bo.getVerifyUsername());
        return context;
    }

    private BatchVerifyContext buildBatchVerifyContext(BatchVerifyBo bo) {
        Map<Integer, RowParseResult> importDataMap = bo.getImportDataMap();
        Collection<RowParseResult> rowParseResults = importDataMap.values();
        BatchVerifyContext verifyContext = new BatchVerifyContext();
        verifyContext.setVerifyUserId(bo.getUserId());
        verifyContext.setVerifyUsername(bo.getUsername());
        verifyContext.setAsyncTaskId(bo.getAsyncTaskId());
        verifyContext.setRowParseResults(rowParseResults);
        verifyContext.setVerifyResultMap(Maps.newTreeMap());
        return verifyContext;
    }
}
