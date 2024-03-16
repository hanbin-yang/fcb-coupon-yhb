package com.fcb.coupon.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fcb.coupon.backend.mapper.CouponGenerateBatchMapper;
import com.fcb.coupon.backend.model.entity.CouponGenerateBatchEntity;
import com.fcb.coupon.backend.model.param.request.PageRequest;
import com.fcb.coupon.backend.model.param.response.CouponGenerateBatchResponse;
import com.fcb.coupon.backend.model.param.response.PageResponse;
import com.fcb.coupon.backend.service.CouponGenerateBatchService;
import com.fcb.coupon.common.enums.CouponBatchTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class CouponGenerateBatchServiceImpl extends ServiceImpl<CouponGenerateBatchMapper, CouponGenerateBatchEntity> implements CouponGenerateBatchService {

//    @Resource
//    private CouponThemeService couponThemeService;
//    @Resource
//    private MidPlatformLoginHelper midPlatformLoginHelper;

    @Override
    public PageResponse<CouponGenerateBatchResponse> listPage(PageRequest request) {
        IPage<CouponGenerateBatchEntity> entityPage = new Page<>(request.getCurrentPage(), request.getItemsPerPage());
        LambdaQueryWrapper<CouponGenerateBatchEntity> query = Wrappers.lambdaQuery(CouponGenerateBatchEntity.class).orderByDesc(CouponGenerateBatchEntity::getCreateTime);
        entityPage = this.page(entityPage, query);
        if (entityPage.getTotal() == 0) {
            return new PageResponse<>(Collections.emptyList(), 0);
        }

        List<CouponGenerateBatchResponse> responseList = new ArrayList<>();
        for (CouponGenerateBatchEntity entity : entityPage.getRecords()) {
            CouponGenerateBatchResponse response = new CouponGenerateBatchResponse();
            BeanUtils.copyProperties(entity, response);
            response.setTypeName(CouponBatchTypeEnum.getDescByType(entity.getType()));
            response.setModuleName("优惠券");
            responseList.add(response);
        }
        return new PageResponse<>(responseList, (int) entityPage.getTotal());
    }


//    @Override
//    public ExportImportCouponTaskDto queryCouponGenerateLog(Long couponGenerateBatchId) {
//        ExportImportCouponTaskDto result = new ExportImportCouponTaskDto();
//
//        CouponGenerateBatchEntity generateBatchEntity = this.baseMapper.selectById(couponGenerateBatchId);
//        if (Objects.nonNull(generateBatchEntity)) {
//            result.setSnum("1");
//            result.setType("导入券码");
//            result.setSendCouponStatus(null != generateBatchEntity.getSendCouponStatus() && generateBatchEntity.getSendCouponStatus().intValue() == 1 ? "成功" : "失败");
//            result.setGenerateNums(generateBatchEntity.getGenerateNums());
//            result.setCreateTime(transforNull(DateUtils.parseDateToString(generateBatchEntity.getCreateTime(), DateUtils.FORMAT_YYYY_MM_DD_HH_MM_SS)));
//            result.setCreateUsername(transforNull(generateBatchEntity.getCreateUsername()));
//            result.setGenerateBatchId(generateBatchEntity.getId());
//
//            CouponThemeEntity couponThemeEntity = couponThemeService.getById(generateBatchEntity.getThemeId());
//            if (Objects.nonNull(couponThemeEntity)) {
//                result.setThemeTitle(couponThemeEntity.getThemeTitle());
//                result.setThemeTime(DateUtils.parseDateToString(couponThemeEntity.getStartTime(), DateUtils.FORMAT_YYYY_MM_DD_HH_MM_SS) + " - "
//                        + DateUtils.parseDateToString(couponThemeEntity.getEndTime(), DateUtils.FORMAT_YYYY_MM_DD_HH_MM_SS));
//            }
//        }
//        return result;
//    }
//
//    /**
//     * null值转换
//     *
//     * @param obj
//     * @return
//     */
//    private String transforNull(Object obj) {
//        if (obj == null) {
//            return "";
//        }
//        return String.valueOf(obj);
//    }
}
