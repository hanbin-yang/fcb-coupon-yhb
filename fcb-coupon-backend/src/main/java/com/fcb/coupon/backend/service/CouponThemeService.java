package com.fcb.coupon.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fcb.coupon.backend.model.bo.*;
import com.fcb.coupon.backend.model.entity.CouponThemeEntity;
import com.fcb.coupon.backend.model.param.response.*;
import com.fcb.coupon.client.backend.param.request.CouponThemeListCmsRequest;
import com.fcb.coupon.client.backend.param.response.CouponThemeListCmsResponse;
import com.fcb.coupon.common.enums.CouponThemeStatus;

import java.util.List;

/**
 * @author YangHanBin
 * @date 2021-06-11 17:06
 */
public interface CouponThemeService extends IService<CouponThemeEntity> {


    /**
     * 删除单个券活动
     *
     * @param couponThemeId 券活动主键
     * @return 1:删除成功
     */
    boolean delete(Long couponThemeId);

    /*
    更新券活动状态
     */
    int updateCouponThemeStatus(Long couponThemeId, CouponThemeStatus status);

    /*
    更新券活动状态
     */
    boolean submitAudit(Long couponThemeId);

    /*
    审核不通过
     */
    boolean auditNotPass(Long couponThemeId, String remark);


    /*
     * @description 获取优惠券金额信息
     * @author 唐陆军
     * @param: couponThemeEntity
     * @date 2021-8-6 17:28
     * @return: java.lang.String
     */
    String getCouponAmount(CouponThemeEntity couponThemeEntity);


    /**
     * 查询优惠券列表-优惠券管理
     *
     * @param bo
     * @return
     */
    PageResponse<CouponThemeListResponse> listCouponTheme(CouponThemeListBo bo);

    /**
     * 查询优惠券列表-运营位
     *
     * @param bo
     * @return
     */
    PageResponse<CouponThemePositionMarketingResponse> listPositionMarketingCouponTheme(CouponThemePositionMarketingBo bo);

    /**
     * 查询优惠券列表-主动营销
     *
     * @param bo
     * @return
     */
    PageResponse<CouponThemeInitiativeMarketingResponse> listInitiativeMarketingCouponTheme(CouponThemeInitiativeMarketingBo bo);

    /**
     * 通过组织id关联优惠券活动id
     *
     * @param orgIds
     * @return
     */
    List<Long> queryAuthThemeId(List<Long> orgIds);


    /**
     * @author 唐陆军
     * @Description 异步导出优惠券活动
     * @createTime 2021年06月17日 15:53:00
     */
    Long exportCouponThemeListAsync(CouponThemeExportBo bo);

    /**
     * 管理后台->营销中心->优惠券管理->查看优惠券活动详情
     *
     * @param bo
     * @return
     */
    CouponThemeDetailResponse getCouponThemeDetailById(CouponThemeDetailBo bo);


    /*
    cms查询优惠券活动列表
     */
    CouponThemeListCmsResponse listByCms(CouponThemeListCmsRequest request);

}
