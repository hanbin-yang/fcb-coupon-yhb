package com.fcb.coupon.backend.business.verification.executor.single;

import com.fcb.coupon.backend.business.verification.context.SingleVerifyContext;
import com.fcb.coupon.backend.business.verification.context.VerifyServiceContext;
import com.fcb.coupon.backend.business.verification.executor.AbstractVerifyExecutor;
import com.fcb.coupon.backend.elasticsearch.CouponEsDoc;
import com.fcb.coupon.backend.model.cache.CouponThemeCache;
import com.fcb.coupon.backend.model.dto.StoreInfoOutDto;
import com.fcb.coupon.backend.model.entity.CouponEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * 单个核销执行器
 * @author YangHanBin
 * @date 2021-09-09 11:31
 */
public abstract class AbstractSingleVerifyExecutor extends AbstractVerifyExecutor {
    @Getter
    private final SingleVerifyContext verifyContext;

    // 线下预制券标志
    @Setter
    private Boolean offLineFlag;
    public Boolean getOffLineFlag() {
        if (Objects.isNull(offLineFlag)) {
            throw new UnsupportedOperationException("请先执行UserInfoSingleVerifyExecutor");
        }
        return offLineFlag;
    }

    @Setter
    @Getter
    private String verifyUnionId;

    @Setter
    private CouponEsDoc couponEsDoc;
    public CouponEsDoc getCouponEsDoc() {
        if (Objects.isNull(couponEsDoc)) {
            throw new UnsupportedOperationException("请先执行CouponSingleVerifyExecutor");
        }
        return couponEsDoc;
    }

    @Setter
    private CouponEntity dbCoupon;
    public CouponEntity getDbCoupon() {
        if (Objects.isNull(dbCoupon)) {
            throw new UnsupportedOperationException("请先执行CouponSingleVerifyExecutor");
        }
        return dbCoupon;
    }

    @Setter
    private CouponThemeCache couponThemeCache;
    public CouponThemeCache getCouponThemeCache() {
        if (Objects.isNull(couponThemeCache)) {
            throw new UnsupportedOperationException("请先执行CouponSingleVerifyExecutor");
        }
        return couponThemeCache;
    }

    @Setter
    private StoreInfoOutDto verifyStoreInfo;
    public StoreInfoOutDto getVerifyStoreInfo() {
        if (Objects.isNull(verifyStoreInfo)) {
            throw new UnsupportedOperationException("请先执行StoreInfoSingleVerifyExecutor");
        }
        return verifyStoreInfo;
    }

    public AbstractSingleVerifyExecutor(SingleVerifyContext verifyContext, VerifyServiceContext serviceContext) {
        super(serviceContext, verifyContext.getVerifyUserId(), verifyContext.getVerifyUsername());
        this.verifyContext = verifyContext;
    }

    protected void before() {

    }
    @Override
    public void execute() {
        before();
        doExecute();
        after();
    }
    protected abstract void doExecute();
    protected void after() {
    }
}
