package com.fcb.coupon.backend.business.verification.executor.single;

import com.alibaba.fastjson.JSON;
import com.fcb.coupon.backend.business.verification.context.SingleVerifyContext;
import com.fcb.coupon.backend.business.verification.context.VerifyServiceContext;
import com.fcb.coupon.backend.elasticsearch.CouponEsDoc;
import com.fcb.coupon.backend.exception.CouponVerificationErrorCode;
import com.fcb.coupon.backend.model.cache.CouponThemeCache;
import com.fcb.coupon.backend.model.entity.CouponEntity;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.exception.ResponseErrorCode;
import com.fcb.coupon.common.util.AESPromotionUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.List;
import java.util.Objects;

/**
 * 券校验相关执行器
 * @author YangHanBin
 * @date 2021-09-09 15:52
 */
public class CouponSingleVerifyExecutor extends AbstractSingleVerifyExecutor {

    public CouponSingleVerifyExecutor(SingleVerifyContext verifyContext, VerifyServiceContext serviceContext) {
        super(verifyContext, serviceContext);
    }

    @Override
    protected void before() {
        // es查询 根据券码找到couponId
        String couponCode = getVerifyContext().getCouponCode();
        setCouponEsDoc(couponCode);
        setDbCoupon(couponCode);
    }

    @Override
    protected void doExecute() {
        validate();
    }

    @Override
    protected void after() {
        setCouponThemeCache();
        UserInfoSingleVerifyExecutor delegate = new UserInfoSingleVerifyExecutor(getVerifyContext(), getServiceContext());
        delegate.setCouponEsDoc(getCouponEsDoc());
        delegate.setDbCoupon(getDbCoupon());
        delegate.setCouponThemeCache(getCouponThemeCache());
        delegate.execute();
    }

    private void setDbCoupon(String couponCode) {
        Long couponId = getCouponEsDoc().getId();
        // 查coupon表 进一步获取真实数据
        CouponEntity dbCoupon = getServiceContext().getCouponService().getById(couponId);
        if (Objects.isNull(dbCoupon)) {
            CouponVerificationErrorCode errorCode = CouponVerificationErrorCode.COUPON_CODE_NOT_EXIST;
            String format = String.format(errorCode.getMessage(), couponCode);
            errorCode.setMessage(format);
            throw new BusinessException(errorCode);
        }

        setDbCoupon(dbCoupon);
    }

    private void setCouponEsDoc(String couponCode) {
        CouponEsDoc couponEsDoc = getCouponEsDocByCouponCode(couponCode);
        if (Objects.isNull(couponEsDoc)) {
            CouponVerificationErrorCode errorCode = CouponVerificationErrorCode.COUPON_CODE_NOT_EXIST;
            String format = String.format(errorCode.getMessage(), couponCode);
            errorCode.setMessage(format);
            throw new BusinessException(errorCode);
        }
        setCouponEsDoc(couponEsDoc);
    }

    private void setCouponThemeCache() {
        Long couponThemeId = getCouponEsDoc().getCouponThemeId();
        CouponThemeCache couponThemeCache = getServiceContext().getCouponThemeCacheService().getById(couponThemeId);
        if (Objects.isNull(couponThemeCache)) {
            throw new BusinessException(CouponVerificationErrorCode.COUPON_THEME_NOT_EXIST);
        }
        setCouponThemeCache(couponThemeCache);
    }

    private void validate() {
        ResponseErrorCode errorCode = validateCouponDb4Single(getDbCoupon());
        if (errorCode != null) {
            throw new BusinessException(errorCode);
        }
    }

    private CouponEsDoc getCouponEsDocByCouponCode(String couponCode) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(QueryBuilders.termQuery(CouponEsDoc.COUPON_CODE, Objects.requireNonNull(AESPromotionUtil.encrypt(couponCode))));

        List<CouponEsDoc> couponEsDocList = getCouponEsDocList(boolQueryBuilder);

        if (CollectionUtils.isEmpty(couponEsDocList)) {
            throw new BusinessException(CouponVerificationErrorCode.COUPON_CODE_NOT_EXIST.getCode(), String.format(CouponVerificationErrorCode.COUPON_CODE_NOT_EXIST.getMessage(), couponCode));
        }
        if (couponEsDocList.size() != 1) {
            log.error("券码不唯一，查询elasticsearch存在{}条数据, couponCode={}, couponEsDocList={}", couponEsDocList.size(), couponCode, JSON.toJSON(couponEsDocList));
            throw new BusinessException(CouponVerificationErrorCode.COUPON_CODE_NOT_UNIQUE);
        }
        return couponEsDocList.get(0);
    }
}
