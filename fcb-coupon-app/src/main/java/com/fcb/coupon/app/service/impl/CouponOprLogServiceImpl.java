package com.fcb.coupon.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fcb.coupon.app.mapper.CouponOprLogMapper;
import com.fcb.coupon.app.model.dto.OprLogDo;
import com.fcb.coupon.app.model.entity.CouponOprLogEntity;
import com.fcb.coupon.app.model.param.response.PageResponse;
import com.fcb.coupon.app.service.CouponOprLogService;
import com.fcb.coupon.common.constant.CouponConstant;
import com.fcb.coupon.common.util.RedisUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * @author YangHanBin
 * @date 2021-06-16 18:23
 */
@Service
public class CouponOprLogServiceImpl extends ServiceImpl<CouponOprLogMapper, CouponOprLogEntity> implements CouponOprLogService {

    /**
     * 异步日志
     * @param dto 入参
     */
    @Override
    @Async("couponCommonExecutor")
    public void saveOprLogAsync(OprLogDo dto) {
        saveOprLog(dto);
    }

    /**
     * 非异步
     * @param dto 入参
     */
    @Override
    public void saveOprLog(OprLogDo dto) {
        CouponOprLogEntity entity = new CouponOprLogEntity();
        entity.setOprRefId(dto.getRefId());
        entity.setOprSummary(dto.getOprType().getDesc());
        entity.setOprType(dto.getOprType().getType());
        entity.setOprThemeType(dto.getOprThemeType().getType());
        entity.setOperContent(dto.getOprContent());
        entity.setCreateUserid(dto.getOprUserId());
        entity.setCreateUsername(dto.getOprUserName());
        entity.setExtData(dto.getExtData());
        baseMapper.insert(entity);
    }

    @Override
    public void saveOprLogBatch(List<OprLogDo> doList) {
        List<CouponOprLogEntity> insetBeans =new ArrayList<>();
        Queue<Long> ids = RedisUtil.generateIds(doList.size());
        doList.forEach(bean -> {
            CouponOprLogEntity entity = new CouponOprLogEntity();
            entity.setId(ids.poll());
            entity.setOprRefId(bean.getRefId());
            entity.setOprSummary(bean.getOprType().getDesc());
            entity.setOprType(bean.getOprType().getType());
            entity.setOprThemeType(bean.getOprThemeType().getType());
            entity.setOperContent(bean.getOprContent());
            entity.setCreateUserid(bean.getOprUserId());
            entity.setCreateUsername(bean.getOprUserName());
            entity.setIsDeleted(CouponConstant.NO);
            insetBeans.add(entity);
        });
        saveBatch(insetBeans);
    }
}
