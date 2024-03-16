package com.fcb.coupon.app.business.impl;

import com.alibaba.fastjson.JSON;
import com.fcb.coupon.app.business.CouponUserBusiness;
import com.fcb.coupon.app.exception.CouponErrorCode;
import com.fcb.coupon.app.model.PageDto;
import com.fcb.coupon.app.model.bo.CouponUserGetBo;
import com.fcb.coupon.app.model.bo.CouponUserListBo;
import com.fcb.coupon.app.model.dto.CouponThemeCache;
import com.fcb.coupon.app.model.entity.CouponEntity;
import com.fcb.coupon.app.model.entity.CouponGiveEntity;
import com.fcb.coupon.app.model.entity.CouponUserEntity;
import com.fcb.coupon.app.model.extension.CouponThemeCacheExtension;
import com.fcb.coupon.app.model.param.response.CouponDetailResponse;
import com.fcb.coupon.app.model.param.response.CouponListResponse;
import com.fcb.coupon.app.model.param.response.CouponUserEffectiveSoaResponse;
import com.fcb.coupon.app.model.param.response.PageResponse;
import com.fcb.coupon.app.remote.dto.BrokerInfoDto;
import com.fcb.coupon.app.remote.dto.input.BrokerInfoByUnionIdInput;
import com.fcb.coupon.app.remote.user.MemberFeignClient;
import com.fcb.coupon.app.service.*;
import com.fcb.coupon.common.constant.CouponConstant;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.enums.CouponSourceTypeEnum;
import com.fcb.coupon.common.enums.CouponStatusEnum;
import com.fcb.coupon.common.enums.UserTypeEnum;
import com.fcb.coupon.common.enums.YesNoEnum;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.exception.CommonErrorCode;
import com.fcb.coupon.common.util.AESPromotionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月27日 17:18:00
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
public class CouponUserBusinessImpl implements CouponUserBusiness {

    private final CouponUserService couponUserService;
    private final CouponService couponService;
    private final CouponThemeCacheService couponThemeCacheService;
    private final UserFacadeService userFacadeService;
    private final CouponBeforeGiveService couponBeforeGiveService;
    private final MemberFeignClient memberFeignClient;
    private final CouponGiveService couponGiveService;


    @Override
    public CouponDetailResponse getByCouponId(CouponUserGetBo bo) {
        CouponUserEntity couponUserEntity = couponUserService.get(bo.getUserId(), bo.getUserType(), bo.getCouponId());
        if (couponUserEntity == null) {
            return null;
        }

        CouponDetailResponse couponDetailResponse = new CouponDetailResponse();
        //初始化字段
        couponDetailResponse.setCanAssign(false);
        couponDetailResponse.setCanSendMsg(false);

        couponDetailResponse.setBindTime(couponUserEntity.getCreateTime());
        couponDetailResponse.setBindUserType(UserTypeEnum.getStrByUserType(couponUserEntity.getUserType()));
        couponDetailResponse.setCouponId(couponUserEntity.getCouponId());
        couponDetailResponse.setStatus(couponUserEntity.getStatus());
        couponDetailResponse.setThemeId(couponUserEntity.getCouponThemeId());

        //查询券活动
        CouponThemeCache couponThemeCache = couponThemeCacheService.getById(couponDetailResponse.getThemeId());
        if (couponThemeCache == null) {
            log.warn("券活动数据不存在，需要检查脏数据: themeId={}", couponDetailResponse.getThemeId());
            return couponDetailResponse;
        }

        CouponThemeCacheExtension couponThemeCacheExtension = new CouponThemeCacheExtension(couponThemeCache);

        couponDetailResponse.setCanDonation(YesNoEnum.YES.getValue().equals(couponThemeCache.getCanDonation()));
        couponDetailResponse.setCouponDiscountType(couponThemeCache.getCouponDiscountType());
        couponDetailResponse.setCouponType(couponThemeCache.getCouponType());
        couponDetailResponse.setCouponValue(couponThemeCacheExtension.getCouponValue());
        couponDetailResponse.setThemeDesc(couponThemeCache.getThemeDesc());
        couponDetailResponse.setThemeTitle(couponThemeCache.getThemeTitle());
        couponDetailResponse.setThemeType(couponThemeCache.getThemeType());
        couponDetailResponse.setUseRuleRemark(couponThemeCacheExtension.getUseRuleRemark());
        couponDetailResponse.setCrowdScopeIds(couponThemeCacheExtension.getCrowdScopeIds());

        CouponEntity couponEntity = couponService.getById(couponUserEntity.getCouponId());
        if (couponEntity == null) {
            log.warn("券数据不存在，需要检查脏数据: couponId={}", couponUserEntity.getCouponId());
            return couponDetailResponse;
        }

        couponDetailResponse.setCouponCode(AESPromotionUtil.decrypt(couponEntity.getCouponCode()));
        couponDetailResponse.setStartTime(couponEntity.getStartTime());
        couponDetailResponse.setEndTime(couponEntity.getEndTime());
        couponDetailResponse.setSource(couponEntity.getSource());
        couponDetailResponse.setSourceStr(getSourceStr(couponEntity));

        //状态特殊处理
        if (CouponStatusEnum.STATUS_USE.getStatus().equals(couponDetailResponse.getStatus())) {
            // 查询券是否转赠中 beforeGiveCount>0表示转赠中
            int beforeGiveCount = couponBeforeGiveService.getBeforeGiveCount(couponDetailResponse.getCouponId());
            if (beforeGiveCount > 0) {
                couponDetailResponse.setStatus(CouponStatusEnum.STATUS_BEFORE_DONATE.getStatus());
            }
            // 过期券转失效
            if (couponDetailResponse.getEndTime().before(new Date())) {
                couponDetailResponse.setStatus(CouponStatusEnum.STATUS_INVALID.getStatus());
            }
        }
        // “已上锁”状态转换为“已使用”
        if (CouponStatusEnum.STATUS_LOCKED.getStatus().equals(couponDetailResponse.getStatus())) {
            couponDetailResponse.setStatus(CouponStatusEnum.STATUS_USED.getStatus());
        }

        return couponDetailResponse;
    }


    @Override
    public PageResponse<CouponListResponse> listByEffective(CouponUserListBo queryBo, PageDto pageDto) {
        //检查参数
        if (StringUtils.isEmpty(queryBo.getUserId()) || queryBo.getUserType() == null) {
            throw new BusinessException(CommonErrorCode.PARAMS_ERROR);
        }

        Integer total = couponUserService.countByEffective(queryBo);
        if (total == 0) {
            return new PageResponse(Collections.EMPTY_LIST, 0);
        }

        //查询我的优惠券列表
        List<CouponUserEntity> couponUserEntities = couponUserService.listByEffective(queryBo, pageDto);
        if (CollectionUtils.isEmpty(couponUserEntities)) {
            return new PageResponse(Collections.EMPTY_LIST, total);
        }
        List<CouponListResponse> couponListResponses = populateCouponResponses(couponUserEntities);
        return new PageResponse(couponListResponses, total);
    }


    @Override
    public List<CouponUserEffectiveSoaResponse> listByEffectiveAndUnionIds(List<String> unionIdList) {
        List<CouponUserEffectiveSoaResponse> responses = new ArrayList<>();
        //遍历查询用户可用优惠券
        for (String unionId : unionIdList) {
            BrokerInfoDto brokerInfoDto = getBrokerInfo(unionId);
            if (brokerInfoDto == null) {
                throw new BusinessException(CouponErrorCode.QUERY_COUPON_USER_NONE_EXCEPTION);
            }
            CouponUserListBo couponUserListBo = new CouponUserListBo();
            couponUserListBo.setUserId(brokerInfoDto.getBrokerId());
            couponUserListBo.setUserType(UserTypeEnum.B.getUserType());
            couponUserListBo.setStatusList(Arrays.asList(CouponStatusEnum.STATUS_USE.getStatus(), CouponStatusEnum.STATUS_FREEZE.getStatus()));
            //最多查询100条
            PageDto pageDto = new PageDto(1, 100);
            List<CouponUserEntity> couponUserEntities = couponUserService.listByEffective(couponUserListBo, pageDto);
            List<CouponUserEffectiveSoaResponse> soaResponses = populateCouponSoaResponses(couponUserEntities, unionId);
            responses.addAll(soaResponses);
        }
        return responses;
    }


    private BrokerInfoDto getBrokerInfo(String unionId) {
        BrokerInfoByUnionIdInput param = new BrokerInfoByUnionIdInput();
        param.setUnionId(unionId);
        ResponseDto<BrokerInfoDto> brokerInfoDtoResponseDto = memberFeignClient.findBrokerInfoByUnionId(param);
        if (!CouponConstant.SUCCESS_CODE.equals(brokerInfoDtoResponseDto.getCode())) {
            throw new BusinessException(CommonErrorCode.API_CALL_ERROR.getCode(), brokerInfoDtoResponseDto.getMessage());
        }
        return brokerInfoDtoResponseDto.getData();
    }


    @Override
    public PageResponse<CouponListResponse> listByExpired(CouponUserListBo queryBo, PageDto pageDto) {
        //检查参数
        if (StringUtils.isEmpty(queryBo.getUserId()) || queryBo.getUserType() == null) {
            throw new BusinessException(CommonErrorCode.PARAMS_ERROR);
        }

        Integer total = couponUserService.countByExpired(queryBo);
        if (total == 0) {
            return new PageResponse(Collections.EMPTY_LIST, 0);
        }

        //查询我的优惠券列表
        List<CouponUserEntity> couponUserEntities = couponUserService.listByExpired(queryBo, pageDto);
        if (CollectionUtils.isEmpty(couponUserEntities)) {
            return new PageResponse(Collections.EMPTY_LIST, total);
        }
        List<CouponListResponse> couponListResponses = populateCouponResponses(couponUserEntities);
        return new PageResponse(couponListResponses, total);
    }


    private List<CouponListResponse> populateCouponResponses(List<CouponUserEntity> couponUserEntities) {
        //查询券数据
        List<Long> couponIds = couponUserEntities.stream().map(m -> m.getCouponId()).collect(Collectors.toList());
        List<CouponEntity> couponEntities = couponService.listByIds(couponIds);
        if (CollectionUtils.isEmpty(couponEntities)) {
            log.warn("券数据不存在，需要检查脏数据: couponIds={}", JSON.toJSONString(couponIds));
            return Collections.EMPTY_LIST;
        }
        Map<Long, CouponEntity> couponEntityMap = couponEntities.stream().collect(Collectors.toMap(m -> m.getId(), m -> m));

        //查询券活动数据
        Set<Long> themeIdSet = couponUserEntities.stream().map(m -> m.getCouponThemeId()).collect(Collectors.toSet());
        List<CouponThemeCache> couponThemeCaches = couponThemeCacheService.listCouponTheme(new ArrayList<>(themeIdSet));
        Map<Long, CouponThemeCache> couponThemeCacheMap = couponThemeCaches.stream().collect(Collectors.toMap(m -> m.getId(), m -> m));

        //构造结果
        List<CouponListResponse> couponListResponses = new ArrayList<>(couponUserEntities.size());
        for (CouponUserEntity couponUserEntity : couponUserEntities) {
            CouponEntity couponEntity = couponEntityMap.get(couponUserEntity.getCouponId());
            if (couponEntity == null) {
                log.warn("券数据不存在，需要检查脏数据:couponId={}", couponUserEntity.getCouponId());
                continue;
            }

            //构造券信息
            CouponListResponse couponListResponse = new CouponListResponse();
            couponListResponses.add(couponListResponse);
            couponListResponse.setCouponId(couponEntity.getId());
            couponListResponse.setCouponCode(couponEntity.getCouponCode());
            couponListResponse.setThemeTitle(couponEntity.getThemeTitle());
            couponListResponse.setStartTime(couponEntity.getStartTime());
            couponListResponse.setEndTime(couponEntity.getEndTime());
            couponListResponse.setStatus(couponEntity.getStatus());
            couponListResponse.setBindTime(couponUserEntity.getCreateTime());
            couponListResponse.setCouponType(couponEntity.getCouponType());

            //券活动信息
            CouponThemeCache couponThemeCache = couponThemeCacheMap.get(couponUserEntity.getCouponThemeId());
            if (couponThemeCache == null) {
                log.warn("券活动缓存数据未获取成功,couponThmemeId={}", couponUserEntity.getCouponThemeId());
                continue;
            }

            CouponThemeCacheExtension couponThemeCacheExtension = new CouponThemeCacheExtension(couponThemeCache);

            couponListResponse.setThemeType(couponThemeCache.getThemeType());
            couponListResponse.setCouponDiscountType(couponThemeCache.getCouponDiscountType());
            couponListResponse.setCouponValue(couponThemeCacheExtension.getCouponValue());
            couponListResponse.setUseRuleRemark(couponThemeCacheExtension.getUseRuleRemark());
        }

        return couponListResponses;
    }


    private List<CouponUserEffectiveSoaResponse> populateCouponSoaResponses(List<CouponUserEntity> couponUserEntities, String unionId) {
        List<CouponUserEffectiveSoaResponse> responses = new ArrayList<>(couponUserEntities.size());
        for (CouponUserEntity couponUserEntity : couponUserEntities) {
            CouponUserEffectiveSoaResponse soaResponse = new CouponUserEffectiveSoaResponse();
            soaResponse.setCouponId(couponUserEntity.getCouponId());
            soaResponse.setCouponThemeId(couponUserEntity.getCouponThemeId());
            soaResponse.setUnionId(unionId);
            responses.add(soaResponse);
        }
        return responses;
    }


    /**
     * 获取来源名称
     */
    private String getSourceStr(CouponEntity couponEntity) {
        //非转赠
        if (!Objects.equals(couponEntity.getSource(), CouponSourceTypeEnum.COUPON_SOURCE_PRESENTED.getSource())) {
            return CouponSourceTypeEnum.getStrBySource(couponEntity.getSource());
        }
        //查询转赠信息
        CouponGiveEntity couponGiveEntity = couponGiveService.getById(couponEntity.getId());
        if (couponGiveEntity == null) {
            return "";
        }
        String giveUserName = couponGiveEntity.getGiveUserName();
        if (StringUtils.isNotBlank(giveUserName)) {
            String idName = StringUtils.rightPad(StringUtils.left(giveUserName, 1), StringUtils.length(giveUserName), "*");
            return "会员" + idName + "转赠";
        } else {
            String phoneStr = StringUtils.left(couponGiveEntity.getGiveUserMobile(), 3) + "****" + StringUtils.right(couponGiveEntity.getGiveUserMobile(), 4);
            return "会员" + phoneStr + "转赠";
        }
    }

}
