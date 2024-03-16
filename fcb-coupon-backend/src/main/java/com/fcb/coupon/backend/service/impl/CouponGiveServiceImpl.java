package com.fcb.coupon.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fcb.coupon.backend.mapper.CouponGiveMapper;
import com.fcb.coupon.backend.service.CouponGiveService;
import com.fcb.coupon.backend.model.entity.CouponGiveEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 转赠服务
 *
 * @Author WeiHaiQi
 * @Date 2021-06-22 15:39
 **/
@Service
@Slf4j
public class CouponGiveServiceImpl extends ServiceImpl<CouponGiveMapper, CouponGiveEntity> implements CouponGiveService {
}
