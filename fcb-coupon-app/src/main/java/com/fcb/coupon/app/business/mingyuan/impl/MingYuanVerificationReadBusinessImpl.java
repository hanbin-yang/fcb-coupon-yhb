package com.fcb.coupon.app.business.mingyuan.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fcb.coupon.app.business.mingyuan.MingYuanVerificationReadBusiness;
import com.fcb.coupon.app.exception.Coupon4OrderErrorCode;
import com.fcb.coupon.app.exception.CouponErrorCode;
import com.fcb.coupon.app.facade.ClientUserFacade;
import com.fcb.coupon.app.model.bo.CouponMingyuanBo;
import com.fcb.coupon.app.model.dto.CouponThemeCache;
import com.fcb.coupon.app.model.query.LambdaFieldNameSelector;
import com.fcb.coupon.app.model.dto.OperateCouponDto;
import com.fcb.coupon.app.model.entity.CouponEntity;
import com.fcb.coupon.app.model.entity.CouponUserEntity;
import com.fcb.coupon.app.model.entity.CouponVerificationEntity;
import com.fcb.coupon.app.model.bo.CheckCouponUsefulBo;
import com.fcb.coupon.app.model.bo.QueryUsefulCouponBo;
import com.fcb.coupon.app.model.param.response.CheckCouponUsefulResponse;
import com.fcb.coupon.app.model.param.response.CouponMingyuanResponse;
import com.fcb.coupon.app.model.param.response.QueryUsefulCouponResponse;
import com.fcb.coupon.app.remote.activity.FcbActivityFeignClient;
import com.fcb.coupon.app.remote.building.BuildingFeignClient;
import com.fcb.coupon.app.remote.dto.input.BuildingListByItemIdInput;
import com.fcb.coupon.app.remote.dto.input.PromotionCheckResInput;
import com.fcb.coupon.app.remote.dto.output.PromotionCheckResOutput;
import com.fcb.coupon.app.remote.dto.output.StoreInfoOutput;
import com.fcb.coupon.app.service.*;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.enums.*;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.util.AESPromotionUtil;
import com.fcb.coupon.common.util.CommonResponseUtil;
import jodd.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author mashiqiong
 * @date 2021-08-24 10:11
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@RefreshScope
public class MingYuanVerificationReadBusinessImpl implements MingYuanVerificationReadBusiness {
    private final CouponThemeCacheService couponThemeCacheService;
    private final CouponVerificationService couponVerificationService;
    private final MktUseRuleService mktUseRuleService;
    private final CouponService couponService;
    private final FcbActivityFeignClient fcbActivityFeignClient;
    private final BuildingFeignClient buildingFeignClient;
    private final ClientUserFacade clientUserFacade;
    private final CouponUserService couponUserService;

    @Value("${mingyuan.user.type:0}")
    private Integer mingyuanUserType;


    @Override
    public List<CouponMingyuanResponse> getMingyuanCouponListByIds(CouponMingyuanBo bo) {
        if (CollectionUtils.isEmpty(bo.getCouponIds())) {
            throw new BusinessException(CouponErrorCode.COUPON_IDS_REQUIRED);
        }

        List<CouponEntity> couponList = couponService.listByIds(bo.getCouponIds());
        if (CollectionUtils.isEmpty(couponList)) {
            log.info("getMingyuanCouponListByIds 条件查询券信息列表返回空数据 param={}", JSON.toJSONString(bo));
            return null;
        }

        // 券用户
        LambdaQueryWrapper<CouponUserEntity> queryUserWrapper = Wrappers.lambdaQuery(CouponUserEntity.class);
        queryUserWrapper.select(CouponUserEntity::getBindTel, CouponUserEntity::getCouponId).in(CouponUserEntity::getCouponId,bo.getCouponIds());
        List<CouponUserEntity> userList = couponUserService.list(queryUserWrapper);
        Map<Long,CouponUserEntity> userMap = userList.stream().collect(Collectors.toMap(CouponUserEntity::getCouponId, Function.identity()));

        // 券核销
        LambdaQueryWrapper<CouponVerificationEntity> queryVerificationWrapper = Wrappers.lambdaQuery(CouponVerificationEntity.class);
        queryVerificationWrapper.select(CouponVerificationEntity::getOrderCode,
                                        CouponVerificationEntity::getProductCode,
                                        CouponVerificationEntity::getCouponId)
                .in(CouponVerificationEntity::getCouponId,bo.getCouponIds());
        List<CouponVerificationEntity> verificationList = couponVerificationService.list(queryVerificationWrapper);
        Map<Long,CouponVerificationEntity> verificationMap = verificationList.stream().collect(Collectors.toMap(CouponVerificationEntity::getCouponId, Function.identity()));

        List<CouponMingyuanResponse> resultList = new ArrayList<>();
        couponList.forEach(vo -> {
            CouponMingyuanResponse dto = new CouponMingyuanResponse();
            dto.setCouponId(vo.getId());
            dto.setCouponCode(AESPromotionUtil.decrypt(vo.getCouponCode()));
            if (System.currentTimeMillis() > vo.getEndTime().getTime() && CouponStatusEnum.STATUS_USE.getStatus().equals(vo.getStatus())) {
                dto.setStatus(CouponStatusEnum.STATUS_INVALID.getStatus());
            } else {
                dto.setStatus(vo.getStatus());
            }

            CouponThemeCache couponThemeCache = couponThemeCacheService.getById(vo.getCouponThemeId());
            dto.setCouponDiscountType(couponThemeCache.getCouponDiscountType());
            dto.setCouponValue(couponThemeCache.getDiscountAmount());

            if (userMap.containsKey(vo.getId())) {
                dto.setPhone(userMap.get(vo.getId()).getBindTel());
            }

            if (verificationMap.containsKey(vo.getId())) {
                CouponVerificationEntity verification = verificationMap.get(vo.getId());
                dto.setTransactionId(verification.getOrderCode());
                dto.setRoomGuid(verification.getProductCode());
            }

            resultList.add(dto);
        });
        return resultList;
    }


    @Override
    public List<QueryUsefulCouponResponse> queryCouponList4Order(QueryUsefulCouponBo bo) {
        List<QueryUsefulCouponResponse> resultList = new ArrayList<>();
        log.info("明源核销，B端/C端 切换，userType:{}", mingyuanUserType);
        Long userId = getUserId(bo.getPhone(), mingyuanUserType);

        //校验是否可叠加优惠券（促销活动）
        PromotionCheckResInput promotionCheckResInput = new PromotionCheckResInput();
        promotionCheckResInput.setRoomGuid(bo.getRoomGuid());
        ResponseDto<PromotionCheckResOutput> promotionInfo = fcbActivityFeignClient.queryActivityInfoByCheck(promotionCheckResInput, ClientTypeEnum.C.getKey());
        if (Objects.nonNull(promotionInfo.getData()) && Objects.equals(promotionInfo.getData().getCanUseCoupon(), 0)) {
            return resultList;
        }

        // 校验楼盘上下架
        // ---调用楼盘接口查询
        BuildingListByItemIdInput buildingListByItemIdInput = new BuildingListByItemIdInput();
        buildingListByItemIdInput.setMingYuanCode(bo.getItemId());
        ResponseDto<List<StoreInfoOutput>> responseDto = buildingFeignClient.queryBuildingInfoByItemId(buildingListByItemIdInput);
        List<StoreInfoOutput> storeInfoOutputList = responseDto.getData();
        if (CollectionUtils.isEmpty(storeInfoOutputList)) {
            return resultList;
        }

        log.info(" 根据手机号:{},roomGuid:{},transactionId:{},ItemId:{},获取可使用优惠券信息,校验楼盘上下架,storeInfoOutputListSize:{},storeInfoOutputList:{}", bo.getPhone(), bo.getRoomGuid(), bo.getTransactionId(), bo.getItemId(), storeInfoOutputList.size(), storeInfoOutputList);

        //查库拿优惠券
        LambdaQueryWrapper<CouponUserEntity> queryWrapper = Wrappers.lambdaQuery(CouponUserEntity.class);
        queryWrapper.in(CouponUserEntity::getStatus, Arrays.asList(
                CouponStatusEnum.STATUS_USE.getStatus(), CouponStatusEnum.STATUS_USED.getStatus(),
                CouponStatusEnum.STATUS_LOCKED.getStatus()))
                .eq(CouponUserEntity::getUserId, userId);

        List<CouponUserEntity> couponUserEntityList = couponUserService.list(queryWrapper);
        log.info(" 根据手机号:{},roomGuid:{},transactionId:{},获取可使用优惠券信息,couponPOListSize:{}", bo.getPhone(), bo.getRoomGuid(), bo.getTransactionId(), couponUserEntityList.size());
        //没有查询到券
        if (CollectionUtils.isEmpty(couponUserEntityList)) {
            return resultList;
        }

        List<Long> inputAllCouponIds = couponUserEntityList.stream().map(m->m.getCouponId()).distinct().collect(Collectors.toList());
        List<CouponVerificationEntity> dbCouponVerificationList = couponVerificationService.listByIds(inputAllCouponIds);
        Map<Long, CouponVerificationEntity> couponVerificationEntityMap = dbCouponVerificationList.stream().collect(Collectors.toMap(CouponVerificationEntity::getCouponId, Function.identity(), (a, b) -> a));

        List<CouponEntity> couponEntitys = couponService.listByIds(inputAllCouponIds);

        for(CouponEntity couponEntity : couponEntitys) {

            // 从缓存拿券活动信息
            CouponThemeCache couponThemeCache = getCouponThemeCache(couponEntity.getCouponThemeId());
            if(couponThemeCache == null) {
                continue;
            }

            //只有电子券才可以核销
            if (!Objects.equals(couponThemeCache.getCouponType(), CouponTypeEnum.COUPON_TYPE_VIRTUAL.getType())) {
                continue;
            }

            //如果券已使用，且旧交易id、新交易id都没有传过来
            if(CouponStatusEnum.STATUS_USED.getStatus().equals(couponEntity.getStatus()) && StringUtil.isBlank(bo.getOldTransactionId()) && StringUtil.isBlank(bo.getTransactionId()) ) {
                continue;
            }

            //如果券已锁定，且旧交易id、新交易id都没有传过来
            if(CouponStatusEnum.STATUS_LOCKED.getStatus().equals(couponEntity.getStatus()) && StringUtil.isBlank(bo.getOldTransactionId()) && StringUtil.isBlank(bo.getTransactionId()) ) {
                continue;
            }

            CouponVerificationEntity couponVerificationEntity = couponVerificationEntityMap.get(couponEntity.getId());
            //比较旧交易id是否相同
            if (!checkTransactionIdIsEqual(bo, couponEntity, couponVerificationEntity)){
                continue;
            }

            //从明细里拿时间比较
            Date nowDate = new Date();
            // 比较有效期是否开始
            if (CouponStatusEnum.STATUS_USE.getStatus().equals(couponEntity.getStatus()) && couponEntity.getStartTime() != null && couponEntity.getStartTime().compareTo(nowDate) > 0) {
                continue;
            }

            // 比较时间是否过期
            if (CouponStatusEnum.STATUS_USE.getStatus().equals(couponEntity.getStatus()) && couponEntity.getEndTime() != null && couponEntity.getEndTime().compareTo(nowDate) < 0) {
                continue;
            }

            // 非平台券才去校验适用楼盘
            if (!Objects.equals(couponThemeCache.getThemeType(), OrgLevelEnum.PLATFORM.getThemeType())) {
                //从缓存或者数据库中拿券的适用楼盘
                final Set<Long> matchStoreIds = mktUseRuleService.getApplicableStoreIds(couponEntity.getCouponThemeId());
                if (CollectionUtils.isEmpty(matchStoreIds)){
                    continue;
                }
                log.info(" 根据手机号:{},roomGuid:{},transactionId:{},ItemId:{},获取可使用优惠券信息,比对项目的楼盘与券的楼盘,matchStoreIdsSize:{},matchStoreIds={}", bo.getPhone(), bo.getRoomGuid(), bo.getTransactionId(), bo.getItemId(), matchStoreIds.size(), matchStoreIds);
                if(storeInfoOutputList.stream().noneMatch(m->matchStoreIds.contains(Long.valueOf(m.getStoreId())))) {
                    continue;
                }
            }

            //是否C端用户可用
            if(!UserTypeEnum.C.getUserType().equals(couponEntity.getUserType()) && Objects.equals(UserTypeEnum.C.getUserType(), mingyuanUserType)) {
                continue;
            }

            //是否B端用户可用
            if(!UserTypeEnum.B.getUserType().equals(couponEntity.getUserType()) && Objects.equals(UserTypeEnum.B.getUserType(), mingyuanUserType)) {
                continue;
            }

            QueryUsefulCouponResponse queryUsefulCouponResponse = createQueryUsefulCouponResponse(couponEntity, couponThemeCache, couponVerificationEntity);
            resultList.add(queryUsefulCouponResponse);
        }

        return resultList;
    }

    /**
     * 明源 操作优惠券校验
     * @param bo
     * @return
     */
    @Override
    public ResponseDto<List<CheckCouponUsefulResponse>> validateCoupons4OrderForQuery(CheckCouponUsefulBo bo) {
        log.info("明源核销，B端/C端 切换，userType:{}", mingyuanUserType);
        // 根据手机号获取用户id
        Long userId = getUserId(bo.getPhone(), mingyuanUserType);

        MultiValueMap<Coupon4OrderErrorCode, CheckCouponUsefulResponse> validateErrorMap = new LinkedMultiValueMap<>();

        //查库拿优惠券
        LambdaQueryWrapper<CouponEntity> queryWrapper = Wrappers.lambdaQuery(CouponEntity.class);
        queryWrapper.in(CouponEntity::getId, bo.getCheckCoupons().stream().map(OperateCouponDto::getCouponId).collect(Collectors.toList())).eq(CouponEntity::getIsDeleted, YesNoEnum.NO.getValue());

        List<CouponEntity> couponEntityList = couponService.list(queryWrapper);
        log.info(" 根据手机号:{},roomGuid:{},transactionId:{},获取可使用优惠券信息,couponPOListSize:{}", bo.getPhone(), bo.getRoomGuid(), bo.getTransactionId(), couponEntityList.size());
        //没有查询到券
        if (CollectionUtils.isEmpty(couponEntityList)) {
            return CommonResponseUtil.fail(Coupon4OrderErrorCode.NO_REAL_COUPON);
        }

        //校验是否可叠加优惠券（促销活动）
        PromotionCheckResInput promotionCheckResInput = new PromotionCheckResInput();
        promotionCheckResInput.setRoomGuid(bo.getRoomGuid());
        ResponseDto<PromotionCheckResOutput> promotionInfo = fcbActivityFeignClient.queryActivityInfoByCheck(promotionCheckResInput, ClientTypeEnum.C.getKey());

        if (Objects.nonNull(promotionInfo.getData()) && Objects.equals(promotionInfo.getData().getCanUseCoupon(), 0)) {
            for(CouponEntity entity : couponEntityList) {
                CheckCouponUsefulResponse vo = createCheckCouponUsefulResponse(entity, Coupon4OrderErrorCode.HAS_PROMOTION);
                log.error("操作优惠券校验error: errorCode:{},errorMessage={}, dbCoupon={}", Coupon4OrderErrorCode.HAS_PROMOTION.getCode(), Coupon4OrderErrorCode.HAS_PROMOTION.getMessage(), JSON.toJSONString(entity));
                validateErrorMap.add(Coupon4OrderErrorCode.HAS_PROMOTION, vo);
            }

            return CommonResponseUtil.fail(Coupon4OrderErrorCode.FAIL, validateErrorMap.get(Coupon4OrderErrorCode.HAS_PROMOTION));
        }

        // 校验楼盘上下架
        // ---调用楼盘接口查询
        BuildingListByItemIdInput buildingListByItemIdInput = new BuildingListByItemIdInput();
        buildingListByItemIdInput.setMingYuanCode(bo.getItemId());
        ResponseDto<List<StoreInfoOutput>> responseDto = buildingFeignClient.queryBuildingInfoByItemId(buildingListByItemIdInput);
        List<StoreInfoOutput> storeInfoOutputList = responseDto.getData();
        if (CollectionUtils.isEmpty(storeInfoOutputList)) {
            for(CouponEntity entity : couponEntityList) {
                CheckCouponUsefulResponse vo = createCheckCouponUsefulResponse(entity, Coupon4OrderErrorCode.NO_BUILDING);
                log.error("操作优惠券校验error: errorCode:{},errorMessage={}, dbCoupon={}", Coupon4OrderErrorCode.NO_BUILDING.getCode(), Coupon4OrderErrorCode.NO_BUILDING.getMessage(), JSON.toJSONString(entity));
                validateErrorMap.add(Coupon4OrderErrorCode.NO_BUILDING, vo);
            }
            return CommonResponseUtil.fail(Coupon4OrderErrorCode.FAIL, validateErrorMap.get(Coupon4OrderErrorCode.NO_BUILDING));
        }

        log.info(" 根据手机号:{},roomGuid:{},transactionId:{},ItemId:{},获取可使用优惠券信息,校验楼盘上下架,storeInfoOutputListSize:{},storeInfoOutputList:{}", bo.getPhone(), bo.getRoomGuid(), bo.getTransactionId(), bo.getItemId(), storeInfoOutputList.size(), storeInfoOutputList);

        checkBusinessValid(userId, bo, validateErrorMap, couponEntityList, storeInfoOutputList);

        //数据库中不存在的券，也要返回提示信息
        List<Long> inputIdList = bo.getCheckCoupons().stream().map(OperateCouponDto::getCouponId).collect(Collectors.toList());
        List<Long> dbIdList = couponEntityList.stream().map(CouponEntity::getId).collect(Collectors.toList());
        inputIdList.removeAll(dbIdList);
        if(CollectionUtils.isNotEmpty(inputIdList)) {
            inputIdList.forEach(item->{
                CheckCouponUsefulResponse vo = new CheckCouponUsefulResponse();
                vo.setCouponId(item);
                vo.setErrorCode(Coupon4OrderErrorCode.COUPON_NOT_EXIST.getCode());
                vo.setErrorMessage(Coupon4OrderErrorCode.COUPON_NOT_EXIST.getMessage());
                validateErrorMap.add(Coupon4OrderErrorCode.COUPON_NOT_EXIST, vo);
            });
            log.error("操作优惠券校验error: errorCode:{},errorMessage={}, inputIdList={}", Coupon4OrderErrorCode.COUPON_NOT_EXIST.getCode(), Coupon4OrderErrorCode.COUPON_NOT_EXIST.getMessage(), JSON.toJSONString(inputIdList));
        }

        List<CheckCouponUsefulResponse> data = new ArrayList<>();
        if (MapUtils.isNotEmpty(validateErrorMap)) {
            for (Coupon4OrderErrorCode key : validateErrorMap.keySet()) {
                data.addAll(validateErrorMap.get(key));
            }
        }

        if(CollectionUtils.isNotEmpty(data)) {
            return CommonResponseUtil.fail(Coupon4OrderErrorCode.FAIL, data);
        }

        return CommonResponseUtil.success();
    }

    /**
     * 进一步校验业务逻辑合法性
     * @param bo
     * @param validateErrorMap
     * @param couponEntityList
     * @param storeInfoOutputList
     */
    private void checkBusinessValid(Long userId, CheckCouponUsefulBo bo,
                                    MultiValueMap<Coupon4OrderErrorCode, CheckCouponUsefulResponse> validateErrorMap, List<CouponEntity> couponEntityList, List<StoreInfoOutput> storeInfoOutputList) {
        //从缓存中拿券主题
        List<Long> couponThemeIds = couponEntityList.stream().map(m -> m.getCouponThemeId()).distinct().collect(Collectors.toList());

        //用于统计符合条件的券数量，方便与订单使用券数量限制数做对比
        Map<Long, Integer> countMap = new HashMap<>();
        for(Long couponThemeId : couponThemeIds) {
            if (!StringUtil.isBlank(bo.getTransactionId())) {
                LambdaQueryWrapper<CouponVerificationEntity> queryWrapper = Wrappers.lambdaQuery(CouponVerificationEntity.class);
                queryWrapper.notIn(CouponVerificationEntity::getCouponId, bo.getCheckCoupons().stream().map(OperateCouponDto::getCouponId).collect(Collectors.toList())).eq(CouponVerificationEntity::getBindUserId, userId).eq(CouponVerificationEntity::getCouponThemeId, couponThemeId).eq(CouponVerificationEntity::getOrderCode, bo.getTransactionId());
                int count = couponVerificationService.count(queryWrapper);
                countMap.put(couponThemeId, count);
            } else {
                countMap.put(couponThemeId, 0);
            }
        }
        List<Long> inputAllCouponIds = couponEntityList.stream().map(m->m.getId()).distinct().collect(Collectors.toList());
        List<CouponVerificationEntity> dbCouponVerificationList = couponVerificationService.listByIds(inputAllCouponIds);
        Map<Long, CouponVerificationEntity> couponVerificationEntityMap = dbCouponVerificationList.stream().collect(Collectors.toMap(CouponVerificationEntity::getCouponId, Function.identity(), (a, b) -> a));

        for(CouponEntity couponEntity : couponEntityList) {
            //校验券是否在有效状态
            if(!Arrays.asList(CouponStatusEnum.STATUS_USE.getStatus(), CouponStatusEnum.STATUS_USED.getStatus(), CouponStatusEnum.STATUS_LOCKED.getStatus()).contains(couponEntity.getStatus())) {
                CheckCouponUsefulResponse vo = createCheckCouponUsefulResponse(couponEntity, Coupon4OrderErrorCode.NOT_CAN_USE_STATUS);
                log.error("操作优惠券校验error: errorCode:{},errorMessage={}, dbCoupon={}", Coupon4OrderErrorCode.NOT_CAN_USE_STATUS.getCode(), Coupon4OrderErrorCode.NOT_CAN_USE_STATUS.getMessage(), JSON.toJSONString(couponEntity));
                validateErrorMap.add(Coupon4OrderErrorCode.NOT_CAN_USE_STATUS, vo);
                continue;
            }

            //校验券有效期
            Date nowDate = new Date();
            // 比较有效期是否开始
            if (CouponStatusEnum.STATUS_USE.getStatus().equals(couponEntity.getStatus()) && couponEntity.getStartTime() != null && couponEntity.getStartTime().compareTo(nowDate) > 0) {
                CheckCouponUsefulResponse vo = createCheckCouponUsefulResponse(couponEntity, Coupon4OrderErrorCode.EXPIRED);
                log.error("操作优惠券校验error: errorCode:{},errorMessage={}, dbCoupon={}", Coupon4OrderErrorCode.EXPIRED.getCode(), Coupon4OrderErrorCode.EXPIRED.getMessage(), JSON.toJSONString(couponEntity));
                validateErrorMap.add(Coupon4OrderErrorCode.EXPIRED, vo);
                continue;
            }

            // 比较时间是否过期
            if (CouponStatusEnum.STATUS_USE.getStatus().equals(couponEntity.getStatus()) && couponEntity.getEndTime() != null && couponEntity.getEndTime().compareTo(nowDate) < 0) {
                CheckCouponUsefulResponse vo = createCheckCouponUsefulResponse(couponEntity, Coupon4OrderErrorCode.EXPIRED);
                log.error("操作优惠券校验error: errorCode:{},errorMessage={}, dbCoupon={}", Coupon4OrderErrorCode.EXPIRED.getCode(), Coupon4OrderErrorCode.EXPIRED.getMessage(), JSON.toJSONString(couponEntity));
                validateErrorMap.add(Coupon4OrderErrorCode.EXPIRED, vo);
                continue;
            }

            //校验证券手机号是否匹配
            //比较手机号开始
            if(!Objects.equals(Long.valueOf(couponEntity.getUserId()), userId)) {
                CheckCouponUsefulResponse vo = createCheckCouponUsefulResponse(couponEntity, Coupon4OrderErrorCode.PHONE_NOT_MATCH);
                log.error("操作优惠券校验error: errorCode:{},errorMessage={}, dbCoupon={}", Coupon4OrderErrorCode.PHONE_NOT_MATCH.getCode(), Coupon4OrderErrorCode.PHONE_NOT_MATCH.getMessage(), JSON.toJSONString(couponEntity));
                validateErrorMap.add(Coupon4OrderErrorCode.PHONE_NOT_MATCH, vo);
                continue;
            }

            CouponVerificationEntity couponVerificationEntity = couponVerificationEntityMap.get(couponEntity.getId());

            //校验证券交易id是否匹配
            //比较交易id开始
            if(Arrays.asList(CouponStatusEnum.STATUS_LOCKED.getStatus(), CouponStatusEnum.STATUS_USED.getStatus()).contains(couponEntity.getStatus()) && Objects.nonNull(couponVerificationEntity) && !Objects.equals(bo.getTransactionId(), couponVerificationEntity.getOrderCode())) {
                CheckCouponUsefulResponse vo = createCheckCouponUsefulResponse(couponEntity, Coupon4OrderErrorCode.ORDER_CODE_NOT_MATCH);
                log.error("操作优惠券校验error: errorCode:{},errorMessage={}, dbCoupon={}", Coupon4OrderErrorCode.ORDER_CODE_NOT_MATCH.getCode(), Coupon4OrderErrorCode.ORDER_CODE_NOT_MATCH.getMessage(), JSON.toJSONString(couponEntity));
                validateErrorMap.add(Coupon4OrderErrorCode.ORDER_CODE_NOT_MATCH, vo);
                continue;
            }

            CouponThemeCache couponThemeCache = getCouponThemeCache(couponEntity.getCouponThemeId());
            if(couponThemeCache == null) {
                CheckCouponUsefulResponse vo = createCheckCouponUsefulResponse(couponEntity, Coupon4OrderErrorCode.NOT_FOUND_COUPON_THEME);
                log.error("操作优惠券校验error: errorCode:{},errorMessage={}, dbCoupon={}", Coupon4OrderErrorCode.NOT_FOUND_COUPON_THEME.getCode(), Coupon4OrderErrorCode.NOT_FOUND_COUPON_THEME.getMessage(), JSON.toJSONString(couponEntity));
                validateErrorMap.add(Coupon4OrderErrorCode.NOT_FOUND_COUPON_THEME, vo);
                continue;
            }

            //只有电子券才可以核销
            if (!Objects.equals(couponThemeCache.getCouponType(), CouponTypeEnum.COUPON_TYPE_VIRTUAL.getType())) {
                CheckCouponUsefulResponse vo = createCheckCouponUsefulResponse(couponEntity, Coupon4OrderErrorCode.CANT_NOT_OPERATE_THIRD_COUPON);
                log.error("操作优惠券校验error: errorCode:{},errorMessage={}, dbCoupon={}", Coupon4OrderErrorCode.CANT_NOT_OPERATE_THIRD_COUPON.getCode(), Coupon4OrderErrorCode.CANT_NOT_OPERATE_THIRD_COUPON.getMessage(), JSON.toJSONString(couponEntity));
                validateErrorMap.add(Coupon4OrderErrorCode.CANT_NOT_OPERATE_THIRD_COUPON, vo);
                continue;
            }

            //校验适用楼盘
            // 非平台券才去校验适用楼盘
            if (!Objects.equals(couponThemeCache.getThemeType(), OrgLevelEnum.PLATFORM.getThemeType())) {
                Set<Long> matchStoreIds = mktUseRuleService.getApplicableStoreIds(couponEntity.getCouponThemeId());
                if (CollectionUtils.isEmpty(matchStoreIds)){
                    CheckCouponUsefulResponse vo = createCheckCouponUsefulResponse(couponEntity, Coupon4OrderErrorCode.NO_MATCH_BUILDING);
                    log.error("操作优惠券校验error: errorCode:{},errorMessage={}, dbCoupon={}", Coupon4OrderErrorCode.NO_MATCH_BUILDING.getCode(), Coupon4OrderErrorCode.NO_MATCH_BUILDING.getMessage(), JSON.toJSONString(couponEntity));
                    validateErrorMap.add(Coupon4OrderErrorCode.NO_MATCH_BUILDING, vo);
                    continue;
                }
                log.info(" 根据手机号:{},roomGuid:{},transactionId:{},ItemId:{},获取可使用优惠券信息,校验项目的楼盘与券的楼盘,matchStoreIdsSize:{},matchStoreIds={}", bo.getPhone(), bo.getRoomGuid(), bo.getTransactionId(), bo.getItemId(), matchStoreIds.size(), matchStoreIds);
                if(storeInfoOutputList.stream().noneMatch(m->matchStoreIds.contains(Long.valueOf(m.getStoreId())))) {
                    CheckCouponUsefulResponse vo = createCheckCouponUsefulResponse(couponEntity, Coupon4OrderErrorCode.NO_MATCH_BUILDING);
                    log.error("操作优惠券校验error: errorCode:{},errorMessage={}, dbCoupon={}", Coupon4OrderErrorCode.NO_MATCH_BUILDING.getCode(), Coupon4OrderErrorCode.NO_MATCH_BUILDING.getMessage(), JSON.toJSONString(couponEntity));
                    validateErrorMap.add(Coupon4OrderErrorCode.NO_MATCH_BUILDING, vo);
                    continue;
                }
            }

            //是否C端用户可用
            if(!Objects.equals(UserTypeEnum.C.getUserType(),couponEntity.getUserType()) && Objects.equals(UserTypeEnum.C.getUserType(), mingyuanUserType)) {
                CheckCouponUsefulResponse vo = createCheckCouponUsefulResponse(couponEntity, Coupon4OrderErrorCode.CROWD_TYPE_NOT_MATCH);
                log.error("操作优惠券校验error: errorCode:{},errorMessage={}, dbCoupon={}", Coupon4OrderErrorCode.CROWD_TYPE_NOT_MATCH.getCode(), Coupon4OrderErrorCode.CROWD_TYPE_NOT_MATCH.getMessage(), JSON.toJSONString(couponEntity));
                validateErrorMap.add(Coupon4OrderErrorCode.CROWD_TYPE_NOT_MATCH, vo);
                continue;
            }

            //是否B端用户可用
            if(!Objects.equals(UserTypeEnum.B.getUserType(), couponEntity.getUserType()) && Objects.equals(UserTypeEnum.B.getUserType(), mingyuanUserType)) {
                CheckCouponUsefulResponse vo = createCheckCouponUsefulResponse(couponEntity, Coupon4OrderErrorCode.CROWD_TYPE_NOT_MATCH);
                log.error("操作优惠券校验error: errorCode:{},errorMessage={}, dbCoupon={}", Coupon4OrderErrorCode.CROWD_TYPE_NOT_MATCH.getCode(), Coupon4OrderErrorCode.CROWD_TYPE_NOT_MATCH.getMessage(), JSON.toJSONString(couponEntity));
                validateErrorMap.add(Coupon4OrderErrorCode.CROWD_TYPE_NOT_MATCH, vo);
                continue;
            }

            //检验所选券数量是否超过单笔订单限制
            if (Objects.nonNull(couponThemeCache.getOrderUseLimit())
                    && couponThemeCache.getOrderUseLimit().intValue() != 0
                    && couponThemeCache.getOrderUseLimit().compareTo(countMap.get(couponEntity.getCouponThemeId()) + 1) < 0) {
                CheckCouponUsefulResponse vo = createCheckCouponUsefulResponse(couponEntity, Coupon4OrderErrorCode.OUT_OF_ORDER_LIMIT);
                log.error("操作优惠券校验error: errorCode:{},errorMessage={}, dbCoupon={}", Coupon4OrderErrorCode.OUT_OF_ORDER_LIMIT.getCode(), Coupon4OrderErrorCode.OUT_OF_ORDER_LIMIT.getMessage(), JSON.toJSONString(couponEntity));
                validateErrorMap.add(Coupon4OrderErrorCode.OUT_OF_ORDER_LIMIT, vo);
                continue;
            }

            countMap.put(couponEntity.getCouponThemeId(), countMap.get(couponEntity.getCouponThemeId()) + 1);
        }
    }

    /**
     * 比较旧交易id是否相同
     * @param couponVerificationEntity
     * @return
     */
    private boolean checkTransactionIdIsEqual(QueryUsefulCouponBo bo, CouponEntity couponEntity, CouponVerificationEntity couponVerificationEntity) {
        if (Objects.nonNull(couponVerificationEntity)) {
            String orderCode = couponVerificationEntity.getOrderCode();
            //如果券已使用，且传了旧交易id过来，则比较旧交易id是否相同
            if (CouponStatusEnum.STATUS_USED.getStatus().equals(couponEntity.getStatus())
                    && !StringUtil.isBlank(bo.getOldTransactionId()) && !bo.getOldTransactionId().equals(orderCode)
                    && !StringUtil.isBlank(bo.getTransactionId()) && !bo.getTransactionId().equals(orderCode)) {
                return false;
            }

            //如果券已使用，且传了旧交易id过来，则比较旧交易id是否相同
            if (CouponStatusEnum.STATUS_USED.getStatus().equals(couponEntity.getStatus())
                    && !StringUtil.isBlank(bo.getOldTransactionId()) && !bo.getOldTransactionId().equals(orderCode)) {
                return false;
            }

            //如果券已使用，且传了交易id过来，则比较交易id是否相同
            if (CouponStatusEnum.STATUS_USED.getStatus().equals(couponEntity.getStatus())
                    && !StringUtil.isBlank(bo.getTransactionId()) && !bo.getTransactionId().equals(orderCode)) {
                return false;
            }

            //如果券已锁定，且传了旧交易id或交易id过来，则比较旧交易id是否相同或交易id是否相同
            if (CouponStatusEnum.STATUS_LOCKED.getStatus().equals(couponEntity.getStatus())
                    && !StringUtil.isBlank(bo.getOldTransactionId()) && !bo.getOldTransactionId().equals(orderCode)
                    && !StringUtil.isBlank(bo.getTransactionId()) && !bo.getTransactionId().equals(orderCode)) {
                return false;
            }

            //如果券已锁定，且传了旧交易id过来，则比较旧交易id是否相同
            if (CouponStatusEnum.STATUS_LOCKED.getStatus().equals(couponEntity.getStatus())
                    && !StringUtil.isBlank(bo.getOldTransactionId()) && !bo.getOldTransactionId().equals(orderCode)) {
                return false;
            }

            //如果券已锁定，且传了交易id过来，则比较交易id是否相同
            if (CouponStatusEnum.STATUS_LOCKED.getStatus().equals(couponEntity.getStatus())
                    && !StringUtil.isBlank(bo.getTransactionId()) && !bo.getTransactionId().equals(orderCode)) {
                return false;
            }
        }
        return true;
    }

    private Long getUserId(String phone, Integer userType) {
        // 根据手机号获取用户id
        Long userId = null;

        switch (UserTypeEnum.of(userType)) {
            case C:
                userId = Long.valueOf(clientUserFacade.getCustomerInfoByPhone(phone).getUserId());
                break;
            case B:
                userId = Long.valueOf(clientUserFacade.getMemberInfoByPhone(phone).getUserId());
                break;
            default:
        }
        return userId;
    }

    private QueryUsefulCouponResponse createQueryUsefulCouponResponse(CouponEntity couponEntity, CouponThemeCache couponThemeCache, CouponVerificationEntity couponVerificationEntity) {
        QueryUsefulCouponResponse queryUsefulCouponResponse = new QueryUsefulCouponResponse();
        if (Objects.equals(CouponDiscountType.DISCOUNT.getType(), couponThemeCache.getCouponDiscountType())) {
            //1 按折扣
            queryUsefulCouponResponse.setCouponDiscountType(1);
            queryUsefulCouponResponse.setCouponValue(couponThemeCache.getDiscountValue().longValue());
        } else {
            //0 按金额
            queryUsefulCouponResponse.setCouponDiscountType(0);
            queryUsefulCouponResponse.setCouponValue(couponThemeCache.getDiscountAmount().multiply(new BigDecimal("100")).longValue());
        }

        queryUsefulCouponResponse.setCouponId(couponEntity.getId().toString());
        queryUsefulCouponResponse.setEndTime(couponEntity.getEndTime());
        queryUsefulCouponResponse.setStartTime(couponEntity.getStartTime());
        queryUsefulCouponResponse.setStatus(couponEntity.getStatus());
        queryUsefulCouponResponse.setCouponName(couponThemeCache.getThemeTitle());
        queryUsefulCouponResponse.setOrderUseLimit(couponThemeCache.getOrderUseLimit());
        queryUsefulCouponResponse.setUseLimit(couponThemeCache.getUseLimit());
        if (Objects.nonNull(couponVerificationEntity)){
            queryUsefulCouponResponse.setRoomName(couponVerificationEntity.getProductName());
            queryUsefulCouponResponse.setRoomGuid(couponVerificationEntity.getProductCode());
            queryUsefulCouponResponse.setTransactionId(couponVerificationEntity.getOrderCode());
        }
        // 券码
        queryUsefulCouponResponse.setCouponCode(AESPromotionUtil.decrypt(couponEntity.getCouponCode()));
        return queryUsefulCouponResponse;
    }

    private CouponThemeCache getCouponThemeCache(Long couponThemeId) {
        LambdaFieldNameSelector<CouponThemeCache> selector = new LambdaFieldNameSelector<>(CouponThemeCache.class)
                .select(CouponThemeCache::getId)
                .select(CouponThemeCache::getOrderUseLimit)
                .select(CouponThemeCache::getCouponType)
                .select(CouponThemeCache::getThemeType)
                .select(CouponThemeCache::getUseLimit)
                .select(CouponThemeCache::getThemeTitle)
                .select(CouponThemeCache::getDiscountAmount);
        return couponThemeCacheService.getById(couponThemeId, selector);
    }

    private CheckCouponUsefulResponse createCheckCouponUsefulResponse(CouponEntity couponEntity, Coupon4OrderErrorCode error) {
        CheckCouponUsefulResponse checkCouponUsefulResponse = new CheckCouponUsefulResponse();
        checkCouponUsefulResponse.setCouponId(couponEntity.getId());
        checkCouponUsefulResponse.setCouponName(couponEntity.getThemeTitle());
        checkCouponUsefulResponse.setStatus(couponEntity.getStatus());
        checkCouponUsefulResponse.setErrorCode(error.getCode());
        checkCouponUsefulResponse.setErrorMessage(error.getMessage());
        return checkCouponUsefulResponse;
    }
}
