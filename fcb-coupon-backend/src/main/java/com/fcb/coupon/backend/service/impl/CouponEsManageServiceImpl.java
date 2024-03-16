package com.fcb.coupon.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fcb.coupon.backend.elasticsearch.CouponEsDoc;
import com.fcb.coupon.backend.exception.CouponThemeErrorCode;
import com.fcb.coupon.backend.model.bo.CouponQueryWrapperBo;
import com.fcb.coupon.backend.model.cache.CouponThemeCache;
import com.fcb.coupon.backend.model.entity.CouponEntity;
import com.fcb.coupon.backend.model.entity.CouponThemeEntity;
import com.fcb.coupon.backend.model.entity.CouponUserEntity;
import com.fcb.coupon.backend.service.*;
import com.fcb.coupon.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 优惠券ES管理服务实现
 *
 * @Author WeiHaiQi
 * @Date 2021-06-18 15:52
 **/
@Service
@Slf4j
public class CouponEsManageServiceImpl implements CouponEsManageService {

    @Resource
    private CouponEsDocService couponEsDocService;
    @Resource
    private CouponService couponService;
    @Resource
    private CouponThemeCacheService couponThemeCacheService;
    @Resource
    private CouponUserService couponUserService;
    @Resource
    private CouponGiveService couponGiveService;
    @Resource
    private CouponThemeService couponThemeService;
    @Resource
    private CouponVerificationService couponVerificationService;

    @Override
    public void syncAllCoupon() {
        LambdaQueryWrapper<CouponThemeEntity> queryWrapper = Wrappers.lambdaQuery(CouponThemeEntity.class);
        queryWrapper.select(CouponThemeEntity::getId);
        queryWrapper.orderByDesc(CouponThemeEntity::getCreateTime);

        int page = 0;
        int pageSie = 100;
        do {
            queryWrapper.last(String.format("limit %d,%d",page*pageSie,pageSie));
            List<CouponThemeEntity> themeIdList = couponThemeService.getBaseMapper().selectList(queryWrapper);
            if (CollectionUtils.isEmpty(themeIdList)) {
                break;
            }

            List<Long> couponThemeIdList = themeIdList.stream().map(CouponThemeEntity::getId).collect(Collectors.toList());
            batchConponThemeToEs(couponThemeIdList);
            page++;
        } while (true);
    }

    @Override
    public void refreshEsByCouponIds(List<Long> ids) {
        List<CouponEntity> couponList = couponService.listByIds(ids);
        if (CollectionUtils.isEmpty(couponList)) {
            return;
        }

        List<CouponEsDoc> result = new ArrayList<>();
        couponList.forEach(vo -> {
            CouponEsDoc couponEsDoc = new CouponEsDoc();
            BeanUtils.copyProperties(vo,couponEsDoc);
            // 构建详情
            bindCouponEsDoc(vo.getId(), couponEsDoc);

            result.add(couponEsDoc);
        });

        couponEsDocService.saveBatch(result);
    }

    /**
     * 构建优惠券详情
     * @param couponId
     * @param couponEsDoc
     */
    private void bindCouponEsDoc(Long couponId,CouponEsDoc couponEsDoc) {
        // 优惠券活信息
        Long couponThemId = couponEsDoc.getCouponThemeId();
        CouponThemeCache couponThemeCache = couponThemeCacheService.getById(couponThemId);

        // 券用户信息
        CouponUserEntity couponUser = couponUserService.getById(couponId);
        if (Objects.nonNull(couponUser)) {
            couponEsDoc.setBindTel(couponUser.getBindTel());
            couponEsDoc.setBindTime(couponUser.getCreateTime());
        }

    }



    /**
     * 刷新ES
     * @param couponThemeId
     * @return
     */
    @Override
    public long refreshEsByCouponThemeId(Long couponThemeId) {
        long totalAmount = 0;
        CouponThemeEntity themePO = couponThemeService.getBaseMapper().selectById(couponThemeId);
        if (Objects.isNull(themePO)) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_NOT_EXIST.getCode(),CouponThemeErrorCode.COUPON_THEME_NOT_EXIST.getMessage());
        }

        CouponQueryWrapperBo qw = new CouponQueryWrapperBo();
        qw.setCouponThemeId(couponThemeId);
        totalAmount = refreshEsBatch(qw);

        log.info("条件刷新 es end: couponThemeId={}, output刷新总数total={}", couponThemeId, totalAmount);
        return totalAmount;
    }

    /**
     * 刷新Es
     * @param qw
     * @return
     */
    private long refreshEsBatch(CouponQueryWrapperBo qw) {
        int pageSize = 5000;
        qw.setPageSize(pageSize);

        List<CouponEsDoc> couponPOList;
        long total = 0;

        for (int num=1; num<33; num++) {
            String couponTableName = String.format("coupon%03d",num);

            int i = 0;
            do {
                qw.setOffset(i * pageSize);

                couponPOList = listByDynamicSelectiveForEs(qw,couponTableName);
                if (CollectionUtils.isEmpty(couponPOList)) {
                    log.info("刷新es total={}", total);
                    break;
                }

                couponEsDocService.saveBatch(couponPOList);

                i++;
                total += couponPOList.size();
            } while (true);
        }
        return total;
    }

    /**
     * 动态表查券
     * @param qw                参数
     * @param couponTableName   券分表
     * @return
     */
    private List<CouponEsDoc> listByDynamicSelectiveForEs(CouponQueryWrapperBo qw,String couponTableName) {

        List<CouponEntity> couponList = couponService.dynamicSelect(couponTableName,qw);
        List<CouponEsDoc> result = new ArrayList<>();
        couponList.forEach(vo -> {
            CouponEsDoc couponEsDoc = new CouponEsDoc();
            BeanUtils.copyProperties(vo,couponEsDoc);
            // 构建详情
            bindCouponEsDoc(vo.getId(), couponEsDoc);

            result.add(couponEsDoc);
        });

        return result;
    }

    /**
     * 批量同步优惠券活动下的所有券到ES
     * @param themeIdList
     */
    private void batchConponThemeToEs(List<Long> themeIdList){
        List<List<Long>> subList = listToGroupList(themeIdList,20);

        subList.forEach(list -> {
            final CountDownLatch downLatch = new CountDownLatch(list.size());
            ExecutorService executor = Executors.newFixedThreadPool(list.size());

            list.forEach(couponThemeId -> {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            refreshEsByCouponThemeId(couponThemeId);
                        } catch (Exception e) {
                            log.error("优惠券活动 id:{} 全量刷新ES异常 {}",couponThemeId,e);
                        }
                        downLatch.countDown();
                    }
                });
            });

            try {
                downLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * list对象分段
     * @param sourceList       源列表
     * @param subLen           每段最大长度
     * @param <T>
     * @return
     */
    private <T> List<List<T>> listToGroupList(List<T> sourceList,int subLen) {
        int maxSize = subLen;
        int pageNum = 1;

        int modNum = 0;
        if (maxSize < sourceList.size()) {
            pageNum = sourceList.size()/maxSize;
            modNum = sourceList.size()%maxSize;

            if (modNum > 0) {
                pageNum++;
            }
        } else {
            modNum = sourceList.size();
        }

        List<List<T>> result = new ArrayList<>();
        for (int i=1;i<=pageNum;i++) {
            int startIndex = (i -1) * maxSize;
            int endIndex = i * maxSize;
            if (i == pageNum && modNum > 0) {
                endIndex = startIndex + modNum;
            }

            List<T> sublist = sourceList.subList(startIndex,endIndex);
            result.add(sublist);
        }
        return result;
    }
}
