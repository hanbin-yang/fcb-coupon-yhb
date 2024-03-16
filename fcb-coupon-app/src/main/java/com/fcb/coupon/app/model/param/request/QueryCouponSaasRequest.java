package com.fcb.coupon.app.model.param.request;

import cn.hutool.core.bean.BeanUtil;
import com.fcb.coupon.app.model.bo.CouponQueryBo;
import lombok.Data;

import java.util.Objects;

/**
 * TODO
 *
 * @Author WeiHaiQi
 * @Date 2021-08-24 15:14
 **/
@Data
public class QueryCouponSaasRequest extends QueryCouponRequest {

    private String authorization;

    @Override
    public CouponQueryBo convert() {
        CouponQueryBo bo = new CouponQueryBo();
        BeanUtil.copyProperties(this, bo);

        // userid
        bo.setRealUserId(this.getUserId());

        if (this.getCurrentPage() < 1) {
            bo.setCurrentPage(1);
        }
        if (this.getItemsPerPage() < 1) {
            bo.setItemsPerPage(10);
        }

        if (Objects.equals(this.getCouponDiscountType(), 999)) {
            bo.setCouponDiscountType(null);
        }
        return bo;
    }
}
