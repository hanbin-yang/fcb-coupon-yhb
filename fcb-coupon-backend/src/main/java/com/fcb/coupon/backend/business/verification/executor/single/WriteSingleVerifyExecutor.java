package com.fcb.coupon.backend.business.verification.executor.single;

import com.fcb.coupon.backend.business.verification.context.SingleVerifyContext;
import com.fcb.coupon.backend.business.verification.context.VerifyServiceContext;
import com.fcb.coupon.backend.elasticsearch.CouponEsDoc;
import com.fcb.coupon.backend.listener.event.MinCouponEvent;
import com.fcb.coupon.backend.model.bo.CouponSingleVerifyBo;
import com.fcb.coupon.backend.model.dto.CouponGrowingDto;
import com.fcb.coupon.backend.model.dto.OprLogDo;
import com.fcb.coupon.common.constant.InfraConstant;
import com.fcb.coupon.common.enums.CouponStatusEnum;
import com.fcb.coupon.common.enums.LogOprThemeType;
import com.fcb.coupon.common.enums.LogOprType;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.MDC;

import java.util.Collections;
import java.util.List;

/**
 * 正式写入核销执行器
 * @author YangHanBin
 * @date 2021-09-07 17:15
 */
public class WriteSingleVerifyExecutor extends AbstractSingleVerifyExecutor {
    @Getter
    @Setter
    private CouponSingleVerifyBo couponSingleVerifyBo;


    public WriteSingleVerifyExecutor(SingleVerifyContext verifyContext, VerifyServiceContext serviceContext) {
        super(verifyContext, serviceContext);
    }

    @Override
    protected void doExecute() {
        CouponSingleVerifyBo bo = prepareSingleVerifyBoBean(getVerifyContext().getBindTel(), getVerifyContext().getSubscribeCode(), getDbCoupon(), getVerifyStoreInfo(), getCouponThemeCache());
        // 正式核销
        doSingleVerify(bo);
        setCouponSingleVerifyBo(bo);
    }

    @Override
    protected void after() {
        CouponGrowingDto couponGrowingDto = prepareCouponGrowingDtoBean(getCouponSingleVerifyBo());
        couponGrowingDto.setUnionId(getVerifyUnionId());
        couponGrowingDto.setUserId(getDbCoupon().getUserId());

        String traceId = MDC.get(InfraConstant.TRACE_ID);
        // 异步
        getServiceContext().getCouponVerificationExecutor().execute(() -> {
            try {
                MDC.put(InfraConstant.TRACE_ID, traceId);
                // 同步es
                CouponEsDoc updateDoc = new CouponEsDoc();
                updateDoc.setId(getDbCoupon().getId());
                updateDoc.setStatus(CouponStatusEnum.STATUS_USED.getStatus());
                updateDoc.setBindTel(getVerifyContext().getBindTel());
                List<CouponEsDoc> updateEsDocList = Collections.singletonList(updateDoc);
                getServiceContext().getCouponEsDocService().updateBatch(updateEsDocList);
                //埋点
                sendGrowingMessage(Collections.singletonList(couponGrowingDto));
                // 保底券
                getServiceContext().getPublisher().publishEvent(new MinCouponEvent(Collections.singletonList(getVerifyContext().getBindTel())));

                // 记录操作日志
                OprLogDo oprLogDo = OprLogDo.builder()
                        .oprUserId(getVerifyContext().getVerifyUserId())
                        .oprUserName(getVerifyContext().getVerifyUsername())
                        .oprContent("后台核销")
                        .refId(getDbCoupon().getId())
                        .oprThemeType(LogOprThemeType.COUPON)
                        .oprType(LogOprType.VERIFICATION)
                        .build();
                getServiceContext().getCouponOprLogService().saveOprLog(oprLogDo);
            } finally {
                MDC.remove(InfraConstant.TRACE_ID);
            }
        });
    }
}
