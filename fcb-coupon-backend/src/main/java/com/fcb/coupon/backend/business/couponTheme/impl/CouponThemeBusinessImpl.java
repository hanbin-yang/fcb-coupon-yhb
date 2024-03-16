package com.fcb.coupon.backend.business.couponTheme.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fcb.coupon.backend.business.couponTheme.CouponThemeBusiness;
import com.fcb.coupon.backend.elasticsearch.CouponEsDoc;
import com.fcb.coupon.backend.exception.CouponThemeErrorCode;
import com.fcb.coupon.backend.model.ao.CouponThemeOwnedOrgAo;
import com.fcb.coupon.backend.model.bo.CouponThemeSaveBo;
import com.fcb.coupon.backend.model.bo.CouponThemeUpdateAfterCheckBo;
import com.fcb.coupon.backend.model.bo.CouponThemeUpdateBo;
import com.fcb.coupon.backend.model.cache.CouponThemeCache;
import com.fcb.coupon.backend.model.dto.OprLogDo;
import com.fcb.coupon.backend.model.entity.CouponThemeEntity;
import com.fcb.coupon.backend.model.entity.CouponThemeOrgEntity;
import com.fcb.coupon.backend.model.entity.CouponThemeStatisticEntity;
import com.fcb.coupon.backend.service.*;
import com.fcb.coupon.common.constant.CouponConstant;
import com.fcb.coupon.common.dto.AuthorityHolder;
import com.fcb.coupon.common.dto.UserInfo;
import com.fcb.coupon.common.enums.CouponThemeStatus;
import com.fcb.coupon.common.enums.CouponTypeEnum;
import com.fcb.coupon.common.enums.LogOprThemeType;
import com.fcb.coupon.common.enums.LogOprType;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年09月03日 11:37:00
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
public class CouponThemeBusinessImpl implements CouponThemeBusiness {

    private final CouponThemeService couponThemeService;
    private final CouponThemeTxService couponThemeTxService;
    private final CouponOprLogService couponOprLogService;
    private final CouponThemeStatisticService couponThemeStatisticService;
    private final CouponThemeOrgService couponThemeOrgService;
    private final CouponThemeCacheService couponThemeCacheService;
    private final CouponEsDocService couponEsDocService;

    @Autowired
    @Qualifier("couponCommonExecutor")
    private ThreadPoolTaskExecutor couponCommonExecutor;

    @Override
    public Long save(CouponThemeSaveBo bo) {
        //如果是创建平台券，需要校验是否拥有创建权限
        String orgLevelCode = bo.getOrgList().get(0).getOrgLevelCode();
        validatePlatFormTypeIfNecessary(orgLevelCode, bo.getUserOrgLevelCode());
        // 准备相关bean
        CouponThemeEntity couponThemeEntity = prepareCouponThemeEntityBean(bo);
        CouponThemeStatisticEntity couponThemeStatisticEntity = prepareCouponThemeStatisticBean(bo);
        List<CouponThemeOrgEntity> couponThemeOrgEntityList = prepareCouponThemeOrgBeans(bo);
        // 正式添加
        couponThemeTxService.saveCouponThemeRelatedDataWithTx(couponThemeEntity, couponThemeStatisticEntity, couponThemeOrgEntityList);

        // 异步日志
        OprLogDo oprLogDo = OprLogDo.builder()
                .oprUserId(bo.getUserId())
                .oprUserName(bo.getUsername())
                .oprContent(LogOprType.CREATE.getDesc())
                .refId(bo.getCouponThemeId())
                .oprThemeType(LogOprThemeType.COUPON_THEME)
                .oprType(LogOprType.CREATE)
                .build();
        couponOprLogService.saveOprLogAsync(oprLogDo);

        return couponThemeEntity.getId();
    }


    @Override
    public boolean edit(CouponThemeUpdateBo bo) {
        // 如果是平台券，需要校验是否拥有编辑权限
        String orgLevelCode = bo.getOrgList().get(0).getOrgLevelCode();
        validatePlatFormTypeIfNecessary(orgLevelCode, bo.getUserOrgLevelCode());

        // 获取券活动信息
        CouponThemeEntity dbBean = couponThemeService.getById(bo.getCouponThemeId());
        if (Objects.isNull(dbBean)) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_NOT_EXIST);
        }

        if (!couponThemeCanEdit(dbBean)) {
            throw new BusinessException(CouponThemeErrorCode.CAN_NOT_EDIT);
        }
        // theme主表需要更新
        CouponThemeEntity couponThemeBean = couponThemeNeedToUpdate(bo, dbBean);
        // theme统计表需要更新
        CouponThemeStatisticEntity couponThemeStatisticBean = couponThemeStatisticNeedToUpdate(bo);
        // 所属商家需要更新
        Tuple2<List<CouponThemeOrgEntity>, List<CouponThemeOrgEntity>> orgBeans = couponThemeOrgNeedToUpdate(bo);

        couponThemeTxService.updateCouponThemeRelatedDataWithTx(couponThemeBean, couponThemeStatisticBean, orgBeans);

        // 异步日志
        OprLogDo oprLogDo = OprLogDo.builder()
                .oprUserId(bo.getUserId())
                .oprUserName(bo.getUsername())
                .oprContent(LogOprType.EDIT.getDesc())
                .refId(bo.getCouponThemeId())
                .oprThemeType(LogOprThemeType.COUPON_THEME)
                .oprType(LogOprType.EDIT)
                .build();
        couponOprLogService.saveOprLogAsync(oprLogDo);

        return true;
    }


    @Override
    public boolean copy(Long couponThemeId) {
        // 准备coupon_theme表数据
        CouponThemeEntity newCouponThemeBean = prepareCouponThemeCopyBean(couponThemeId);
        // 准备coupon_theme_statistic表数据
        CouponThemeStatisticEntity newCouponThemeStatisticBean = prepareCouponThemeStatisticCopyBean(couponThemeId, newCouponThemeBean);
        // 准备coupon_theme_org表数据
        List<CouponThemeOrgEntity> newCouponThemeOrgBeans = prepareCouponThemeOrgCopyBeans(couponThemeId, newCouponThemeBean.getId());

        couponThemeTxService.saveCouponThemeRelatedDataWithTx(newCouponThemeBean, newCouponThemeStatisticBean, newCouponThemeOrgBeans);
        return true;
    }


    @Override
    public boolean close(Long couponThemeId) {
        CouponThemeEntity dbBean = couponThemeService.getById(couponThemeId);
        if (Objects.isNull(dbBean)) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_NOT_EXIST);
        }

        if (!couponThemeCanClose(dbBean)) {
            throw new BusinessException(CouponThemeErrorCode.CAN_NOT_CLOSE);
        }

        // 更新数据库和缓存状态为关闭
        int result = couponThemeTxService.updateCouponThemeAndCacheStatusWithTx(couponThemeId, CouponThemeStatus.CLOSED);
        if (result != CouponConstant.YES) {
            throw new BusinessException(CouponThemeErrorCode.CAN_NOT_EDIT);
        }

        // 异步日志
        UserInfo userInfo = AuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        OprLogDo oprLogDo = OprLogDo.builder()
                .oprUserId(userInfo.getUserId())
                .oprUserName(userInfo.getUsername())
                .oprContent(LogOprType.CLOSE.getDesc())
                .refId(couponThemeId)
                .oprThemeType(LogOprThemeType.COUPON_THEME)
                .oprType(LogOprType.CLOSE)
                .build();
        couponOprLogService.saveOprLogAsync(oprLogDo);

        return true;
    }


    @Override
    public boolean auditPass(Long couponThemeId, String remark) {
        CouponThemeEntity dbBean = couponThemeService.getById(couponThemeId);
        if (Objects.isNull(dbBean)) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_NOT_EXIST);
        }

        if (!couponThemeCanAuditPass(dbBean)) {
            throw new BusinessException(CouponThemeErrorCode.CAN_NOT_SUBMIT_AUDIT);
        }
        // 更新数据库和缓存的状态
        couponThemeTxService.updateCouponThemeAndCacheStatusWithTx(couponThemeId, CouponThemeStatus.EFFECTIVE);

        // 异步日志
        UserInfo userInfo = AuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        OprLogDo oprLogDo = OprLogDo.builder()
                .oprUserId(userInfo.getUserId())
                .oprUserName(userInfo.getUsername())
                .oprContent(String.format("通过，原因：%s", remark))
                .refId(couponThemeId)
                .oprThemeType(LogOprThemeType.COUPON_THEME)
                .oprType(LogOprType.AUDIT)
                .build();
        couponOprLogService.saveOprLogAsync(oprLogDo);
        return true;
    }


    @Override
    public boolean updateAfterCheck(CouponThemeUpdateAfterCheckBo bo) {
        CouponThemeCache cacheBean = couponThemeCacheService.getById(bo.getCouponThemeId());
        if (Objects.isNull(cacheBean)) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_NOT_EXIST);
        }

        // coupon_theme表需要更新
        CouponThemeEntity couponThemeUpdateBean = prepareCouponThemeBeanUpdateForAfterCheckIfNecessary(bo, cacheBean);

        couponThemeTxService.updateAfterCheckRelatedDataWithTx(couponThemeUpdateBean, cacheBean.getEffDateEndTime());

        if (Objects.isNull(couponThemeUpdateBean)) {
            return true;
        }

        // 异步日志
        StringBuilder updateContent = new StringBuilder();
        boolean updateFlag = false;
        if (Objects.nonNull(couponThemeUpdateBean.getEndTime())) {
            updateContent
                    .append("修改活动截止时间：由[")
                    .append(DateUtil.format(cacheBean.getEndTime(), DatePattern.NORM_DATETIME_MINUTE_PATTERN))
                    .append("]延期至[")
                    .append(DateUtil.format(couponThemeUpdateBean.getEndTime(), DatePattern.NORM_DATETIME_MINUTE_PATTERN))
                    .append("]&&&")
            ;
            updateFlag = true;
        }
        if (Objects.nonNull(couponThemeUpdateBean.getThemeDesc())) {
            updateContent
                    .append("修改使用说明：原[")
                    .append(cacheBean.getThemeDesc())
                    .append("]改为[")
                    .append(couponThemeUpdateBean.getThemeDesc())
                    .append("]&&&")
            ;
            updateFlag = true;
        }
        if (Objects.nonNull(couponThemeUpdateBean.getEffDateEndTime())) {
            updateContent.append("修改券码有效期截止时间：由[")
                    .append(DateUtil.format(cacheBean.getEffDateEndTime(), DatePattern.NORM_DATE_PATTERN))
                    .append("]延期至[")
                    .append(DateUtil.format(couponThemeUpdateBean.getEffDateEndTime(), DatePattern.NORM_DATE_PATTERN))
                    .append("]&&&")
            ;
            updateFlag = true;
        }

        if (!updateFlag) {
            return updateFlag;
        }

        int lastIndexOf = updateContent.lastIndexOf("&");
        updateContent.delete(lastIndexOf - 2, lastIndexOf + 1);
        // 准备异步日志dto
        OprLogDo oprLogDo = OprLogDo.builder()
                .oprUserId(bo.getUserId())
                .oprUserName(bo.getUsername())
                .oprContent(updateContent.toString())
                .refId(bo.getCouponThemeId())
                .oprThemeType(LogOprThemeType.COUPON_THEME)
                .oprType(LogOprType.UPDATE_THEME_AFTER_CHECK)
                .build();
        // 异步
        couponCommonExecutor.execute(() -> {
            // 操作日志
            couponOprLogService.saveOprLog(oprLogDo);
            // 券码有效期截止时间有变更，则更新es
            if (Objects.nonNull(couponThemeUpdateBean.getEffDateEndTime())) {
                couponEsUpdateAfterCheck(bo.getCouponThemeId(), couponThemeUpdateBean.getEffDateEndTime());
            }
        });

        return updateFlag;
    }







    private void couponEsUpdateAfterCheck(Long couponThemeId, Date couponEndTime) {
        CouponEsDoc couponEsDoc = new CouponEsDoc();
        couponEsDoc.setEndTime(couponEndTime);
        couponEsDocService.updateSelectedFieldsByCouponThemeId(couponThemeId, couponEsDoc);
    }

    private void validatePlatFormTypeIfNecessary(String orgLevelCode, String userOrgLevelCode) {
        if (StringUtils.equals(orgLevelCode, "PT")) {
            if (!StringUtils.equals(orgLevelCode, userOrgLevelCode)) {
                UserInfo userInfo = AuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
                log.error("该用户没有权限创建此优惠券 oprUserId={}, oprUserName={}, userOrgLevelCode={},  优惠券orgLevelCode={}", userInfo.getUserId(), userInfo.getUsername(), userOrgLevelCode, orgLevelCode);

                throw new BusinessException(CouponThemeErrorCode.CREATE_PLATFORM_COUPON_WITHOUT_AUTH);
            }
        }
    }

    private CouponThemeEntity prepareCouponThemeEntityBean(CouponThemeSaveBo bo) {
        CouponThemeEntity entity = new CouponThemeEntity();
        BeanUtil.copyProperties(bo, entity);
        Long id = RedisUtil.generateId();
        entity.setId(id);
        entity.setCreateUserid(bo.getUserId());
        entity.setCreateUsername(bo.getUsername());
        entity.setStatus(CouponThemeStatus.CREATE.getStatus());

        entity.setEffDateStartTime(bo.getStartTimeConfig());
        entity.setEffDateEndTime(bo.getEndTimeConfig());
        entity.setEffDateDays(bo.getEffDays());
        entity.setDiscountAmount(bo.getCouponAmount());
        entity.setDiscountValue(bo.getCouponDiscount());
        entity.setApplicableUserTypes(bo.getCrowdScopeIds());

        entity.setCanDonation(bo.getCanDonation());
        entity.setCanTransfer(bo.getCanAssign());
        entity.setUseLimit(bo.getUseLimit());

        bo.setCouponThemeId(id);
        return entity;
    }

    private CouponThemeStatisticEntity prepareCouponThemeStatisticBean(CouponThemeSaveBo bo) {
        CouponThemeStatisticEntity entity = new CouponThemeStatisticEntity();
        entity.setCouponThemeId(bo.getCouponThemeId());
        entity.setTotalCount(bo.getTotalLimit());
        // 如果是自动生成券码，createCount字段塞个totalLimit值
        if (CouponTypeEnum.COUPON_TYPE_VIRTUAL.getType().equals(bo.getCouponType())) {
            entity.setCreatedCount(bo.getTotalLimit());
        }
        return entity;
    }

    private List<CouponThemeOrgEntity> prepareCouponThemeOrgBeans(CouponThemeSaveBo bo) {
        List<CouponThemeOrgEntity> entityList = new ArrayList<>();

        List<CouponThemeOwnedOrgAo> ownedOrgAoList = bo.getOrgList().stream().distinct().collect(Collectors.toList());
        ownedOrgAoList.forEach(bean -> {
            CouponThemeOrgEntity entity = prepareInsertCouponThemeOrgBean(bo.getCouponThemeId(), bo.getUserId(), bo.getUsername(), bean);
            entityList.add(entity);
        });

        return entityList;
    }

    private CouponThemeOrgEntity prepareInsertCouponThemeOrgBean(Long couponThemeId, Long createUserId, String createUserName, CouponThemeOwnedOrgAo ao) {
        CouponThemeOrgEntity entity = new CouponThemeOrgEntity();
        entity.setOrgLevelCode(ao.getOrgLevelCode());
        entity.setOrgId(ao.getOrgId());
        entity.setCouponThemeId(couponThemeId);
        // 创建用户信息
        entity.setCreateUserid(createUserId);
        entity.setCreateUsername(createUserName);
        entity.setIsDeleted(CouponConstant.NO);
        return entity;
    }

    private boolean couponThemeCanEdit(CouponThemeEntity dbBean) {
        if (!CouponThemeStatus.CREATE.getStatus().equals(dbBean.getStatus()) && !CouponThemeStatus.AWAITING_APPROVAL.getStatus().equals(dbBean.getStatus())) {
            log.error("券活动不允许编辑,原因为状态不是新建或待审: couponThemeId={}, status={}", dbBean.getId(), dbBean.getStatus());
            return false;
        }

        return true;
    }

    private CouponThemeEntity prepareCouponThemeCopyBean(Long couponThemeId) {
        // 获取源coupon_theme表数据
        CouponThemeEntity oldDbBean = couponThemeService.getById(couponThemeId);
        if (Objects.isNull(oldDbBean)) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_NOT_EXIST);
        }

        CouponThemeEntity newBean = new CouponThemeEntity();
        BeanUtil.copyProperties(oldDbBean, newBean);
        newBean.setThemeTitle(oldDbBean.getThemeTitle() + "-复制");
        newBean.setVersionNo(null);
        newBean.setUpdateUserid(null);
        newBean.setUpdateUsername(null);
        newBean.setUpdateTime(null);
        // 重新生成新的
        newBean.setId(RedisUtil.generateId());

        newBean.setStatus(CouponThemeStatus.CREATE.getStatus());

        UserInfo userInfo = AuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        newBean.setCreateUserid(userInfo.getUserId());
        newBean.setCreateUsername(userInfo.getUsername());

        return newBean;
    }


    private CouponThemeStatisticEntity prepareCouponThemeStatisticCopyBean(Long oldCouponThemeId, CouponThemeEntity newCouponThemeBean) {
        CouponThemeStatisticEntity dbBean = couponThemeStatisticService.getById(oldCouponThemeId);
        Integer total = 0;
        if (dbBean != null) {
            total = dbBean.getTotalCount();
        }
        CouponThemeStatisticEntity newBean = new CouponThemeStatisticEntity();
        newBean.setCouponThemeId(newCouponThemeBean.getId());
        newBean.setTotalCount(total);
        if (CouponTypeEnum.COUPON_TYPE_VIRTUAL.getType().equals(newCouponThemeBean.getCouponType())) {
            newBean.setCreatedCount(total);
        } else {
            newBean.setCreatedCount(0);
        }
        return newBean;
    }

    private List<CouponThemeOrgEntity> prepareCouponThemeOrgCopyBeans(Long oldCouponThemeId, Long newCouponThemeId) {
        List<CouponThemeOrgEntity> oldBeans = couponThemeOrgService.listByCouponThemeId(oldCouponThemeId);
        if (CollectionUtil.isEmpty(oldBeans)) {
            return Collections.EMPTY_LIST;
        }

        List<CouponThemeOrgEntity> newBeans = new ArrayList<>(oldBeans.size());

        Queue<Long> ids = RedisUtil.generateIds(oldBeans.size());
        oldBeans.forEach(oldBean -> {
            CouponThemeOrgEntity newBean = new CouponThemeOrgEntity();
            newBean.setId(ids.poll());
            newBean.setCouponThemeId(newCouponThemeId);
            newBean.setOrgId(oldBean.getOrgId());
            newBean.setOrgLevelCode(oldBean.getOrgLevelCode());

            UserInfo userInfo = AuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
            newBean.setCreateUserid(userInfo.getId());
            newBean.setCreateUsername(userInfo.getUsername());

            newBean.setIsDeleted(CouponConstant.NO);
            newBeans.add(newBean);
        });

        return newBeans;
    }


    private CouponThemeEntity couponThemeNeedToUpdate(CouponThemeUpdateBo bo, CouponThemeEntity dbBean) {
        CouponThemeEntity returnEntity = new CouponThemeEntity();
        boolean updateFlag = false;
        // 发券类型
        if (Objects.nonNull(bo.getCouponGiveRule()) && !Objects.equals(bo.getCouponGiveRule(), dbBean.getCouponGiveRule())) {
            returnEntity.setCouponGiveRule(bo.getCouponGiveRule());
            updateFlag = true;
        }
        // 费用归属
        if (Objects.nonNull(bo.getBelongingOrgId()) && !Objects.equals(bo.getBelongingOrgId(), dbBean.getBelongingOrgId())) {
            returnEntity.setBelongingOrgId(bo.getBelongingOrgId());
            updateFlag = true;
        }
        // 个人总领券限制
        if (Objects.nonNull(bo.getIndividualLimit()) && !Objects.equals(bo.getIndividualLimit(), dbBean.getIndividualLimit())) {
            returnEntity.setIndividualLimit(bo.getIndividualLimit());
            updateFlag = true;
        }
        // 个人每天领券限制
        if (Objects.nonNull(bo.getEveryDayLimit()) && !Objects.equals(bo.getEveryDayLimit(), dbBean.getEveryDayLimit())) {
            returnEntity.setEveryDayLimit(bo.getEveryDayLimit());
            updateFlag = true;
        }
        // 个人每月领券限制
        if (Objects.nonNull(bo.getEveryMonthLimit()) && !Objects.equals(bo.getEveryMonthLimit(), dbBean.getEveryMonthLimit())) {
            returnEntity.setEveryMonthLimit(bo.getEveryMonthLimit());
            updateFlag = true;
        }
        // 券活动名称
        if (StringUtils.isNotBlank(dbBean.getActivityName()) && !StringUtils.equals(bo.getActivityName(), dbBean.getActivityName())) {
            returnEntity.setActivityName(bo.getActivityName());
            updateFlag = true;
        }
        // 券名称
        if (StringUtils.isNotBlank(dbBean.getThemeTitle()) && !StringUtils.equals(bo.getThemeTitle(), dbBean.getThemeTitle())) {
            returnEntity.setThemeTitle(bo.getThemeTitle());
            updateFlag = true;
        }
        // 券码生成方式
        if (Objects.nonNull(bo.getCouponType()) && !Objects.equals(bo.getCouponType(), dbBean.getCouponType())) {
            returnEntity.setCouponType(bo.getCouponType());
            updateFlag = true;
        }
        // 优惠方式
        if (Objects.nonNull(bo.getEffdateCalcMethod()) && !Objects.equals(bo.getEffdateCalcMethod(), dbBean.getEffDateCalcMethod())) {
            returnEntity.setEffDateCalcMethod(bo.getEffdateCalcMethod());
            updateFlag = true;
        }
        // 适用范围
        if (Objects.nonNull(bo.getThemeType()) && !Objects.equals(bo.getThemeType(), dbBean.getThemeType())) {
            returnEntity.setThemeType(bo.getThemeType());
            updateFlag = true;
        }
        // 券活动日期 开始
        if (Objects.nonNull(bo.getStartTime()) && !Objects.equals(bo.getStartTime(), dbBean.getStartTime())) {
            returnEntity.setStartTime(bo.getStartTime());
            updateFlag = true;
        }
        // 券活动日期 结束
        if (Objects.nonNull(bo.getEndTime()) && !Objects.equals(bo.getEndTime(), dbBean.getEndTime())) {
            returnEntity.setEndTime(bo.getEndTime());
            updateFlag = true;
        }
        // 使用说明
        if (StringUtils.isNotBlank(bo.getThemeDesc()) && !StringUtils.equals(bo.getThemeDesc(), dbBean.getThemeDesc())) {
            returnEntity.setThemeDesc(bo.getThemeDesc());
            updateFlag = true;
        }
        // 适用人群
        if (StringUtils.isNotBlank(bo.getCrowdScopeIds()) && !StringUtils.equals(bo.getCrowdScopeIds(), dbBean.getApplicableUserTypes())) {
            returnEntity.setApplicableUserTypes(bo.getCrowdScopeIds());
            updateFlag = true;
        }
        // 使用条件 订单满？元可用
        if (Objects.nonNull(bo.getUseLimit()) && dbBean.getUseLimit().compareTo(bo.getUseLimit()) != 0) {
            returnEntity.setUseLimit(bo.getUseLimit());
            updateFlag = true;
        }
        // 每个订单号每次最多可使用?张
        if (Objects.nonNull(bo.getOrderUseLimit()) && !Objects.equals(bo.getOrderUseLimit(), dbBean.getOrderUseLimit())) {
            returnEntity.setOrderUseLimit(bo.getOrderUseLimit());
            updateFlag = true;
        }
        // 固定有效时间 开始
        if (Objects.nonNull(bo.getStartTimeConfig()) && !Objects.equals(bo.getStartTimeConfig(), dbBean.getEffDateStartTime())) {
            returnEntity.setEffDateStartTime(bo.getStartTimeConfig());
            updateFlag = true;
        }
        // 固定有效时间 结束
        if (Objects.nonNull(bo.getEndTimeConfig()) && !Objects.equals(bo.getEndTimeConfig(), dbBean.getEffDateEndTime())) {
            returnEntity.setEffDateEndTime(bo.getEndTimeConfig());
            updateFlag = true;
        }
        // 有效天数
        if (Objects.nonNull(bo.getEffDays()) && !Objects.equals(bo.getEffDays(), dbBean.getEffDateDays())) {
            returnEntity.setEffDateDays(bo.getEffDays());
            updateFlag = true;
        }
        // 优惠券面值
        if (Objects.nonNull(bo.getCouponAmount()) && !Objects.equals(bo.getCouponAmount(), dbBean.getDiscountAmount())) {
            returnEntity.setDiscountAmount(bo.getCouponAmount());
            updateFlag = true;
        }
        // 优惠券折扣 7折为70
        if (Objects.nonNull(bo.getCouponDiscount()) && !Objects.equals(bo.getCouponDiscount(), dbBean.getDiscountValue())) {
            returnEntity.setDiscountValue(bo.getCouponDiscount());
            updateFlag = true;
        }
        // 是否可赠送
        if (Objects.nonNull(bo.getCanDonation()) && !Objects.equals(bo.getCanDonation(), dbBean.getCanDonation())) {
            returnEntity.setCanDonation(bo.getCanDonation());
            updateFlag = true;
        }
        // 是否可转让
        if (Objects.nonNull(bo.getCanAssign()) && !Objects.equals(bo.getCanAssign(), dbBean.getCanTransfer())) {
            returnEntity.setCanTransfer(bo.getCanAssign());
            updateFlag = true;
        }

        if (updateFlag) {
            returnEntity.setId(bo.getCouponThemeId());
            returnEntity.setUpdateUserid(bo.getUserId());
            returnEntity.setUpdateUsername(bo.getUsername());
            return returnEntity;
        } else {
            return null;
        }
    }

    private CouponThemeStatisticEntity couponThemeStatisticNeedToUpdate(CouponThemeUpdateBo bo) {
        CouponThemeStatisticEntity dbBean = couponThemeStatisticService.getById(bo.getCouponThemeId());
        if (dbBean != null && Objects.equals(bo.getTotalLimit(), dbBean.getTotalCount())) {
            return null;
        }
        CouponThemeStatisticEntity returnBean = new CouponThemeStatisticEntity();
        returnBean.setCouponThemeId(bo.getCouponThemeId());
        returnBean.setTotalCount(bo.getTotalLimit());
        if (CouponTypeEnum.COUPON_TYPE_VIRTUAL.getType().equals(bo.getCouponType())) {
            returnBean.setCreatedCount(bo.getTotalLimit());
        }
        return returnBean;
    }

    /**
     * @param bo bo入参
     * @return 2元组 t1:需要新增的beans t2:需要删除的beans
     */
    private Tuple2<List<CouponThemeOrgEntity>, List<CouponThemeOrgEntity>> couponThemeOrgNeedToUpdate(CouponThemeUpdateBo bo) {
        LambdaQueryWrapper<CouponThemeOrgEntity> queryWrapper = Wrappers.lambdaQuery(CouponThemeOrgEntity.class);
        queryWrapper.eq(CouponThemeOrgEntity::getCouponThemeId, bo.getCouponThemeId());
        List<CouponThemeOrgEntity> dbBeans = couponThemeOrgService.getBaseMapper().selectList(queryWrapper);

        Set<Long> dbOrgIds = dbBeans.stream().map(CouponThemeOrgEntity::getOrgId).collect(Collectors.toSet());

        if (CollectionUtil.isNotEmpty(bo.getOrgList())) {
            Map<Long, CouponThemeOwnedOrgAo> inputOrgIdMap = bo.getOrgList().stream().collect(Collectors.toMap(CouponThemeOwnedOrgAo::getOrgId, Function.identity()));

            // 需要新增的
            List<CouponThemeOrgEntity> insertEntityList = new ArrayList<>();
            inputOrgIdMap.keySet().forEach(inputOrgId -> {
                if (!dbOrgIds.contains(inputOrgId)) {
                    CouponThemeOrgEntity entity = new CouponThemeOrgEntity();
                    CouponThemeOwnedOrgAo inputOrgBean = inputOrgIdMap.get(inputOrgId);
                    prepareInsertCouponThemeOrgBean(bo.getCouponThemeId(), bo.getUserId(), bo.getUsername(), inputOrgBean);
                    insertEntityList.add(entity);
                }
            });

            List<CouponThemeOrgEntity> deleteEntityList = new ArrayList<>();
            dbOrgIds.forEach(dbOrgId -> {
                if (!inputOrgIdMap.containsKey(dbOrgId)) {
                    CouponThemeOrgEntity deleteEntity = new CouponThemeOrgEntity();
                    deleteEntity.setOrgId(dbOrgId);

                    deleteEntity.setUpdateUserid(bo.getUserId());
                    deleteEntity.setUpdateUsername(bo.getUsername());
                    deleteEntityList.add(deleteEntity);
                }
            });
            return Tuples.of(insertEntityList, deleteEntityList);
        }

        return Tuples.of(Collections.emptyList(), Collections.emptyList());
    }

    private boolean couponThemeCanClose(CouponThemeEntity dbBean) {
        if (!CouponThemeStatus.APPROVED.getStatus().equals(dbBean.getStatus()) && !CouponThemeStatus.EFFECTIVE.getStatus().equals(dbBean.getStatus())) {
            log.error("券活动不允许关闭，原因状态不是未开始或进行中: couponThemeId={}, status={}", dbBean.getId(), dbBean.getStatus());
            return false;
        } else if (dbBean.getEndTime().before(new Date())) {
            log.error("券活动不允许关闭，原因为活动已结束: couponThemeId={}, endTime={}", dbBean.getId(), DateUtil.format(dbBean.getEndTime(), "yyyy-MM-dd HH:mm:ss"));
            return false;
        }

        return true;
    }

    private boolean couponThemeCanAuditPass(CouponThemeEntity dbBean) {
        if (!CouponThemeStatus.AWAITING_APPROVAL.getStatus().equals(dbBean.getStatus())) {
            log.error("券活动不允许变为审核通过，原因为状态不为待审核: couponThemeId={}, status={}", dbBean.getId(), dbBean.getStatus());
            return false;
        }
        return true;
    }

    private CouponThemeEntity prepareCouponThemeBeanUpdateForAfterCheckIfNecessary(CouponThemeUpdateAfterCheckBo bo, CouponThemeCache cacheBean) {
        CouponThemeEntity entity = new CouponThemeEntity();

        boolean updateFlag = false;
        if (couponThemeEndTimeNeedToUpdateForAfterCheck(bo, cacheBean)) {
            // 券活动结束时间需要更新
            entity.setEndTime(bo.getEndTime());
            updateFlag = true;
        }

        if (couponThemeDescNeedToUpdateForAfterCheck(bo, cacheBean)) {
            // 券活动使用说明是否需要更新
            entity.setThemeDesc(bo.getThemeDesc());
            updateFlag = true;
        }

        if (Objects.nonNull(bo.getEndTimeConfig()) && !Objects.equals(bo.getEndTimeConfig(), cacheBean.getEffDateEndTime())) {
            entity.setEffDateEndTime(bo.getEndTimeConfig());
            updateFlag = true;
        }

        if (updateFlag) {
            entity.setUpdateUserid(bo.getUserId());
            entity.setUpdateUsername(bo.getUsername());
            entity.setId(cacheBean.getId());
            return entity;
        }
        return null;
    }

    private boolean couponThemeDescNeedToUpdateForAfterCheck(CouponThemeUpdateAfterCheckBo bo, CouponThemeCache cacheBean) {
        return StringUtils.isNotBlank(bo.getThemeDesc()) && !StringUtils.equals(bo.getThemeDesc(), cacheBean.getThemeDesc());
    }

    private boolean couponThemeEndTimeNeedToUpdateForAfterCheck(CouponThemeUpdateAfterCheckBo bo, CouponThemeCache cacheBean) {
        return Objects.nonNull(bo.getEndTime()) && !Objects.equals(bo.getEndTime(), cacheBean.getEndTime());
    }
}
