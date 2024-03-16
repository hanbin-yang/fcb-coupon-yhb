package com.fcb.coupon.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fcb.coupon.backend.mapper.CouponOprLogMapper;
import com.fcb.coupon.backend.model.bo.CouponOprLogQueryBo;
import com.fcb.coupon.backend.model.dto.OprLogDo;
import com.fcb.coupon.backend.model.param.response.CouponOprLogResponse;
import com.fcb.coupon.backend.model.param.response.PageResponse;
import com.fcb.coupon.backend.service.CouponOprLogService;
import com.fcb.coupon.common.constant.CouponConstant;
import com.fcb.coupon.common.util.RedisUtil;
import com.fcb.coupon.backend.model.entity.CouponOprLogEntity;
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
        baseMapper.insertBatch(insetBeans);
    }

    @Override
    public PageResponse<CouponOprLogResponse> listByPageRequest(CouponOprLogQueryBo bo) {
        QueryWrapper<CouponOprLogEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("opr_ref_id",bo.getOprRefId());
        queryWrapper.eq("opr_theme_type",bo.getOprThemeType());
        queryWrapper.orderByDesc("create_time");
        Integer total = baseMapper.selectCount(queryWrapper);

        int start = bo.getStartItem();
        queryWrapper.last(String.format("limit %d,%d",start,bo.getItemsPerPage()));
        List<CouponOprLogEntity> couponEntityList = baseMapper.selectList(queryWrapper);

        List<CouponOprLogResponse> list = couponEntityList.stream().map(vo -> {
            CouponOprLogResponse rs = new CouponOprLogResponse();
            BeanUtils.copyProperties(vo,rs);
            return rs;
        }).collect(Collectors.toList());

        PageResponse<CouponOprLogResponse> result = new PageResponse<>();
        result.setListObj(list);
        result.setTotal(total);
        return result;
    }
}
