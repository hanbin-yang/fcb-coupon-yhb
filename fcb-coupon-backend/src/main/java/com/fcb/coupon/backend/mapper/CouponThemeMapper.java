package com.fcb.coupon.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fcb.coupon.backend.model.dto.*;
import com.fcb.coupon.backend.model.entity.CouponThemeEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 劵活动表 Mapper 接口
 * </p>
 *
 * @author 自动生成
 * @since 2021-06-11
 */
public interface CouponThemeMapper extends BaseMapper<CouponThemeEntity> {
    /**
     * 管理后台->营销中心->优惠券管理->优惠券活动列表
     *
     * @param dos 查询条件
     * @return
     */
    List<CouponThemeEntity> listCouponTheme(CouponThemeListDo dos);

    /**
     * 管理后台->营销中心->优惠券管理->优惠券活动列表 统计数量
     *
     * @param dos 查询条件
     * @return
     */
    Integer listCouponThemeCount(CouponThemeListDo dos);

    /**
     * 管理后台->营销中心->优惠券管理->优惠券运营位管理->添加优惠券->查询优惠券列表
     *
     * @param dos 查询条件
     * @return
     */
    List<CouponThemeEntity> listPositionMarketingCouponTheme(CouponThemePositionMarketingDo dos);

    /**
     * 管理后台->营销中心->优惠券管理->优惠券运营位管理->添加优惠券->查询优惠券列表 统计数量
     *
     * @param dos 查询条件
     * @return
     */
    Integer listPositionMarketingCouponThemeCount(CouponThemePositionMarketingDo dos);

    /**
     * 管理后台->营销中心->主动营销->营销任务管理->编辑任务流->添加优惠券->查询优惠券列表
     *
     * @param dos 查询条件
     * @return
     */
    List<CouponThemeEntity> listInitiativeMarketingCouponTheme(CouponThemeInitiativeMarketingDo dos);

    /**
     * 管理后台->营销中心->主动营销->营销任务管理->编辑任务流->添加优惠券->查询优惠券列表 统计数量
     *
     * @param dos 查询条件
     * @return
     */
    Integer listInitiativeMarketingCouponThemeCount(CouponThemeInitiativeMarketingDo dos);

    /**
     * 管理后台->营销中心->优惠券管理->优惠券活动列表->导出Excel
     *
     * @param dos 查询条件
     * @return
     */
    List<CouponThemeEntity> listExportCouponTheme(CouponThemeExportDo dos);

    /**
     * 管理后台->营销中心->优惠券管理->优惠券活动列表->导出Excel 统计数量
     *
     * @param dos 查询条件
     * @return
     */
    Integer listExportCouponThemeCount(CouponThemeExportDo dos);

    /**
     * 管理后台->营销中心->营销活动页->新增页面->选择优惠券活动列表
     *
     * @param dos 查询条件
     * @return
     */
    List<CouponThemeEntity> listActivityPageCouponTheme(CouponThemeActivityPageDo dos);

    /**
     * 管理后台->营销中心->营销活动页->新增页面->选择优惠券活动列表 统计数量
     *
     * @param dos 查询条件
     * @return
     */
    Integer listActivityPageCouponThemeCount(CouponThemeActivityPageDo dos);

    /**
     * 通过组织id关联优惠券活动id
     *
     * @param orgIds
     * @return
     */
    List<Long> queryAuthThemeId(@Param("orgIds") List<Long> orgIds);


    /*
    分页查询cms需要的活动列表数据
     */
    CouponThemeCmsPageQuery<CouponThemeEntity> listByCms(@Param("query") CouponThemeCmsPageQuery<CouponThemeEntity> query);
}
