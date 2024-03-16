package com.fcb.coupon.backend.business.couponTheme;

import com.fcb.coupon.backend.model.bo.CouponThemeSaveBo;
import com.fcb.coupon.backend.model.bo.CouponThemeUpdateAfterCheckBo;
import com.fcb.coupon.backend.model.bo.CouponThemeUpdateBo;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年09月03日 11:37:00
 */
public interface CouponThemeBusiness {

    /**
     * 添加一个券活动
     *
     * @param bo bo
     */
    Long save(CouponThemeSaveBo bo);

    /**
     * 编辑单个券活动
     *
     * @param bo bo
     */
    boolean edit(CouponThemeUpdateBo bo);

    /**
     * 复制 券活动
     *
     * @param couponThemeId 券活动主键
     * @return true/false
     */
    boolean copy(Long couponThemeId);

    /**
     * 关闭券活动
     *
     * @param couponThemeId 券活动主键
     */
    boolean close(Long couponThemeId);

    /**
     * 审核通过
     */
    boolean auditPass(Long couponThemeId, String remark);

    /**
     * 更新规则
     */
    boolean updateAfterCheck(CouponThemeUpdateAfterCheckBo bo);






}
