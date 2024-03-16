package com.fcb.coupon.app.business;

import com.fcb.coupon.app.model.PageDto;
import com.fcb.coupon.app.model.bo.CouponUserGetBo;
import com.fcb.coupon.app.model.bo.CouponUserListBo;
import com.fcb.coupon.app.model.param.response.CouponDetailResponse;
import com.fcb.coupon.app.model.param.response.CouponListResponse;
import com.fcb.coupon.app.model.param.response.CouponUserEffectiveSoaResponse;
import com.fcb.coupon.app.model.param.response.PageResponse;

import java.util.List;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月27日 17:18:00
 */
public interface CouponUserBusiness {


    /*
     * @description 查询用户优惠券详情
     * @author 唐陆军
     * @param: bo
     * @date 2021-8-31 11:12
     * @return: com.fcb.coupon.app.model.param.response.CouponDetailResponse
     */
    CouponDetailResponse getByCouponId(CouponUserGetBo bo);

    PageResponse<CouponListResponse> listByEffective(CouponUserListBo queryBo, PageDto pageDto);

    List<CouponUserEffectiveSoaResponse> listByEffectiveAndUnionIds(List<String> unionIdList);

    PageResponse<CouponListResponse> listByExpired(CouponUserListBo queryBo, PageDto pageDto);

}
