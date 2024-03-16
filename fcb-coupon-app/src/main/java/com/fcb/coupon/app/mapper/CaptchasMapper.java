package com.fcb.coupon.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fcb.coupon.app.model.entity.CaptchasEntity;

/**
 * <p>
 * 劵表 Mapper 接口
 * </p>
 *
 * @author 自动生成
 * @since 2021-08-19
 */
public interface CaptchasMapper extends BaseMapper<CaptchasEntity> {


    /**
    * 方法描述:将某个手机号的验证码全部失效
    * @author liubingpei
    * @date 16:54 2021-9-15
    * @param captchasEntity
    * @return
    */
    void disabledByMobile(CaptchasEntity captchasEntity);


    /**
     * 更新验证码（消费验证码）
     * @param captchasEntity
     * @return
     */
    int updateByMobile(CaptchasEntity captchasEntity);


}
