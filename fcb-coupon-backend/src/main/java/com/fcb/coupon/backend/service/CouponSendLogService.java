package com.fcb.coupon.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fcb.coupon.backend.model.entity.CouponSendLogEntity;

import java.util.List;


/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月23日 08:38:00
 */
public interface CouponSendLogService extends IService<CouponSendLogEntity> {


    /*
     * @description 根据批次号和sourceid查询
     */
    List<CouponSendLogEntity> listByThemeIdAndTransIds(Long themeId, List<String> transactionIds);

}
