package com.fcb.coupon.backend.business.verification.executor.single;

import com.fcb.coupon.backend.business.verification.context.SingleVerifyContext;
import com.fcb.coupon.backend.business.verification.context.VerifyServiceContext;

/**
 * 单个核销执行入口
 * @author YangHanBin
 * @date 2021-09-09 10:24
 */
public class SingleVerifyExecutor extends AbstractSingleVerifyExecutor {
    public SingleVerifyExecutor(SingleVerifyContext verifyContext, VerifyServiceContext serviceContext) {
        super(verifyContext, serviceContext);
    }

    @Override
    protected void doExecute() {
        CouponSingleVerifyExecutor delegate = new CouponSingleVerifyExecutor(getVerifyContext(), getServiceContext());
        delegate.execute();
    }
}
