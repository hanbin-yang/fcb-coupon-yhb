package com.fcb.coupon.app.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fcb.coupon.app.facade.ClientUserFacade;
import com.fcb.coupon.app.mapper.CouponThemeMapper;
import com.fcb.coupon.app.model.bo.CouponThemeListBo;
import com.fcb.coupon.app.model.bo.CouponThemeListHouseBo;
import com.fcb.coupon.app.model.dto.CouponThemeCache;
import com.fcb.coupon.app.model.dto.CouponUserStatisticCache;
import com.fcb.coupon.app.model.entity.CouponThemeEntity;
import com.fcb.coupon.app.model.extension.CouponThemeCacheExtension;
import com.fcb.coupon.app.model.param.response.*;
import com.fcb.coupon.app.service.CouponThemeCacheService;
import com.fcb.coupon.app.service.CouponThemeService;
import com.fcb.coupon.app.service.CouponUserStatisticCacheService;
import com.fcb.coupon.app.service.UserFacadeService;
import com.fcb.coupon.common.constant.CouponConstant;
import com.fcb.coupon.common.enums.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author mashiqiong
 * @date 2021-8-17 15:30
 */
@Service
@RefreshScope
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@Slf4j
public class CouponThemeServiceImpl implements CouponThemeService {
    public static final String CROWD_SCOPE_IDS = "crowdScopeIds";
    public static final String IDS = "ids";

    private final CouponThemeCacheService couponThemeCacheService;
    private final CouponUserStatisticCacheService couponUserStatisticCacheService;


    @Override
    public CouponThemeResponse getByThemeId(Long themeId) {
        return getByThemeIdAndUserUserId(themeId, null, null);
    }

    @Override
    public List<CouponThemeResponse> listByThemeIds(List<Long> themeIds) {
        return listByThemeIds(themeIds, null, null);
    }

    /**
     * 查询优惠券活动列表
     */
    @Override
    public List<CouponThemeResponse> listByThemeIds(List<Long> themeIds, Integer userType, String userId) {
        if (CollectionUtils.isEmpty(themeIds)) {
            return Collections.EMPTY_LIST;
        }
        List<CouponThemeResponse> resultList = new ArrayList<>();
        for (Long themeId : themeIds) {
            CouponThemeResponse couponThemeListResponse = getByThemeIdAndUserUserId(themeId, userType, userId);
            if (couponThemeListResponse == null) {
                continue;
            }
            resultList.add(couponThemeListResponse);
        }

        return resultList;
    }


    private CouponThemeResponse getByThemeIdAndUserUserId(Long themeId, Integer userType, String userId) {
        CouponThemeCache couponThemeCache = couponThemeCacheService.getById(themeId);
        if (Objects.isNull(couponThemeCache)) {
            return null;
        }

        CouponThemeResponse couponThemeResponse = new CouponThemeResponse();
        BeanUtils.copyProperties(couponThemeCache, couponThemeResponse, CROWD_SCOPE_IDS);
        //单独设置适用人群属性值
        List<Integer> crowdScopeIdList = convertCrowdScopeId(couponThemeCache);
        couponThemeResponse.setCrowdScopeIds(crowdScopeIdList);

        //单独设置适用人群中文名称
        List<String> crowdScopeNameList = convertCrowdScopeName(crowdScopeIdList);
        couponThemeResponse.setCrowdScopedStr(String.join("、", crowdScopeNameList));

        CouponThemeCacheExtension couponThemeCacheExtension = new CouponThemeCacheExtension(couponThemeCache);

        //券面额
        couponThemeResponse.setCouponAmount(couponThemeCache.getDiscountAmount());
        //使用门槛
        couponThemeResponse.setUseRuleRemark(couponThemeCacheExtension.getUseRuleRemark());

        //设置优惠券状态
        setStatusStr(couponThemeResponse);

        //转换券面额
        convertCouponAmount(couponThemeResponse, couponThemeCache);

        //设置张数
        convertCount(couponThemeCache, couponThemeResponse);

        //设置前端显示状态
        setFrontStatusStr(couponThemeResponse, userType, userId);
        return couponThemeResponse;
    }


    /**
     * 适用人群json对象转数字数组
     *
     * @param couponThemeCache 实体对象
     */
    private List<Integer> convertCrowdScopeId(CouponThemeCache couponThemeCache) {
        JSONObject jsonObject = JSON.parseObject(couponThemeCache.getApplicableUserTypes());
        List<Integer> crowdScopeIdList = JSON.parseArray(jsonObject.getString(IDS), Integer.class);
        return crowdScopeIdList;
    }

    /**
     * 适用人群ids转中文名称
     *
     * @param crowdScopeIdList 适用人群Id集合
     */
    private List<String> convertCrowdScopeName(List<Integer> crowdScopeIdList) {
        List<String> crowdScopeNameList = new ArrayList<>();
        crowdScopeIdList.stream().forEach(crowdscopeId -> {
            crowdScopeNameList.add(UserTypeEnum.getStrByUserType(crowdscopeId));
        });
        return crowdScopeNameList;
    }


    /**
     * 设置前端显示状态
     */
    private void setStatusStr(CouponThemeResponse couponThemeResponse) {
        //进行中状态
        if (CouponThemeStatus.EFFECTIVE.getStatus().equals(couponThemeResponse.getStatus())) {
            //判断时间
            if (couponThemeResponse.getStartTime().after(new Date())) {
                //未开始
                couponThemeResponse.setStatus(CouponThemeStatus.APPROVED.getStatus());
                couponThemeResponse.setStatusStr(CouponThemeStatus.APPROVED.getDesc());
                return;
            }
            if (couponThemeResponse.getEndTime().before(new Date())) {
                //已失效
                couponThemeResponse.setStatus(CouponThemeStatus.INEFFECTIVE.getStatus());
                couponThemeResponse.setStatusStr(CouponThemeStatus.INEFFECTIVE.getDesc());
                return;
            }
            //进行中
            couponThemeResponse.setStatus(CouponThemeStatus.EFFECTIVE.getStatus());
            couponThemeResponse.setStatusStr(CouponThemeStatus.EFFECTIVE.getDesc());
            return;
        }

        //结束状态
        if (CouponThemeStatus.INEFFECTIVE.getStatus().equals(couponThemeResponse.getStatus()) || CouponThemeStatus.CLOSED.getStatus().equals(couponThemeResponse.getStatus())) {
            //已失效
            couponThemeResponse.setStatus(CouponThemeStatus.INEFFECTIVE.getStatus());
            couponThemeResponse.setStatusStr(CouponThemeStatus.INEFFECTIVE.getDesc());
            return;
        }

        //其他状态都列为未开始
        couponThemeResponse.setStatus(CouponThemeStatus.APPROVED.getStatus());
        couponThemeResponse.setStatusStr(CouponThemeStatus.APPROVED.getDesc());
    }


    /**
     * 设置用户优惠券状态
     */
    private void setFrontStatusStr(CouponThemeResponse couponThemeResponse, Integer userType, String userId) {
        //进行中状态
        if (CouponThemeStatus.EFFECTIVE.getStatus().equals(couponThemeResponse.getStatus())) {
            setUserFrontStatusStr(couponThemeResponse, userType, userId);
            return;
        }

        //结束状态
        if (CouponThemeStatus.INEFFECTIVE.getStatus().equals(couponThemeResponse.getStatus()) || CouponThemeStatus.CLOSED.getStatus().equals(couponThemeResponse.getStatus())) {
            // 已结束
            couponThemeResponse.setFrontStatus(CouponThemeFrontStatusEnum.FINISHED.getStatus());
            couponThemeResponse.setFrontStatusStr(CouponThemeFrontStatusEnum.FINISHED.getStatusStr());
            return;
        }

        //其他状态都列为未开始
        couponThemeResponse.setFrontStatus(CouponThemeFrontStatusEnum.NOT_STARTED.getStatus());
        couponThemeResponse.setFrontStatusStr(CouponThemeFrontStatusEnum.NOT_STARTED.getStatusStr());
    }

    /**
     * 设置用户优惠券领券状态
     */
    private void setUserFrontStatusStr(CouponThemeResponse couponThemeResponse, Integer userType, String userId) {
        //判断时间
        if (couponThemeResponse.getStartTime().after(new Date())) {
            // 未开始
            couponThemeResponse.setFrontStatus(CouponThemeFrontStatusEnum.NOT_STARTED.getStatus());
            couponThemeResponse.setFrontStatusStr(CouponThemeFrontStatusEnum.NOT_STARTED.getStatusStr());
            return;
        }
        if (couponThemeResponse.getEndTime().before(new Date())) {
            // 已结束
            couponThemeResponse.setFrontStatus(CouponThemeFrontStatusEnum.FINISHED.getStatus());
            couponThemeResponse.setFrontStatusStr(CouponThemeFrontStatusEnum.FINISHED.getStatusStr());
            return;
        }

        //判断库存
        if (couponThemeResponse.getCanSendAmount() <= 0) {
            // 已抢光
            couponThemeResponse.setFrontStatus(CouponThemeFrontStatusEnum.HAS_GONE.getStatus());
            couponThemeResponse.setFrontStatusStr(CouponThemeFrontStatusEnum.HAS_GONE.getStatusStr());
            return;
        }

        //判断是否有用户信息
        if (StringUtils.isBlank(userId)) {
            //立即领取
            couponThemeResponse.setFrontStatus(CouponThemeFrontStatusEnum.IMMEDIATELY_GET.getStatus());
            couponThemeResponse.setFrontStatusStr(CouponThemeFrontStatusEnum.IMMEDIATELY_GET.getStatusStr());
            return;
        }

        //获取用户领券数量
        CouponUserStatisticCache couponUserStatisticCache = couponUserStatisticCacheService.getByUnionKey(couponThemeResponse.getId(), userId, userType);
        if (couponUserStatisticCache == null) {
            couponUserStatisticCache.setTodayCount(0);
            couponUserStatisticCache.setMonthCount(0);
            couponUserStatisticCache.setTodayCount(0);
        }
        //设置用户领券数量
        couponThemeResponse.setReceiveNumCurrentUser(couponUserStatisticCache.getTotalCount());
        //不能再领取
        if (!validateCanReceive(couponThemeResponse, couponUserStatisticCache)) {
            // 已领取
            couponThemeResponse.setFrontStatus(CouponThemeFrontStatusEnum.ALREADY_RECEIVED.getStatus());
            couponThemeResponse.setFrontStatusStr(CouponThemeFrontStatusEnum.ALREADY_RECEIVED.getStatusStr());
            return;
        }
        //已领过
        if (couponThemeResponse.getReceiveNumCurrentUser() > 0) {
            // 再领取
            couponThemeResponse.setFrontStatus(CouponThemeFrontStatusEnum.GET_AGAIN.getStatus());
            couponThemeResponse.setFrontStatusStr(CouponThemeFrontStatusEnum.GET_AGAIN.getStatusStr());
            return;
        }

        // 立即领取
        couponThemeResponse.setFrontStatus(CouponThemeFrontStatusEnum.IMMEDIATELY_GET.getStatus());
        couponThemeResponse.setFrontStatusStr(CouponThemeFrontStatusEnum.IMMEDIATELY_GET.getStatusStr());
    }


    /*
     * @description 校验用户能否继续领
     * @author 唐陆军
     * @param: couponThemeCache
     * @param: couponUserStatisticCache
     * @date 2021-9-2 9:04
     * @return: boolean
     */
    private boolean validateCanReceive(CouponThemeResponse couponThemeResponse, CouponUserStatisticCache couponUserStatisticCache) {
        //超过总领券数
        if (couponUserStatisticCache.getTotalCount() >= couponThemeResponse.getIndividualLimit()) {
            return false;
        }
        //超过每月限领数量
        if (couponUserStatisticCache.getMonthCount() >= couponThemeResponse.getEveryMonthLimit()) {
            return false;
        }
        //超过每天限领数量
        if (couponUserStatisticCache.getTodayCount() >= couponThemeResponse.getEveryDayLimit()) {
            return false;
        }

        return true;
    }

    /**
     * 转换券面额
     *
     * @param couponThemeResponse
     * @param couponThemeCache
     */
    private void convertCouponAmount(CouponThemeResponse couponThemeResponse, CouponThemeCache couponThemeCache) {
        //券折扣
        if (Objects.equals(CouponDiscountType.DISCOUNT.getType(), couponThemeCache.getCouponDiscountType())) {
            BigDecimal discount = new BigDecimal(couponThemeCache.getDiscountValue());
            BigDecimal divisor = new BigDecimal(100);

            couponThemeResponse.setCouponAmount(new BigDecimal(discount.divide(divisor, 2, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()));
            couponThemeResponse.setUseUpLimit(couponThemeCache.getDiscountAmount());
            couponThemeResponse.setCouponUnit(CouponConstant.COUPON_UNIT_DISCOUNT);
            couponThemeResponse.setRuleType(CouponRuleType.DISCOUNT.getType());
        }

        //券金额
        if (Objects.equals(CouponDiscountType.CASH.getType(), couponThemeCache.getCouponDiscountType())) {
            couponThemeResponse.setCouponAmount(new BigDecimal(new BigDecimal(couponThemeCache.getDiscountAmount() + "").stripTrailingZeros().toPlainString()));
            couponThemeResponse.setCouponAmountExt1(BigDecimal.ZERO);
            couponThemeResponse.setCouponUnit(CouponConstant.COUPON_UNIT_AMOUNT);
            couponThemeResponse.setRuleType(CouponRuleType.AMOUNT.getType());
        }

        //是否可赠送
        if (Objects.equals(CouponDiscountType.WELFARE_CARD.getType(), couponThemeCache.getCouponDiscountType())) {
            if (Objects.equals(couponThemeCache.getCanDonation(), YesNoEnum.YES.getValue())) {
                couponThemeResponse.setCanDonationBool(Boolean.TRUE);
            } else if (Objects.equals(couponThemeCache.getCanDonation(), YesNoEnum.NO.getValue())) {
                couponThemeResponse.setCanDonationBool(Boolean.FALSE);
            }

        }
    }

    /**
     * 设置发行总张数、已经生成的张数、已领取的张数、已使用的张数、还可生成的张数
     *
     * @param couponThemeCache    活动信息
     * @param couponThemeResponse 返回前端的数据对象
     */
    private void convertCount(CouponThemeCache couponThemeCache, CouponThemeResponse couponThemeResponse) {

        //还可发放张数
        int canSendAmount = 0;
        //还可生成张数
        int availableCoupons = 0;
        //已生成张数
        int drawedCoupons = 0;
        if (Objects.equals(CouponTypeEnum.COUPON_TYPE_THIRD.getType(), couponThemeResponse.getCouponType()) || Objects.equals(CouponTypeEnum.COUPON_TYPE_REAL.getType(), couponThemeResponse.getCouponType())) {
            //还可生成张数
            availableCoupons = couponThemeCache.getTotalCount() - couponThemeCache.getCreatedCount();
            //还可发放张数
            canSendAmount = couponThemeCache.getCreatedCount() - couponThemeCache.getSendedCount();
            //已生成张数
            drawedCoupons = couponThemeCache.getCreatedCount();
        }

        if (Objects.equals(CouponTypeEnum.COUPON_TYPE_VIRTUAL.getType(), couponThemeResponse.getCouponType()) || Objects.equals(CouponTypeEnum.COUPON_TYPE_REDENVELOPE.getType(), couponThemeResponse.getCouponType())) {
            //还可发放张数
            canSendAmount = couponThemeCache.getTotalCount() - couponThemeCache.getSendedCount();
        }

        couponThemeResponse.setCanSendAmount(Math.max(canSendAmount, 0));
        couponThemeResponse.setAvailableCoupons(Math.max(availableCoupons, 0));
        //发行总张数
        couponThemeResponse.setTotalLimit(couponThemeCache.getTotalCount());

        //已生成张数
        couponThemeResponse.setDrawedCoupons(drawedCoupons);

        //已发放张数
        couponThemeResponse.setSendedCouopns(couponThemeCache.getSendedCount());
    }


}