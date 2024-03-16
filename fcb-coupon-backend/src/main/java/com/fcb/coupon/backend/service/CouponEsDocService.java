package com.fcb.coupon.backend.service;

import com.fcb.coupon.backend.elasticsearch.CouponEsDoc;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.util.List;


public interface CouponEsDocService {
    /**
     * 保存券到ES
     *
     * @param couponEsDoc 券信息
     * @author liangjiangyun
     * @date 2021/4/13
     * @since 0.0.1
     */
    void save(CouponEsDoc couponEsDoc);

    /**
     * 批量保存券明细列表
     *
     * @param couponEsDocs 券信息列表
     * @author liangjiangyun
     * @date 2021/4/13
     * @since 0.0.1
     */
    void saveBatch(List<CouponEsDoc> couponEsDocs);

    /**
     * 保存/更新券到ES
     *
     * @param couponEsDoc 券信息
     * @author liangjiangyun
     * @date 2021/4/13
     * @since 0.0.1
     */
    void saveOrUpdate(CouponEsDoc couponEsDoc);


    /*
    保存/更新券到ES（会比较版本号）
     */
    void saveOrUpdateByVersion(CouponEsDoc couponEsDoc);

    /**
     * 根据ID更新券ES
     *
     * @param couponEsDoc
     */
    void updateById(CouponEsDoc couponEsDoc);

    /**
     * 根据couponThemeId更新字段
     *
     * @param couponThemeId
     * @param couponEsDoc
     * @return
     */
    long updateSelectedFieldsByCouponThemeId(Long couponThemeId, CouponEsDoc couponEsDoc);

    void updateBatch(List<CouponEsDoc> list);

    /**
     * 分页查询
     *
     * @param queryBuilder
     * @return
     */
    Page<CouponEsDoc> searchPage(NativeSearchQueryBuilder queryBuilder);
}
