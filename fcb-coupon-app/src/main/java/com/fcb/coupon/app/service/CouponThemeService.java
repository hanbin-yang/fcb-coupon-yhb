package com.fcb.coupon.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fcb.coupon.app.model.bo.CouponThemeListBo;
import com.fcb.coupon.app.model.bo.CouponThemeListHouseBo;
import com.fcb.coupon.app.model.entity.CouponThemeEntity;
import com.fcb.coupon.app.model.param.response.CouponThemeResponse;
import com.fcb.coupon.app.model.param.response.PageResponse;

import java.util.List;

/**
 * @author mashiqiong
 * @date 2021-06-16 17:26
 */
public interface CouponThemeService {

    /*
     * @description 获取优惠券活动信息
     * @author 唐陆军
     * @param: themeId
     * @date 2021-9-2 10:10
     * @return: com.fcb.coupon.app.model.param.response.CouponThemeListResponse
     */
    CouponThemeResponse getByThemeId(Long themeId);

    /*
     * @description 批量查询优惠券活动信息
     * @author 唐陆军
     * @param: themeIds
     * @date 2021-9-2 11:56
     * @return: com.fcb.coupon.app.model.param.response.CouponThemeResponse
     */
    List<CouponThemeResponse> listByThemeIds(List<Long> themeIds);

    /**
     * 优惠券活动列表
     */
    List<CouponThemeResponse> listByThemeIds(List<Long> themeIds, Integer userType, String userId);


}
