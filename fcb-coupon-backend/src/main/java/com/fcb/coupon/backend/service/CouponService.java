package com.fcb.coupon.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fcb.coupon.backend.model.bo.*;
import com.fcb.coupon.backend.model.dto.CouponMergedDto;
import com.fcb.coupon.backend.model.entity.CouponEntity;
import com.fcb.coupon.backend.model.entity.CouponThirdEntity;
import com.fcb.coupon.backend.model.param.response.CouponViewResponse;
import com.fcb.coupon.backend.model.param.response.PageResponse;

import java.util.List;

public interface CouponService extends IService<CouponEntity> {

    /*
    批量发送自动生成券
     */
    void batchSendGenerateCoupon(List<CouponMergedDto> couponMergedDtos);

    /*
    批量发送第三方券
     */
    void batchSendThirdPartCoupon(List<CouponMergedDto> couponMergedDtos);


    /*
    批量更新为可使用状态
     */
    Integer batchUpdateUseStatus(List<CouponEntity> couponEntities);


    /**
     * 管理后台-优惠券明细
     *
     * @param bo 请求参数
     * @return
     */
    PageResponse<CouponViewResponse> queryCouponByPageRequest(CouponQueryBo bo);

    /**
     * 异步导出优惠券明细
     *
     * @param bo
     * @return
     */
    Long exportCouponListAsync(CouponExportBo bo);

    /**
     * 异步导出赠送优惠券明细
     *
     * @param bo
     * @return
     */
    Long exportDonateCouponListAsync(DonateCouponsExportBo bo);

    /**
     * 动态表查询券
     *
     * @param tableName 券表名
     * @param param     查询参数
     * @return
     */
    List<CouponEntity> dynamicSelect(String tableName, CouponQueryWrapperBo param);


    /**
     * 作废优惠券明细
     *
     * @param bo
     */
    void invalidCouponWithTx(CouponInvalidBo bo);

    /**
     * 冻结/解冻优惠券
     *
     * @param bo
     * @return
     */
    void freezeCouponWithTx(FreezeCouponBo bo);

    /**
     * 优惠券延期
     *
     * @param bo
     * @return
     */
    void postponeCouponWithTx(PostponeCouponBo bo);


    /*
    生产券
     */
    void generateCouponsWithTx(List<CouponEntity> couponEntityList);

    /*
     * @description 生成第三方券码
     * @author 唐陆军
     * @param: couponEntityList
     * @param: couponThirdEntities
     * @date 2021-8-11 18:49
     */
    void generateThirdCouponsWithTx(List<CouponEntity> couponEntityList, List<CouponThirdEntity> couponThirdEntities);

    /*
     * @description 同步到ES
     * @author 唐陆军

     * @param: couponEntityList
     * @date 2021-8-4 19:38
     */
    void syncCouponEs(List<CouponEntity> couponEntityList);
}
