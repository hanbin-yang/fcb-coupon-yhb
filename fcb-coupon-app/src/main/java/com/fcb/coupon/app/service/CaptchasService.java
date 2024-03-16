package com.fcb.coupon.app.service;

import com.fcb.coupon.app.model.bo.CaptchasBo;

/**
 * 验证码处理
 * @author mashiqiong
 * @date 2021/08/19
 */
public interface CaptchasService {

    /**
     * 校验验证码是否有效
     *
     * @param bo
     */
    void verifyCaptchas(CaptchasBo bo);

    /**
     * 发送验证码
     *
     * @param bo
     * @return
     */
    void sendCaptchasWithTx(CaptchasBo bo) ;

    /**
     * 查询验证码是否存在
     *
     * @param bo
     */
    Boolean queryCaptchas(CaptchasBo bo);
}
