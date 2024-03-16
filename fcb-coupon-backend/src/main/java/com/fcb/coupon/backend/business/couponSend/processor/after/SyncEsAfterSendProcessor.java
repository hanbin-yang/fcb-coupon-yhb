package com.fcb.coupon.backend.business.couponSend.processor.after;

import cn.hutool.core.bean.BeanUtil;
import com.fcb.coupon.backend.business.couponSend.AfterSendProcessor;
import com.fcb.coupon.backend.elasticsearch.CouponEsDoc;
import com.fcb.coupon.backend.model.dto.CouponSendContext;
import com.fcb.coupon.backend.model.entity.CouponThemeEntity;
import com.fcb.coupon.backend.service.CouponEsDocService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 唐陆军
 * @Description 同步ES
 * @createTime 2021年08月10日 17:47:00
 */
@Slf4j
@Component
public class SyncEsAfterSendProcessor implements AfterSendProcessor {

    @Autowired
    private CouponEsDocService couponEsDocService;


    @Override
    public void process(List<CouponSendContext> sendCouponContexts, CouponThemeEntity couponTheme) {
        List<CouponSendContext> successSendContexts = sendCouponContexts.stream().filter(m -> !m.getIsFailure()).collect(Collectors.toList());
        //同步es
        if (CollectionUtils.isEmpty(successSendContexts)) {
            return;
        }

        List<CouponEsDoc> esDocList = new ArrayList<>();
        successSendContexts.forEach(m -> {
            CouponEsDoc doc = new CouponEsDoc();
            BeanUtil.copyProperties(m.getCouponEntity(), doc);
            doc.setBindTel(m.getCouponUserEntity().getBindTel());
            doc.setBindTime(m.getCouponUserEntity().getCreateTime());
            esDocList.add(doc);
        });

//        if (CouponTypeEnum.COUPON_TYPE_THIRD.getType().equals(couponTheme.getCouponType())) {
//            couponEsDocService.updateBatch(esDocList);
//        } else {
//            couponEsDocService.saveBatch(esDocList);
//        }

        couponEsDocService.saveBatch(esDocList);
    }
}
