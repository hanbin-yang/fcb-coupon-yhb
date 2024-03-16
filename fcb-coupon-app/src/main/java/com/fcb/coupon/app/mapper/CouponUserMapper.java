package com.fcb.coupon.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fcb.coupon.app.model.bo.CouponUserListBo;
import com.fcb.coupon.app.model.entity.CouponUserEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 劵表 Mapper 接口
 * </p>
 *
 * @author 自动生成
 * @since 2021-06-11
 */
public interface CouponUserMapper extends BaseMapper<CouponUserEntity> {


    Integer countByEffective(@Param("query") CouponUserListBo query);

    /*
     * @description 查询用户有效优惠券列表
     * @author 唐陆军
     * @param: query
     * @date 2021-8-27 14:33
     * @return: java.util.List<com.fcb.coupon.app.model.entity.CouponUserEntity>
     */
    List<CouponUserEntity> listByEffective(@Param("query") CouponUserListBo query, @Param("start") Integer start, @Param("page") Integer page);


    Integer countByExpired(@Param("query") CouponUserListBo query);

    /*
     * @description 查询用户失效优惠券列表
     * @author 唐陆军
     * @param: query
     * @date 2021-8-27 14:33
     * @return: java.util.List<com.fcb.coupon.app.model.entity.CouponUserEntity>
     */
    List<CouponUserEntity> listByExpired(@Param("query") CouponUserListBo query, @Param("start") Integer start, @Param("page") Integer page);

}
