package com.fcb.coupon.app.business.mingyuan.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fcb.coupon.app.business.mingyuan.MingYuanVerificationBusiness;
import com.fcb.coupon.app.exception.Coupon4OrderErrorCode;
import com.fcb.coupon.app.facade.ClientUserFacade;
import com.fcb.coupon.app.infra.inteceptor.AppUserInfo;
import com.fcb.coupon.app.listener.event.MinCouponEvent;
import com.fcb.coupon.app.model.bo.OperateCouponBo;
import com.fcb.coupon.app.model.bo.OperateCouponDbBo;
import com.fcb.coupon.app.model.dto.*;
import com.fcb.coupon.app.model.entity.CouponEntity;
import com.fcb.coupon.app.model.entity.CouponVerificationEntity;
import com.fcb.coupon.app.model.param.response.OperateCoupons4OrderResponse;
import com.fcb.coupon.app.model.query.LambdaFieldNameSelector;
import com.fcb.coupon.app.remote.activity.FcbActivityFeignClient;
import com.fcb.coupon.app.remote.building.BuildingFeignClient;
import com.fcb.coupon.app.remote.dto.input.BuildingListByItemIdInput;
import com.fcb.coupon.app.remote.dto.input.PromotionCheckResInput;
import com.fcb.coupon.app.remote.dto.output.PromotionCheckResOutput;
import com.fcb.coupon.app.remote.dto.output.StoreInfoOutput;
import com.fcb.coupon.app.service.*;
import com.fcb.coupon.common.constant.InfraConstant;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.enums.*;
import com.fcb.coupon.common.exception.BusinessException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author YangHanBin
 * @date 2021-08-24 10:11
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@RefreshScope
public class MingYuanVerificationBusinessImpl implements MingYuanVerificationBusiness {
    private final CouponThemeCacheService couponThemeCacheService;
    private final CouponVerificationService couponVerificationService;
    private final MktUseRuleService mktUseRuleService;
    private final CouponOprLogService couponOprLogService;
    private final CouponService couponService;
    private final FcbActivityFeignClient fcbActivityFeignClient;
    private final BuildingFeignClient buildingFeignClient;
    private final ApplicationEventPublisher publisher;
    private final ClientUserFacade clientUserFacade;
    private final KafkaTemplate kafkaTemplate;

    @Resource(name = "couponVerificationOprExecutor")
    private ThreadPoolTaskExecutor couponVerificationOprExecutor;

    @Value("${mingyuan.user.type:0}")
    private Integer mingYuanUserType;

    private final Cache<String, List<StoreInfoOutput>> mingYuanCodeBuildingsCache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES).build();
    private final Interner<String> mingYuanCodeIntern = Interners.newWeakInterner();

    @Override
    public List<OperateCoupons4OrderResponse> operateCoupons4Order(OperateCouponBo bo) {
        log.info("operateCoupons4Order, 明源核销，B端/C端 切换，userType:{}", mingYuanUserType);
        bo.setUserType(mingYuanUserType);
        // 退房/挞定 可能只是记录个日志
        if (MingYuanOprTypeEnum.TART_FOR_SUB.getType().equals(bo.getOprType()) || MingYuanOprTypeEnum.TART_FOR_SIGN.getType().equals(bo.getOprType()) || MingYuanOprTypeEnum.TART_FOR_RESET_SUB.getType().equals(bo.getOprType())) {
            boolean unlockFlag = CollectionUtils.isNotEmpty(bo.getUnlockCoupons());
            boolean lockFlag = CollectionUtils.isNotEmpty(bo.getLockCoupons());
            boolean rebindFlag = CollectionUtils.isNotEmpty(bo.getRebindCoupons());
            boolean verifyFlag = CollectionUtils.isNotEmpty(bo.getVerifyCoupons());

            if (!unlockFlag && !lockFlag && !rebindFlag && !verifyFlag) {
                log.info("操作优惠券，只是记录日志， bo={}", JSON.toJSONString(bo));
                onlyRecordOprLog(bo);
                return null;
            }
        }
        // 校验
        Tuple3<List<OperateCoupons4OrderResponse>, Map<OperateCouponEnum, List<CouponDo>>, List<StoreInfoOutput>> validateResponse = this.validateCoupons4Order(bo);

        List<OperateCoupons4OrderResponse> responseDto = validateResponse.getT1();
        if (CollectionUtils.isNotEmpty(responseDto)) {
            return responseDto;
        }

        List<CouponDo> unlockCoupons = validateResponse.getT2().get(OperateCouponEnum.UNLOCK);
        List<CouponDo> rebindCoupons = validateResponse.getT2().get(OperateCouponEnum.REBIND);
        List<CouponDo> lockCoupons = validateResponse.getT2().get(OperateCouponEnum.LOCK);
        List<CouponDo> verifyCoupons = validateResponse.getT2().get(OperateCouponEnum.VERIFY);

        // 明源项目id下的上线楼盘
        List<StoreInfoOutput> oprOnlineBuildings = validateResponse.getT3();

        // 准备数据
        OperateCouponDbBo dbBo = createOperateCouponDbBean(bo, unlockCoupons, rebindCoupons, lockCoupons, verifyCoupons, oprOnlineBuildings);
        // 正式操作优惠券
        couponVerificationService.operateCoupon4OrderWithTx(dbBo);
        if (CollectionUtils.isNotEmpty(bo.getVerifyCoupons())) {
            String traceId = MDC.get(InfraConstant.TRACE_ID);
            // 异步
            couponVerificationOprExecutor.execute(() -> {
                try {
                    MDC.put(InfraConstant.TRACE_ID, traceId);
                    if (CollectionUtils.isNotEmpty(verifyCoupons)) {
                        // 核销判断触发保底券
                        // 保底券
                        publisher.publishEvent(new MinCouponEvent(Collections.singletonList(bo.getPhone())));
                        // 核销埋点
                        List<CouponGrowingDto> couponGrowingDtos = prepareCouponGrowingDtoBean(verifyCoupons, dbBo);
                        sendGrowingMessage(couponGrowingDtos);
                    }

                    StringBuilder extData = fillExtData(bo, oprOnlineBuildings);
                    // 记录日志
                    List<Long> couponIds = bo.getVerifyCoupons().stream().map(OperateCouponDto::getCouponId).collect(Collectors.toList());
                    List<OprLogDo> logList = createOprLogBeans(bo.getOprType(), extData, couponIds, LogOprType.VERIFICATION);

                    couponOprLogService.saveOprLogBatch(logList);
                } finally {
                    MDC.remove(InfraConstant.TRACE_ID);
                }
            });
        } else {
            couponVerificationOprExecutor.execute(() -> {
                // 记录日志，异步
                addOprLogsForOperateCoupons(bo, oprOnlineBuildings);
            });
        }
        return null;
    }



    /**
     * 埋点
     * @param dtoList
     */
    private void sendGrowingMessage(List<CouponGrowingDto> dtoList) {
        try {
            String jsonString = JSON.toJSONString(dtoList);
            log.info("核销埋点发kafa消息data:" + jsonString);
            kafkaTemplate.send("GROWING_COUPON_VERIFICATION_TOPIC", jsonString);
            log.info("核销埋点发kafa消息data end");
        } catch (Exception ex) {
            log.error("核销埋点发kafa消息发送出错:", ex);
        }
    }


    private List<CouponGrowingDto> prepareCouponGrowingDtoBean(List<CouponDo> couponSingleVerifyBos, OperateCouponDbBo dbBo) {
        return couponSingleVerifyBos.stream().map(couponSingleVerifyBo -> {
            CouponGrowingDto dto = new CouponGrowingDto();
            dto.setUsedTime(dbBo.getUpdateTime());
            dto.setCouponThemeId(couponSingleVerifyBo.getCouponThemeId());
            dto.setThemeTitle(couponSingleVerifyBo.getThemeTitle());
            dto.setUsedStoreId(dbBo.getStoreId());
            dto.setUsedStoreName(dbBo.getStoreName());
            dto.setUserType(couponSingleVerifyBo.getUserType());
            return dto;
        }).collect(Collectors.toList());
    }

    private List<OprLogDo> createOprLogBeans(Integer oprType, StringBuilder extData, List<Long> couponIds, LogOprType logOprType) {
        List<OprLogDo> logList = new ArrayList<>();
        couponIds.forEach(couponId -> {
            String oprContent = "明源:" + MingYuanOprTypeEnum.getDescByType(oprType);
            OprLogDo oprLogDo = OprLogDo.builder()
                    .refId(couponId)
                    .oprContent(oprContent)
                    .oprThemeType(LogOprThemeType.COUPON)
                    .oprType(logOprType)
                    .extData(extData.toString())
                    .build();
            logList.add(oprLogDo);
        });
        return logList;
    }

    private void addOprLogsForOperateCoupons(OperateCouponBo bo, List<StoreInfoOutput> oprBuildings) {
        List<OprLogDo> oprLogs = new ArrayList<>();

        boolean unlockFlag = CollectionUtils.isNotEmpty(bo.getUnlockCoupons());
        boolean lockFlag = CollectionUtils.isNotEmpty(bo.getLockCoupons());
        boolean rebindFlag = CollectionUtils.isNotEmpty(bo.getRebindCoupons());
        StringBuilder extraData = fillExtData(bo, oprBuildings);

        if (unlockFlag) {
            List<Long> couponIds = bo.getUnlockCoupons().stream().map(OperateCouponDto::getCouponId).collect(Collectors.toList());
            List<OprLogDo> unlockLogs = createOprLogBeans(bo.getOprType(), extraData, couponIds, LogOprType.COUPON_UNLOCK_NO);
            oprLogs.addAll(unlockLogs);

        }
        if (rebindFlag) {
            List<Long> couponIds = bo.getRebindCoupons().stream().map(OperateCouponDto::getCouponId).collect(Collectors.toList());
            List<OprLogDo> rebindLogs = createOprLogBeans(bo.getOprType(), extraData, couponIds, LogOprType.COUPON_REBIND_NO);
            oprLogs.addAll(rebindLogs);

        }
        if (lockFlag) {
            List<Long> couponIds = bo.getLockCoupons().stream().map(OperateCouponDto::getCouponId).collect(Collectors.toList());
            List<OprLogDo> lockLogs = createOprLogBeans(bo.getOprType(), extraData, couponIds, LogOprType.COUPON_LOCK_NO);
            oprLogs.addAll(lockLogs);
        }

        if (CollectionUtils.isNotEmpty(oprLogs)) {
            couponOprLogService.saveOprLogBatch(oprLogs);
        }
    }

    private StringBuilder fillExtData(OperateCouponBo bo, List<StoreInfoOutput> oprBuildings) {
        StringBuilder sb = new StringBuilder();
        sb.append("明源项目guid:").append(bo.getItemId());
        sb.append(";物业类型:").append(MingYuanWuYeType.getDescByType(Integer.parseInt(bo.getPropertyType())));
        sb.append(";对应上架楼盘:");
        int i = 0;
        for (StoreInfoOutput building : oprBuildings) {
            sb.append(building.getStoreName()).append("、");
            i++;
            if (i >= 3) {
                sb.deleteCharAt(sb.length() - 1);
                sb.append("....");
                break;
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb;
    }

    private OperateCouponDbBo createOperateCouponDbBean(OperateCouponBo bo, List<CouponDo> unlockCoupons, List<CouponDo> rebindCoupons, List<CouponDo> lockCoupons, List<CouponDo> verifyCoupons, List<StoreInfoOutput> oprOnlineBuildings) {
        OperateCouponDbBo dbBo = new OperateCouponDbBo();
        StoreInfoOutput storeInfo = oprOnlineBuildings.get(0);
        dbBo.setOrderCode(bo.getTransactionId());
        dbBo.setStoreId(Long.parseLong(storeInfo.getStoreId()));
        dbBo.setStoreName(storeInfo.getStoreName());
        dbBo.setBuildCode(storeInfo.getBuildCode());
        dbBo.setRoomGuid(bo.getRoomGuid());
        dbBo.setRoomName(bo.getRoomName());
        dbBo.setUnlockCoupons(unlockCoupons);
        dbBo.setRebindCoupons(rebindCoupons);
        dbBo.setLockCoupons(lockCoupons);
        dbBo.setVerifyCoupons(verifyCoupons);
        dbBo.setUsedChannel(bo.getUsedChannel());
        dbBo.setUpdateTime(new Date());
        dbBo.setBindTel(bo.getPhone());
        return dbBo;
    }

    private Tuple3<List<OperateCoupons4OrderResponse>, Map<OperateCouponEnum, List<CouponDo>>, List<StoreInfoOutput>> validateCoupons4Order(OperateCouponBo bo) {
        boolean verifyFlag = CollectionUtils.isNotEmpty(bo.getVerifyCoupons());
        boolean unlockFlag = CollectionUtils.isNotEmpty(bo.getUnlockCoupons());
        boolean rebindFlag = CollectionUtils.isNotEmpty(bo.getRebindCoupons());
        boolean lockFlag = CollectionUtils.isNotEmpty(bo.getLockCoupons());
        // 核销 和 解锁/替换/上锁不能同时存在
        if (verifyFlag && (unlockFlag || rebindFlag || lockFlag)) {
            throw new BusinessException(Coupon4OrderErrorCode.CAN_OPERATE_VERIFY_OR_OTHER);
        }

        // 根据手机号获取用户userId
        String userId = "";
        switch (UserTypeEnum.of(bo.getUserType())) {
            case C:
                userId = getCUserIdByPhone(bo.getPhone());
                break;
            case B:
                userId = getBUserIdByPhone(bo.getPhone());
                break;
            default:
        }
        bo.setUserId(userId);

        // 合并入参的couponIds
        List<Long> inputAllCouponIds = mergeInputCouponIds(bo);
        // 批量 集中查询券详情
        List<CouponDo> dbCoupons = getDbCoupons(inputAllCouponIds);
        Map<Long, CouponDo> dbCouponIdMap = dbCoupons.stream().collect(Collectors.toMap(CouponDo::getId, Function.identity()));
        Tuple3<Map<Long, OperateCoupons4OrderResponse>, Map<OperateCouponEnum, List<CouponDo>>, MultiValueMap<Long, CouponDo>> responseDto = this.couponDbValidateError(bo, dbCouponIdMap);
        Map<Long, OperateCoupons4OrderResponse> errorCouponVoMap = responseDto.getT1();

        MultiValueMap<Long, CouponDo> themeIdCouponMap = responseDto.getT3();
        if (CollectionUtils.isNotEmpty(bo.getLockCoupons())) {
            // 校验是否可叠加优惠券（促销活动） 已被绑定的券，不判断促销活动
            if (!canSuperimposedCoupons(bo.getRoomGuid())) {
                List<CouponDo> lockCoupons = responseDto.getT2().get(OperateCouponEnum.LOCK);
                lockCoupons.forEach(item -> {
                    OperateCoupons4OrderResponse vo = createErrorCouponInfoBean(item, Coupon4OrderErrorCode.HAS_PROMOTION);
                    log.error("操作优惠券，校验是否可叠加优惠券 error: code={}, message={}, bo={},dbCoupon={}", vo.getErrorCode(), vo.getErrorMessage(), JSON.toJSONString(bo), JSON.toJSONString(item));
                    errorCouponVoMap.put(vo.getCouponId(), vo);
                });
            }
        }

        List<StoreInfoOutput> buildings = getBuildingsByMingYuanCode(bo.getItemId());
        if (CollectionUtils.isEmpty(buildings)) {
            throw new BusinessException(Coupon4OrderErrorCode.CAN_NOT_FIND_BUILDING);
        }

        // 校验楼盘上下架
		/*List<StoreInfoOutput> onlineBuilding = validateOnlineBuilding(bo.getItemId());
		if (CollectionUtils.isEmpty(onlineBuilding)) {
			for (List<CouponDo> values : themeIdCouponMap.values()) {
				values.forEach(item -> {
                    OperateCoupons4OrderResponse vo = createErrorCouponInfoBean(item, Coupon4OrderErrorCode.NO_MATCH_BUILDING);
					log.error("操作优惠券，校验楼盘上下架onlineBuilding null error: code={}, message={}, bo={}, dbCoupon={}", vo.getErrorCode(), vo.getErrorMessage(), JSON.toJSONString(bo), JSON.toJSONString(item));
					errorCouponVoMap.put(vo.getCouponId(), vo);
				});
			}
		}*/

        Set<Long> couponThemeIds = themeIdCouponMap.keySet();
        for (Long couponThemeId : couponThemeIds) {
            // 从缓存拿券活动信息
            CouponThemeCache couponThemeCache = getCouponThemeCache(couponThemeId);

            Integer themeType = couponThemeCache.getThemeType();
            List<CouponDo> sourceCoupons = themeIdCouponMap.get(couponThemeId);
            // 非平台券才去校验适用楼盘
            if (!Objects.equals(themeType, OrgLevelEnum.PLATFORM.getThemeType())) {
                List<OperateCoupons4OrderResponse> noAdaptBuildingCoupons = validateAdaptBuildings(couponThemeId, sourceCoupons, buildings);
                if (CollectionUtils.isNotEmpty(noAdaptBuildingCoupons)) {
                    log.error("操作优惠券，校验楼盘上下架 error: code={}, message={}, bo={},dbCoupons={},onlineBuilding={}", Coupon4OrderErrorCode.NO_MATCH_BUILDING.getCode(), Coupon4OrderErrorCode.NO_MATCH_BUILDING.getMessage(), JSON.toJSONString(bo), JSON.toJSONString(sourceCoupons), JSON.toJSONString(buildings));
                    noAdaptBuildingCoupons.forEach(item -> errorCouponVoMap.put(item.getCouponId(), item));
                }
            }

            // 校验是否第三方券
            if (Objects.equals(couponThemeCache.getCouponType(), CouponTypeEnum.COUPON_TYPE_THIRD.getType())) {
                List<OperateCoupons4OrderResponse> thirdErrorCoupons = createErrorCouponInfoBeans(sourceCoupons, Coupon4OrderErrorCode.CANT_NOT_OPERATE_THIRD_COUPON);
                if (CollectionUtils.isNotEmpty(thirdErrorCoupons)) {
                    thirdErrorCoupons.forEach(item -> errorCouponVoMap.put(item.getCouponId(), item));
                    log.error("操作优惠券，第三方券不能操作 error: errorCode={}, errorMessage={}, bo={}, sourceCoupons={}", thirdErrorCoupons.get(0).getErrorCode(), thirdErrorCoupons.get(0).getErrorMessage(), JSON.toJSONString(bo), JSON.toJSONString(sourceCoupons));
                }
            }

            // 校验单笔订单限制 (上锁/换绑)/转签约核销时需校验
            if (lockFlag || rebindFlag || (MingYuanOprTypeEnum.SUB_TO_SIGN.getType().equals(bo.getOprType()))) {
                List<OperateCoupons4OrderResponse> outOfOrderLimitCoupons = validateOrderLimit(bo, sourceCoupons, couponThemeCache);
                if (CollectionUtils.isNotEmpty(outOfOrderLimitCoupons)) {
                    outOfOrderLimitCoupons.forEach(item -> errorCouponVoMap.put(item.getCouponId(), item));
                    log.error("操作优惠券，校验单笔订单限制 error: errorCode={}, errorMessage={}, bo={}, sourceCoupons={}", outOfOrderLimitCoupons.get(0).getErrorCode(), outOfOrderLimitCoupons.get(0).getErrorMessage(), JSON.toJSONString(bo), JSON.toJSONString(sourceCoupons));
                }
            }
        }

        List<OperateCoupons4OrderResponse> vo = new ArrayList<>(errorCouponVoMap.values());
        return Tuples.of(vo, responseDto.getT2(), buildings);
    }

    private List<StoreInfoOutput> validateOnlineBuilding(String mingYuanCode) {
        List<StoreInfoOutput> onlineBuilding = new ArrayList<>();
        // 校验楼盘上下架
        // ---调用楼盘接口查询
        List<StoreInfoOutput> storesFromBuilding = getBuildingsByMingYuanCode(mingYuanCode);
        if (CollectionUtils.isEmpty(storesFromBuilding)) {
            return Collections.emptyList();
        }
        boolean cOnlineFlag = false;

        for (StoreInfoOutput out : storesFromBuilding) {
            if (Objects.equals(out.getCpointBuildOnlineStatus(), 1)) {
                cOnlineFlag = true;
                onlineBuilding.add(out);
            }
        }
        if (!cOnlineFlag) {
            return Collections.emptyList();
        }
        return onlineBuilding;
    }

    private List<OperateCoupons4OrderResponse> validateOrderLimit(OperateCouponBo bo, List<CouponDo> sourceCoupons, CouponThemeCache couponThemeCache) {
        //被选此交易id选中的券
        List<CouponDo> selectedToOrderCoupons = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(bo.getLockCoupons())) {
            Set<Long> set = bo.getLockCoupons().stream().map(OperateCouponDto::getCouponId).collect(Collectors.toSet());
            for (CouponDo p : sourceCoupons) {
                if (set.contains(p.getId())) {
                    selectedToOrderCoupons.add(p);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(bo.getRebindCoupons())) {
            Set<Long> set = bo.getRebindCoupons().stream().map(OperateCouponDto::getCouponId).collect(Collectors.toSet());
            for (CouponDo p : sourceCoupons) {
                if (set.contains(p.getId())) {
                    selectedToOrderCoupons.add(p);
                }
            }
        }
        if (CollectionUtils.isEmpty(selectedToOrderCoupons)) {
            selectedToOrderCoupons = sourceCoupons;
        }

        Integer orderUseLimit = couponThemeCache.getOrderUseLimit();

        long startTime = System.currentTimeMillis();
        // 查询历史订单
        LambdaQueryWrapper<CouponVerificationEntity> queryWrapper = Wrappers.lambdaQuery(CouponVerificationEntity.class);
        queryWrapper.select(CouponVerificationEntity::getCouponId)
        .eq(CouponVerificationEntity::getCouponThemeId, couponThemeCache.getId())
        .eq(CouponVerificationEntity::getBindUserId, bo.getUserId())
        .eq(CouponVerificationEntity::getUserType, bo.getUserType())
        .eq(CouponVerificationEntity::getOrderCode, bo.getTransactionId());

        List<CouponVerificationEntity> limitCoupons = couponVerificationService.list(queryWrapper);
        log.info("明源-操作优惠券，查询订单绑定券end, couponThemeId={}, transId={}, mobile={}, 耗时{}ms",couponThemeCache.getId(), bo.getTransactionId(), bo.getPhone(),System.currentTimeMillis() - startTime);

        int limitSize = selectedToOrderCoupons.size();
        if (CollectionUtils.isNotEmpty(limitCoupons)) {
            Set<Long> limitSet = limitCoupons.stream().map(CouponVerificationEntity::getCouponId).collect(Collectors.toSet());
            // 除去要解锁的
            if (CollectionUtils.isNotEmpty(bo.getUnlockCoupons())) {
                limitSet.removeAll(bo.getUnlockCoupons().stream().map(OperateCouponDto::getCouponId).collect(Collectors.toSet()));
            }

            // 核销排除需要核销的
            if (CollectionUtils.isNotEmpty(bo.getVerifyCoupons())) {
                limitSet.removeAll(bo.getVerifyCoupons().stream().map(OperateCouponDto::getCouponId).collect(Collectors.toSet()));
            }

            if (CollectionUtils.isNotEmpty(limitSet)) {
                limitSize += limitSet.size();
            }
        }

        if (orderUseLimit != null && !orderUseLimit.equals(0) && orderUseLimit.compareTo(limitSize) < 0) {
            log.error("操作优惠券：超过单笔订单限制：limitSize={}, limitCoupons={}, bo={}", limitSize, JSON.toJSONString(limitCoupons), JSON.toJSONString(bo));
            return createErrorCouponInfoBeans(selectedToOrderCoupons, Coupon4OrderErrorCode.OUT_OF_ORDER_LIMIT);
        }
        return null;
    }

    private List<OperateCoupons4OrderResponse> validateAdaptBuildings(Long couponThemeId, List<CouponDo> sourceCoupons, List<StoreInfoOutput> onlineBuilding) {
        Set<Long> matchStoreIds = mktUseRuleService.getApplicableStoreIds(couponThemeId);
        if (matchStoreIds == null)
        {
            log.error("券活动未配置组织，couponThemeId={}", couponThemeId);
            return createErrorCouponInfoBeans(sourceCoupons, Coupon4OrderErrorCode.NO_MATCH_BUILDING);
        }

        boolean matchBuildingFlag = false;
        for (StoreInfoOutput b : onlineBuilding) {
            if (matchStoreIds.contains(Long.parseLong(b.getStoreId()))) {
                matchBuildingFlag = true;
                break;
            }
        }
        if (!matchBuildingFlag) {
            log.error("适用楼盘不匹配 couponThemeId={}, matchStoreIds={}, onlineBuilding={}",couponThemeId, JSON.toJSONString(matchStoreIds), JSON.toJSONString(onlineBuilding));
            return createErrorCouponInfoBeans(sourceCoupons, Coupon4OrderErrorCode.NO_MATCH_BUILDING);
        }

        return null;
    }

    private List<OperateCoupons4OrderResponse> createErrorCouponInfoBeans(List<CouponDo> sourceCoupons, Coupon4OrderErrorCode errorCode) {
        List<OperateCoupons4OrderResponse> voList = new ArrayList<>();
        sourceCoupons.forEach(item -> {
            OperateCoupons4OrderResponse vo = createErrorCouponInfoBean(item, errorCode);
            voList.add(vo);
        });
        return voList;
    }

    private CouponThemeCache getCouponThemeCache(Long couponThemeId) {
        LambdaFieldNameSelector<CouponThemeCache> selector = new LambdaFieldNameSelector<CouponThemeCache>()
                .select(CouponThemeCache::getId)
                .select(CouponThemeCache::getOrderUseLimit)
                .select(CouponThemeCache::getCouponType)
                .select(CouponThemeCache::getThemeType)
                ;
        return couponThemeCacheService.getById(couponThemeId, selector);
    }

    private List<StoreInfoOutput> getBuildingsByMingYuanCode(String mingYuanCode) {
        List<StoreInfoOutput> buildings = mingYuanCodeBuildingsCache.getIfPresent(mingYuanCode);
        if (CollectionUtils.isEmpty(buildings)) {
            synchronized (mingYuanCodeIntern.intern(mingYuanCode)) {
                buildings = mingYuanCodeBuildingsCache.getIfPresent(mingYuanCode);
                if (CollectionUtils.isEmpty(buildings)) {
                    BuildingListByItemIdInput input = new BuildingListByItemIdInput();
                    input.setMingYuanCode(mingYuanCode);
                    ResponseDto<List<StoreInfoOutput>> responseDto = buildingFeignClient.queryBuildingInfoByItemId(input);
                    buildings = responseDto.getData();
                    mingYuanCodeBuildingsCache.put(mingYuanCode, buildings);
                }
            }
        }
        return buildings;
    }

    private boolean canSuperimposedCoupons(String roomGuid) {
        // 校验是否可叠加优惠券（促销活动） 已被绑定的券，不判断促销活动
        PromotionCheckResInput input = new PromotionCheckResInput();
        input.setRoomGuid(roomGuid);
        ResponseDto<PromotionCheckResOutput> responseDto = fcbActivityFeignClient.queryActivityInfoByCheck(input, ClientTypeEnum.C.getKey());
        PromotionCheckResOutput promotionInfo = responseDto.getData();
        // 判断是否可叠加使用优惠券
        return !Objects.nonNull(promotionInfo) || !Objects.equals(promotionInfo.getCanUseCoupon(), 0);
    }

    /**
     *
     * @param bo
     * @param dbCouponIdMap
     * @return t1校验错误信息 t2需要后续操作数据库的数据 t3 key:couponThemId value:coupon信息集合
     */
    private Tuple3<Map<Long, OperateCoupons4OrderResponse>, Map<OperateCouponEnum, List<CouponDo>>, MultiValueMap<Long, CouponDo>> couponDbValidateError(OperateCouponBo bo, Map<Long, CouponDo> dbCouponIdMap) {
        MultiValueMap<OperateCouponEnum, CouponDo> needToOprCouponsMap = new LinkedMultiValueMap<>();
        List<OperateCouponDto> verifyCoupons = bo.getVerifyCoupons();
        List<OperateCouponDto> unlockCoupons = bo.getUnlockCoupons();
        List<OperateCouponDto> rebindCoupons = bo.getRebindCoupons();
        List<OperateCouponDto> lockCoupons = bo.getLockCoupons();

        Map<Long, OperateCoupons4OrderResponse> errorCouponVoMap= new HashMap<>();
        MultiValueMap<Long, CouponDo> themeIdCouponMap = new LinkedMultiValueMap<>();

        // 1、需要核销的数据校验
        if (CollectionUtils.isNotEmpty(verifyCoupons)) {
            verifyCoupons.forEach(item -> {
                CouponDo dbCoupon = dbCouponIdMap.get(item.getCouponId());
                OperateCoupons4OrderResponse vo = validateVerifyCoupons(bo, dbCoupon, item);
                if (vo != null) {
                    errorCouponVoMap.put(vo.getCouponId(), vo);
                } else {
                    themeIdCouponMap.add(dbCoupon.getCouponThemeId(), dbCoupon);
                    needToOprCouponsMap.add(OperateCouponEnum.VERIFY, dbCoupon);
                }
            });
        }

        // 2、需要解锁的数据校验
        if (CollectionUtils.isNotEmpty(unlockCoupons)) {
            unlockCoupons.forEach(item -> {
                CouponDo dbCoupon = dbCouponIdMap.get(item.getCouponId());
                OperateCoupons4OrderResponse vo = validateUnlockCoupons(bo, dbCoupon, item);
                if (vo != null) {
                    errorCouponVoMap.put(vo.getCouponId(), vo);
                } else {
                    themeIdCouponMap.add(dbCoupon.getCouponThemeId(), dbCoupon);
                    needToOprCouponsMap.add(OperateCouponEnum.UNLOCK, dbCoupon);
                }
            });
        }

        // 3、需要换绑的数据校验
        if (CollectionUtils.isNotEmpty(rebindCoupons)) {
            rebindCoupons.forEach(item -> {
                CouponDo dbCoupon = dbCouponIdMap.get(item.getCouponId());
                OperateCoupons4OrderResponse vo = validateRebindDbCoupons(bo, dbCoupon, item);
                if (vo != null) {
                    errorCouponVoMap.put(vo.getCouponId(), vo);
                } else {
                    themeIdCouponMap.add(dbCoupon.getCouponThemeId(), dbCoupon);
                    needToOprCouponsMap.add(OperateCouponEnum.REBIND, dbCoupon);
                }
            });
        }

        // 4、需要上锁的数据校验
        if (CollectionUtils.isNotEmpty(lockCoupons)) {
            lockCoupons.forEach(item -> {
                CouponDo dbCoupon = dbCouponIdMap.get(item.getCouponId());
                OperateCoupons4OrderResponse vo = validateLockDbCoupons(bo, dbCoupon, item);
                if (vo != null) {
                    errorCouponVoMap.put(vo.getCouponId(), vo);
                } else {
                    themeIdCouponMap.add(dbCoupon.getCouponThemeId(), dbCoupon);
                    needToOprCouponsMap.add(OperateCouponEnum.LOCK, dbCoupon);
                }
            });
        }

        return Tuples.of(errorCouponVoMap, needToOprCouponsMap, themeIdCouponMap);
    }

    private OperateCoupons4OrderResponse validateVerifyCoupons(OperateCouponBo bo, CouponDo dbCoupon, OperateCouponDto inputCoupon) {
        // 不存在
        if (dbCouponNotExist(dbCoupon)) {
            CouponDo po = new CouponDo();
            po.setId(inputCoupon.getCouponId());
            OperateCoupons4OrderResponse vo = createErrorCouponInfoBean(po, Coupon4OrderErrorCode.COUPON_NOT_EXIST);
            log.error("validateVerifyCoupons error：bo={}, vo={}",JSON.toJSONString(bo), JSON.toJSONString(vo));
            return vo;
        }
        OperateCoupons4OrderResponse vo = validateVerifyCoupons(bo, dbCoupon);
        if (Objects.nonNull(vo)) {
            log.error("validateVerifyCoupons error：errorCode={}, errormessage={}, bo={}, commonVo={}",vo.getErrorCode(), vo.getErrorMessage(), JSON.toJSONString(bo), JSON.toJSONString(dbCoupon));
        }
        return vo;
    }
    private OperateCoupons4OrderResponse validateVerifyCoupons(OperateCouponBo bo, CouponDo dbCoupon) {
        // 手机号是否匹配上
        OperateCoupons4OrderResponse commonVo = validateCommonMatch(bo.getUserId(), bo.getTransactionId(), bo.getUserType(), dbCoupon);
        if (commonVo != null) {
            return commonVo;
        }
        // 状态已经是已使用
        if (Objects.equals(dbCoupon.getStatus(), CouponStatusEnum.STATUS_USED.getStatus())) {
            // 重复核销
            return createErrorCouponInfoBean(dbCoupon, Coupon4OrderErrorCode.REPEAT_VERIFY);
        }
        if (!Objects.equals(dbCoupon.getStatus(), CouponStatusEnum.STATUS_LOCKED.getStatus())) {
            // 状态不是上锁, 无法核销
            return createErrorCouponInfoBean(dbCoupon, Coupon4OrderErrorCode.NOT_LOCK_STATUS_FOR_VERIFY);
        }
        return null;
    }

    private OperateCoupons4OrderResponse validateUnlockCoupons(OperateCouponBo bo, CouponDo dbCoupon, OperateCouponDto inputCoupon) {
        // 不存在
        if (dbCouponNotExist(dbCoupon)) {
            CouponDo po = new CouponDo();
            po.setId(inputCoupon.getCouponId());
            OperateCoupons4OrderResponse vo = createErrorCouponInfoBean(po, Coupon4OrderErrorCode.COUPON_NOT_EXIST);
            log.error("validateUnlockCoupons error：bo={}, vo={}",JSON.toJSONString(bo), JSON.toJSONString(vo));
            return vo;
        }
        // 手机号 原交易id 适用人群是否匹配
        OperateCoupons4OrderResponse vo = validateUnlockCoupons(bo, dbCoupon);
        if (Objects.nonNull(vo)) {
            log.error("validateUnlockCoupons error：errorCode={}, errormessage={}, bo={}, vo={}",vo.getErrorCode(), vo.getErrorMessage(), JSON.toJSONString(bo), JSON.toJSONString(dbCoupon));
        }
        return vo;
    }
    private OperateCoupons4OrderResponse validateUnlockCoupons(OperateCouponBo bo, CouponDo dbCoupon) {
        // 状态已经是可使用
        if (Objects.equals(dbCoupon.getStatus(), CouponStatusEnum.STATUS_USE.getStatus())) {
            // 重复解锁
            return createErrorCouponInfoBean(dbCoupon, Coupon4OrderErrorCode.REPEAT_UNLOCK);
        }
        OperateCoupons4OrderResponse commonVo = validateCommonMatch(bo.getUserId(), bo.getOldTransactionId(), bo.getUserType(), dbCoupon);
        if (commonVo != null) {
            return commonVo;
        }
        if (!Objects.equals(dbCoupon.getStatus(), CouponStatusEnum.STATUS_LOCKED.getStatus())) {
            // 状态不是上锁, 无法解锁
            return createErrorCouponInfoBean(dbCoupon, Coupon4OrderErrorCode.NOT_LOCK_STATUS_FOR_UNLOCK);
        }
        return null;
    }

    private OperateCoupons4OrderResponse validateRebindDbCoupons(OperateCouponBo bo, CouponDo dbCoupon, OperateCouponDto inputCoupon) {
        // 不存在
        if (dbCouponNotExist(dbCoupon)) {
            CouponDo po = new CouponDo();
            po.setId(inputCoupon.getCouponId());
            OperateCoupons4OrderResponse vo = createErrorCouponInfoBean(po, Coupon4OrderErrorCode.COUPON_NOT_EXIST);
            log.error("validateRebindDbCoupons error：bo={}, vo={}",JSON.toJSONString(bo), JSON.toJSONString(vo));
            return vo;
        }
        OperateCoupons4OrderResponse vo = validateRebindDbCoupons(bo, dbCoupon);
        if (Objects.nonNull(vo)) {
            log.error("validateRebindDbCoupons error：errorCode={}, errormessage={}, bo={}, vo={}",vo.getErrorCode(), vo.getErrorMessage(), JSON.toJSONString(bo), JSON.toJSONString(dbCoupon));
        }
        return vo;
    }
    private OperateCoupons4OrderResponse validateRebindDbCoupons(OperateCouponBo bo, CouponDo dbCoupon) {
        // 手机号是否匹配上
        if (!StringUtils.equals(bo.getUserId(), String.valueOf(dbCoupon.getUserId()))) {
            return createErrorCouponInfoBean(dbCoupon, Coupon4OrderErrorCode.PHONE_NOT_MATCH);
        }
        // 适用人群是否匹配上
        if (!Objects.equals(dbCoupon.getUserType(), bo.getUserType())) {
            return createErrorCouponInfoBean(dbCoupon, Coupon4OrderErrorCode.CROWD_TYPE_NOT_MATCH);
        }
        // 新旧交易id一致，不能换绑
        if (StringUtils.equals(bo.getOldTransactionId(), bo.getTransactionId())) {
            return createErrorCouponInfoBean(dbCoupon, Coupon4OrderErrorCode.OLD_AND_NEW_TRANSACTION_EQUALS);
        }
        // 已经绑定了现交易id
        if (StringUtils.equals(dbCoupon.getOrderCode(), bo.getTransactionId())) {
            // 重复换绑
            return createErrorCouponInfoBean(dbCoupon, Coupon4OrderErrorCode.REPEAT_REBIND);
        }
        // 原交易id不匹配
        if (!StringUtils.equals(bo.getOldTransactionId(), dbCoupon.getOrderCode())) {
            return createErrorCouponInfoBean(dbCoupon, Coupon4OrderErrorCode.OLD_ORDER_CODE_NOT_MATCH);
        }
        // 要换绑的券不是上锁或不是已使用状态
        if (!Objects.equals(dbCoupon.getStatus(), CouponStatusEnum.STATUS_LOCKED.getStatus()) && !Objects.equals(dbCoupon.getStatus(), CouponStatusEnum.STATUS_USED.getStatus())) {
            return createErrorCouponInfoBean(dbCoupon, Coupon4OrderErrorCode.NOT_LOCK_OR_USED_STATUS_FOR_REBIND);
        }
        return null;
    }

    private OperateCoupons4OrderResponse validateLockDbCoupons(OperateCouponBo bo, CouponDo dbCoupon, OperateCouponDto inputCoupons) {
        if (dbCouponNotExist(dbCoupon)) {
            CouponDo po = new CouponDo();
            po.setId(inputCoupons.getCouponId());
            OperateCoupons4OrderResponse vo = createErrorCouponInfoBean(po, Coupon4OrderErrorCode.COUPON_NOT_EXIST);
            log.error("validateLockDbCoupons error：bo={}, vo={}",JSON.toJSONString(bo), JSON.toJSONString(vo));
            return vo;
        }
        OperateCoupons4OrderResponse vo = validateLockDbCoupons(bo, dbCoupon);
        if (Objects.nonNull(vo)) {
            log.error("validateLockDbCoupons error：errorCode={}, errormessage={}, bo={}, vo={}",vo.getErrorCode(), vo.getErrorMessage(), JSON.toJSONString(bo), JSON.toJSONString(dbCoupon));
        }
        return vo;
    }
    private OperateCoupons4OrderResponse validateLockDbCoupons(OperateCouponBo bo, CouponDo dbCoupon) {
        // 手机号没有匹配上
        if (!StringUtils.equals(String.valueOf(dbCoupon.getUserId()), bo.getUserId())) {
            return createErrorCouponInfoBean(dbCoupon, Coupon4OrderErrorCode.PHONE_NOT_MATCH);
        }
        // 券交易id绑定了其他交易id
        if (StringUtils.isNotEmpty(dbCoupon.getOrderCode()) && !StringUtils.equals(dbCoupon.getOrderCode(), bo.getTransactionId())) {
            return createErrorCouponInfoBean(dbCoupon, Coupon4OrderErrorCode.BIND_OTHER_ORDER_CODE);
        }
        // 券已绑定此次交易id,且状态已是上锁
        if (StringUtils.equals(dbCoupon.getOrderCode(), bo.getTransactionId()) && Objects.equals(dbCoupon.getStatus(), CouponStatusEnum.STATUS_LOCKED.getStatus())) {
            // 重复上锁
            return createErrorCouponInfoBean(dbCoupon, Coupon4OrderErrorCode.REPEAT_LOCK);
        }
        // 查出来的券是否都绑定了C端
        if (!Objects.equals(dbCoupon.getUserType(), bo.getUserType())) {
            return createErrorCouponInfoBean(dbCoupon, Coupon4OrderErrorCode.CROWD_TYPE_NOT_MATCH);
        }
        // 校验券有效期
        Date nowDate = new Date();
        Date startTime = dbCoupon.getStartTime();
        Date endTime = dbCoupon.getEndTime();
        boolean startFlag = (startTime != null && startTime.compareTo(nowDate) > 0);
        boolean endFlag = (endTime != null && endTime.compareTo(nowDate) < 0);
        if (startFlag || endFlag) {
            return createErrorCouponInfoBean(dbCoupon, Coupon4OrderErrorCode.COUPON_EXPIRED);
        }

        // 券状态不是可使用
        if (!Objects.equals(dbCoupon.getStatus(), CouponStatusEnum.STATUS_USE.getStatus())) {

            return createErrorCouponInfoBean(dbCoupon, Coupon4OrderErrorCode.NOT_CAN_USE_STATUS);
        }
        return null;
    }

    private OperateCoupons4OrderResponse validateCommonMatch(String userId, String transactionId, int crowdType, CouponDo dbCoupon) {
        // 手机号是否匹配上
        if (!StringUtils.equals(userId, String.valueOf(dbCoupon.getUserId()))) {
            return createErrorCouponInfoBean(dbCoupon, Coupon4OrderErrorCode.PHONE_NOT_MATCH);
        }
        // 交易id是否匹配上
        if (!StringUtils.equals(transactionId, dbCoupon.getOrderCode())) {
            return createErrorCouponInfoBean(dbCoupon, Coupon4OrderErrorCode.ORDER_CODE_NOT_MATCH);
        }
        // 适用人群是否匹配上
        if (!Objects.equals(dbCoupon.getUserType(), crowdType)) {
            return createErrorCouponInfoBean(dbCoupon, Coupon4OrderErrorCode.CROWD_TYPE_NOT_MATCH);
        }

        return null;
    }

    private OperateCoupons4OrderResponse createErrorCouponInfoBean(CouponDo dbCoupon, Coupon4OrderErrorCode errorCode) {
        OperateCoupons4OrderResponse vo = createOprCoupon4OrderVoBean(dbCoupon);
        vo.setErrorCode(errorCode.getCode());
        vo.setErrorMessage(errorCode.getMessage());
        return vo;
    }
    private OperateCoupons4OrderResponse createOprCoupon4OrderVoBean(CouponDo dbCoupon) {
        OperateCoupons4OrderResponse vo = new OperateCoupons4OrderResponse();
        vo.setCouponId(dbCoupon.getId());
        vo.setCouponName(dbCoupon.getThemeTitle());
        vo.setStatus(dbCoupon.getStatus());
        return vo;
    }

    private List<CouponDo> getDbCoupons(List<Long> inputAllCouponIds) {
        if (CollectionUtils.isEmpty(inputAllCouponIds)) {
            throw new BusinessException(Coupon4OrderErrorCode.NO_OPERATE_COUPONS);
        }
        List<CouponEntity> dbCoupons = couponService.listByIds(inputAllCouponIds);
        if (CollectionUtils.isEmpty(dbCoupons)) {
            log.error("明源操作 查询优惠券，couponIds={},outCoupons={}", JSON.toJSONString(inputAllCouponIds), JSON.toJSONString(dbCoupons));
            throw new BusinessException(Coupon4OrderErrorCode.NO_REAL_COUPON);
        }
        LambdaQueryWrapper<CouponVerificationEntity> verifyEntityQueryWrapper = Wrappers.lambdaQuery(CouponVerificationEntity.class);
        verifyEntityQueryWrapper.in(CouponVerificationEntity::getCouponId, inputAllCouponIds);
        List<CouponVerificationEntity> dbCouponVerificationList = couponVerificationService.list(verifyEntityQueryWrapper);
        Map<Long, CouponVerificationEntity> dbVerificationMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(dbCouponVerificationList)) {
            dbVerificationMap.putAll(dbCouponVerificationList.stream().collect(Collectors.toMap(CouponVerificationEntity::getCouponId, Function.identity())));
        }

        List<CouponDo> couponDoList = new ArrayList<>();
        dbCoupons.forEach(dbCoupon -> {
            // 填充coupon表
            CouponDo couponDo = new CouponDo();
            BeanUtils.copyProperties(dbCoupon, couponDo);
            couponDo.setCouponCreateUserid(dbCoupon.getCreateUserid());
            couponDo.setCouponCreateUsername(dbCoupon.getCreateUsername());
            couponDo.setCouponCreateTime(dbCoupon.getCreateTime());
            // 填充coupon_verification表
            CouponVerificationEntity dbCouponVerification = dbVerificationMap.get(dbCoupon.getId());
            if (Objects.nonNull(dbCouponVerification)) {
                couponDo.setOrderCode(dbCouponVerification.getOrderCode());
                couponDo.setUsedRoomGuid(dbCouponVerification.getUsedRoomGuid());
                couponDo.setProductCode(dbCouponVerification.getProductCode());
                couponDo.setProductName(dbCouponVerification.getProductName());
            }
            couponDoList.add(couponDo);
        });

        return couponDoList;
    }

    private boolean dbCouponNotExist(CouponDo dbCoupon) {
        return dbCoupon == null;
    }

    private List<Long> mergeInputCouponIds(OperateCouponBo bo) {
        List<Long> inputAllCouponIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(bo.getUnlockCoupons())) {
            inputAllCouponIds.addAll(bo.getUnlockCoupons().stream().map(OperateCouponDto::getCouponId).collect(Collectors.toList()));
        }
        if (CollectionUtils.isNotEmpty(bo.getRebindCoupons())) {
            inputAllCouponIds.addAll(bo.getRebindCoupons().stream().map(OperateCouponDto::getCouponId).collect(Collectors.toList()));
        }
        if (CollectionUtils.isNotEmpty(bo.getLockCoupons())) {
            inputAllCouponIds.addAll(bo.getLockCoupons().stream().map(OperateCouponDto::getCouponId).collect(Collectors.toList()));
        }
        if (CollectionUtils.isNotEmpty(bo.getVerifyCoupons())) {
            inputAllCouponIds.addAll(bo.getVerifyCoupons().stream().map(OperateCouponDto::getCouponId).collect(Collectors.toList()));
        }
        if (inputAllCouponIds.size() > 100) {
            log.error("明源操作优惠券数量超限：couponIds= {}", inputAllCouponIds.size());
            throw new BusinessException(Coupon4OrderErrorCode.OPERATE_OUT_OF_RANGE);
        }
        return inputAllCouponIds;
    }

    private String getCUserIdByPhone(String verifyPhone) {
        // C端
        AppUserInfo appUserInfo = clientUserFacade.getCustomerInfoByPhone(verifyPhone);
        if (Objects.isNull(appUserInfo) || Objects.isNull(appUserInfo.getUserId())) {
            throw new BusinessException(Coupon4OrderErrorCode.PHONE_NOT_REGISTER);
        }

        return appUserInfo.getUserId();
    }

    private String getBUserIdByPhone(String verifyPhone) {
        // C端
        AppUserInfo appUserInfo = clientUserFacade.getMemberInfoByPhone(verifyPhone);
        if (Objects.isNull(appUserInfo) || Objects.isNull(appUserInfo.getUserId())) {
            throw new BusinessException(Coupon4OrderErrorCode.PHONE_NOT_REGISTER);
        }

        return appUserInfo.getUserId();
    }

    private void onlyRecordOprLog(OperateCouponBo bo) {
        String oprContent = "明源:" + MingYuanOprTypeEnum.getDescByType(bo.getOprType());
        OprLogDo oprLogDo = OprLogDo.builder()
                .oprContent(oprContent)
                .oprThemeType(LogOprThemeType.COUPON)
                .oprType(LogOprType.MINGY_YUAN_INVALID)
                .build();
        couponOprLogService.saveOprLog(oprLogDo);
    }
}
