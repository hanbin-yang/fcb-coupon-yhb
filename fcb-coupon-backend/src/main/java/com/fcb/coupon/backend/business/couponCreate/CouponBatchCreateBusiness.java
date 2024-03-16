package com.fcb.coupon.backend.business.couponCreate;

import com.fcb.coupon.backend.model.bo.CouponImportBo;
import com.fcb.coupon.backend.model.bo.GenerateCouponBo;

public interface CouponBatchCreateBusiness {

    /*
     * @description 批量导入第三方券
     * @author 唐陆军

     * @param: bo
     * @date 2021-6-18 16:32
     */
    Long batchImportThird(CouponImportBo bo);

    /*
    批量生成优惠券
     */
    boolean batchGenerateCoupon(GenerateCouponBo bo);
}
