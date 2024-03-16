package com.fcb.coupon.backend.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fcb.coupon.backend.exception.CouponThemeErrorCode;
import com.fcb.coupon.backend.mapper.CouponThemeMapper;
import com.fcb.coupon.backend.model.ao.DetailCouponThemeOrgInfoAo;
import com.fcb.coupon.backend.model.ao.OrgRangeAo;
import com.fcb.coupon.backend.model.bo.*;
import com.fcb.coupon.backend.model.dto.*;
import com.fcb.coupon.backend.model.entity.CouponGenerateBatchEntity;
import com.fcb.coupon.backend.model.entity.CouponThemeEntity;
import com.fcb.coupon.backend.model.entity.CouponThemeOrgEntity;
import com.fcb.coupon.backend.model.entity.CouponThemeStatisticEntity;
import com.fcb.coupon.backend.model.param.response.*;
import com.fcb.coupon.backend.remote.client.CommonFileClient;
import com.fcb.coupon.backend.remote.client.OuserWebFeignClient;
import com.fcb.coupon.backend.remote.dto.input.InputDto;
import com.fcb.coupon.backend.remote.dto.input.OrgIdsInput;
import com.fcb.coupon.backend.remote.dto.out.OrgOutDto;
import com.fcb.coupon.backend.remote.dto.out.OutputDto;
import com.fcb.coupon.backend.service.*;
import com.fcb.coupon.client.backend.param.request.CouponThemeListCmsRequest;
import com.fcb.coupon.client.backend.param.response.CouponThemeListCmsResponse;
import com.fcb.coupon.common.constant.CommonConstant;
import com.fcb.coupon.common.constant.CouponConstant;
import com.fcb.coupon.common.constant.InfraConstant;
import com.fcb.coupon.common.dto.*;
import com.fcb.coupon.common.enums.*;
import com.fcb.coupon.common.excel.constant.ImportConstant;
import com.fcb.coupon.common.excel.excetption.ImportException;
import com.fcb.coupon.common.excel.exporter.Exporter;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.file.CommonMultipartFile;
import com.fcb.coupon.common.midplatform.MidPlatformLoginHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author YangHanBin
 * @date 2021-06-11 17:06
 */
@Service
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@Slf4j
public class CouponThemeServiceImpl extends ServiceImpl<CouponThemeMapper, CouponThemeEntity> implements CouponThemeService {

    private static final String CROWD_SCOPE_IDS = "crowdScopeIds";
    private static final String IDS = "ids";
    private static final Integer ALL = -1;

    private final MidPlatformLoginHelper midPlatformLoginHelper;
    private final Exporter excelExporter;
    private final ThreadPoolTaskExecutor couponBatchExecutor;
    private final CommonFileClient commonFileClient;
    private final OuserWebFeignClient ouserWebFeignClient;

    private final CouponThemeOrgService couponThemeOrgService;
    private final CouponThemeStatisticService couponThemeStatisticService;
    private final CouponOprLogService couponOprLogService;
    private final CouponGenerateBatchService couponGenerateBatchService;
    private final MktUseRuleService mktUseRuleService;
    private final CouponUserService couponUserService;


    @Override
    public boolean delete(Long couponThemeId) {
        CouponThemeEntity dbBean = getCouponThemeEntityById(couponThemeId);
        if (!couponThemeCanDelete(dbBean)) {
            throw new BusinessException(CouponThemeErrorCode.CAN_NOT_CLOSE);
        }

        int result = baseMapper.deleteById(couponThemeId);
        if (result != 1) {
            throw new BusinessException(CouponThemeErrorCode.CLOSE_FAIL);
        }

        // 异步日志
        UserInfo userInfo = AuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        OprLogDo oprLogDo = OprLogDo.builder()
                .oprUserId(userInfo.getUserId())
                .oprUserName(userInfo.getUsername())
                .oprContent(LogOprType.DELETE.getDesc())
                .refId(couponThemeId)
                .oprThemeType(LogOprThemeType.COUPON_THEME)
                .oprType(LogOprType.DELETE)
                .build();
        couponOprLogService.saveOprLogAsync(oprLogDo);

        return true;
    }


    @Override
    public int updateCouponThemeStatus(Long couponThemeId, CouponThemeStatus status) {
        CouponThemeEntity entity = new CouponThemeEntity();
        entity.setId(couponThemeId);
        entity.setStatus(status.getStatus());
        return baseMapper.updateById(entity);
    }


    @Override
    public boolean submitAudit(Long couponThemeId) {
        CouponThemeEntity dbBean = getCouponThemeEntityById(couponThemeId);
        if (!couponThemeCanSubmitAudit(dbBean)) {
            throw new BusinessException(CouponThemeErrorCode.CAN_NOT_SUBMIT_AUDIT);
        }
        updateCouponThemeStatus(couponThemeId, CouponThemeStatus.AWAITING_APPROVAL);
        // 异步日志
        UserInfo userInfo = AuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        OprLogDo oprLogDo = OprLogDo.builder()
                .oprUserId(userInfo.getUserId())
                .oprUserName(userInfo.getUsername())
                .oprContent(LogOprType.SUBMIT_AUDIT.getDesc())
                .refId(couponThemeId)
                .oprThemeType(LogOprThemeType.COUPON_THEME)
                .oprType(LogOprType.SUBMIT_AUDIT)
                .build();
        couponOprLogService.saveOprLogAsync(oprLogDo);
        return true;
    }


    @Override
    public boolean auditNotPass(Long couponThemeId, String remark) {
        CouponThemeEntity dbBean = getCouponThemeEntityById(couponThemeId);
        if (!couponThemeCanAuditNotPass(dbBean)) {
            throw new BusinessException(CouponThemeErrorCode.CAN_NOT_SUBMIT_AUDIT);
        }
        updateCouponThemeStatus(couponThemeId, CouponThemeStatus.UN_APPROVE);
        // 异步日志
        UserInfo userInfo = AuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        OprLogDo oprLogDo = OprLogDo.builder()
                .oprUserId(userInfo.getUserId())
                .oprUserName(userInfo.getUsername())
                .oprContent(String.format("不通过，原因：%s", remark))
                .refId(couponThemeId)
                .oprThemeType(LogOprThemeType.COUPON_THEME)
                .oprType(LogOprType.AUDIT)
                .build();
        couponOprLogService.saveOprLogAsync(oprLogDo);
        return true;
    }


    private boolean couponThemeCanDelete(CouponThemeEntity dbBean) {
        if (!CouponThemeStatus.CREATE.getStatus().equals(dbBean.getStatus())) {
            log.error("券活动不允许删除，原因为状态不是待提交 couponThemeId={}, status={}", dbBean.getId(), dbBean.getStatus());
            return false;
        }
        return true;
    }

    private boolean couponThemeCanAuditNotPass(CouponThemeEntity dbBean) {
        if (!CouponThemeStatus.AWAITING_APPROVAL.getStatus().equals(dbBean.getStatus())) {
            log.error("券活动不允许变为审核不通过，原因为状态不为待审核: couponThemeId={}, status={}", dbBean.getId(), dbBean.getStatus());
            return false;
        }
        return true;
    }

    private boolean couponThemeCanSubmitAudit(CouponThemeEntity dbBean) {
        if (!CouponThemeStatus.CREATE.getStatus().equals(dbBean.getStatus()) && !CouponThemeStatus.UN_APPROVE.getStatus().equals(dbBean.getStatus())) {
            log.error("券活动不允许提交审核，原因为状态不为待提交或审核不通过: couponThemeId={}, status={}", dbBean.getId(), dbBean.getStatus());
            return false;
        }
        return true;
    }


    private CouponThemeEntity getCouponThemeEntityById(Long couponThemeId) {
        CouponThemeEntity dbBean = baseMapper.selectById(couponThemeId);
        if (Objects.isNull(dbBean)) {
            log.error("查询数据库,券活动不存在: couponThemeId={}", couponThemeId);
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_NOT_EXIST);
        }
        return dbBean;
    }


    /*
     * @description 获取优惠券金额信息
     * @author 唐陆军
     * @param: couponThemeEntity
     * @date 2021-8-6 17:28
     * @return: java.lang.String
     */
    @Override
    public String getCouponAmount(CouponThemeEntity couponThemeEntity) {
        BigDecimal ruleAmount = couponThemeEntity.getDiscountAmount();
        if (CouponDiscountType.DISCOUNT.getType().equals(couponThemeEntity.getCouponDiscountType())) {
            ruleAmount = new BigDecimal(couponThemeEntity.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        if (null != ruleAmount && ruleAmount.doubleValue() > 0) {
            return CommonConstant.DECIMAL_FORMAT.format(couponThemeEntity.getDiscountAmount());
        }
        return "";
    }


    /**
     * 营销中心->优惠券管理->优惠券活动列表
     *
     * @param bo 查询条件
     * @return
     */
    @Override
    public PageResponse<CouponThemeListResponse> listCouponTheme(CouponThemeListBo bo) {
        //初始化查询条件
        CouponThemeListDo dos = new CouponThemeListDo();
        initCouponThemeListDo(bo, dos);

        //统计总数
        int total = this.baseMapper.listCouponThemeCount(dos);
        List<CouponThemeEntity> couponThemeEntityList = this.baseMapper.listCouponTheme(dos);

        PageResponse<CouponThemeListResponse> pageResponse = new PageResponse<>();
        List<CouponThemeListResponse> listObjs = new ArrayList<>();
        pageResponse.setListObj(listObjs);
        pageResponse.setTotal(total);

        //查询结果为空直接返回
        if (CollectionUtils.isEmpty(couponThemeEntityList)) {
            return pageResponse;
        }

        List<Long> couponThemeIdList = couponThemeEntityList.stream().map(CouponThemeEntity::getId).collect(Collectors.toList());
        List<CouponThemeStatisticEntity> couponThemeStatisticEntityList = couponThemeStatisticService.listByIds(couponThemeIdList);
        Map<Long, CouponThemeStatisticEntity> couponThemeStatisticEntityMap = couponThemeStatisticEntityList.stream().collect(Collectors.toMap(CouponThemeStatisticEntity::getCouponThemeId, item -> item));

        List<CouponThemeOrgEntity> couponThemeOrgEntityList = this.couponThemeOrgService.listByCouponThemeIds(couponThemeIdList);
        Map<Long, List<CouponThemeOrgEntity>> couponThemeOrgEntityMap = couponThemeOrgEntityList.stream().collect(Collectors.groupingBy(CouponThemeOrgEntity::getCouponThemeId));
        List<Long> orgIdList = couponThemeOrgEntityList == null ? new ArrayList<>() : couponThemeOrgEntityList.stream().map(o -> o.getOrgId()).distinct().collect(Collectors.toList());
        List<OrgOutDto> orgInfoAoList = this.getOrgInfoByOrgIdsFromRemote(orgIdList);
        Map<Long, OrgOutDto> orgInfoMap = orgInfoAoList.stream().collect(Collectors.toMap(OrgOutDto::getOrgId, item -> item));

        couponThemeEntityList.stream().forEach(couponThemeEntity -> {
            CouponThemeListResponse couponThemeListResponse = new CouponThemeListResponse();
            //拷贝基本数据项
            BeanUtils.copyProperties(couponThemeEntity, couponThemeListResponse, CROWD_SCOPE_IDS);
            //券面额
            couponThemeListResponse.setCouponAmount(couponThemeEntity.getDiscountAmount());
            //单独设置适用人群属性值
            List<Integer> crowdScopeIdList = convertCrowdScopeId(couponThemeEntity);
            couponThemeListResponse.setCrowdScopeIds(crowdScopeIdList);

            //单独设置适用人群中文名称
            List<String> crowdScopeNameList = convertCrowdScopeName(crowdScopeIdList);
            couponThemeListResponse.setCrowdScopedStr(String.join("、", crowdScopeNameList));

            //设置张数
            convertCount(couponThemeStatisticEntityMap, couponThemeEntity, couponThemeListResponse);

            //设置状态
            convertStatus(couponThemeListResponse, couponThemeEntity);

            //转换券面额
            convertCouponAmount(couponThemeListResponse, couponThemeEntity);

            convertCouponOrg(couponThemeOrgEntityMap, orgInfoMap, couponThemeEntity, couponThemeListResponse);

            listObjs.add(couponThemeListResponse);
        });

        return pageResponse;
    }

    private void convertCouponOrg(Map<Long, List<CouponThemeOrgEntity>> couponThemeOrgEntityMap, Map<Long, OrgOutDto> orgInfoMap, CouponThemeEntity couponThemeEntity, CouponThemeBaseResponse couponThemeBaseResponse) {
        List<CouponThemeOrgEntity> themeOrgEntityList = couponThemeOrgEntityMap.get(couponThemeEntity.getId());
        List<Long> orgIdListSub = themeOrgEntityList == null ? new ArrayList<>() : themeOrgEntityList.stream().map(o -> o.getOrgId()).distinct().collect(Collectors.toList());

        List<String> orgNameList = themeOrgEntityList.stream().map(o -> {
            if (Objects.isNull(orgInfoMap)) {
                return org.apache.commons.lang.StringUtils.EMPTY;
            }
            return Objects.isNull(orgInfoMap.get(o.getOrgId())) ? org.apache.commons.lang.StringUtils.EMPTY : orgInfoMap.get(o.getOrgId()).getOrgName();
        }).collect(Collectors.toList());

        couponThemeBaseResponse.setOrgIds(orgIdListSub);
        couponThemeBaseResponse.setOrgNames(orgNameList);
    }

    /**
     * 转换券面额
     *
     * @param couponThemeBaseResponse
     * @param entity
     */
    private void convertCouponAmount(CouponThemeBaseResponse couponThemeBaseResponse, CouponThemeEntity entity) {
        //券折扣
        if (Objects.equals(CouponDiscountType.DISCOUNT.getType(), entity.getCouponDiscountType())) {
            BigDecimal discount = new BigDecimal(entity.getDiscountValue());
            BigDecimal divisor = new BigDecimal(100);

            couponThemeBaseResponse.setCouponAmount(discount.divide(divisor, 2, BigDecimal.ROUND_HALF_UP));
            couponThemeBaseResponse.setUseUpLimit(entity.getDiscountAmount());
            couponThemeBaseResponse.setCouponUnit(CouponConstant.COUPON_UNIT_DISCOUNT);
            couponThemeBaseResponse.setRuleType(CouponRuleType.DISCOUNT.getType());
        }

        //券金额
        if (Objects.equals(CouponDiscountType.CASH.getType(), entity.getCouponDiscountType())) {
            couponThemeBaseResponse.setCouponAmount(entity.getDiscountAmount());
            couponThemeBaseResponse.setCouponAmountExt1(BigDecimal.ZERO);
            couponThemeBaseResponse.setCouponUnit(CouponConstant.COUPON_UNIT_AMOUNT);
            couponThemeBaseResponse.setRuleType(CouponRuleType.AMOUNT.getType());
        }

        //是否可赠送
        if (Objects.equals(CouponDiscountType.WELFARE_CARD.getType(), entity.getCouponDiscountType())) {
            if (Objects.equals(entity.getCanDonation(), YesNoEnum.YES.getValue())) {
                couponThemeBaseResponse.setCanDonation(Boolean.TRUE);
            } else if (Objects.equals(entity.getCanDonation(), YesNoEnum.NO.getValue())) {
                couponThemeBaseResponse.setCanDonation(Boolean.FALSE);
            }

        }
    }

    /**
     * 转换状态
     *
     * @param couponThemeEntity 实体对象
     * @return
     */
    private void convertStatus(CouponThemeBaseResponse couponThemeBaseResponse, CouponThemeEntity couponThemeEntity) {
        //状态进行中，但开始时间未到，则前端显示未开始
        if (Objects.equals(couponThemeEntity.getStatus(), CouponThemeStatus.EFFECTIVE.getStatus()) && couponThemeEntity.getStartTime().after(new Date())) {
            couponThemeBaseResponse.setStatus(CouponThemeStatus.APPROVED.getStatus());
        }

        //状态进行中，但结束时间已到，则前端显示已经失效
        if (Objects.equals(couponThemeEntity.getStatus(), CouponThemeStatus.EFFECTIVE.getStatus()) && couponThemeEntity.getEndTime().before(new Date())) {
            couponThemeBaseResponse.setStatus(CouponThemeStatus.INEFFECTIVE.getStatus());
        }

    }

    /**
     * 转换状态
     *
     * @param couponThemeEntity 实体对象
     * @return
     */
    private Integer convertStatus(CouponThemeEntity couponThemeEntity) {
        //状态进行中，但开始时间未到，则前端显示未开始
        if (Objects.equals(couponThemeEntity.getStatus(), CouponThemeStatus.EFFECTIVE.getStatus()) && couponThemeEntity.getStartTime().after(new Date())) {
            return CouponThemeStatus.APPROVED.getStatus();
        }

        //状态进行中，但结束时间已到，则前端显示已经失效
        if (Objects.equals(couponThemeEntity.getStatus(), CouponThemeStatus.EFFECTIVE.getStatus()) && couponThemeEntity.getEndTime().before(new Date())) {
            return CouponThemeStatus.INEFFECTIVE.getStatus();
        }

        return couponThemeEntity.getStatus();
    }

    /**
     * 设置发行总张数、已经生成的张数、已领取的张数、已使用的张数、还可生成的张数
     *
     * @param couponThemeStatisticEntityMap 统计信息
     * @param couponThemeEntity             实体对象
     * @param couponThemeBaseResponse       返回前端的数据对象
     */
    private void convertCount(Map<Long, CouponThemeStatisticEntity> couponThemeStatisticEntityMap, CouponThemeEntity couponThemeEntity, CouponThemeBaseResponse couponThemeBaseResponse) {
        CouponThemeStatisticEntity couponThemeStatisticEntity = couponThemeStatisticEntityMap.get(couponThemeEntity.getId());

        //还可发放张数
        int canSendAmount = 0;
        //还可生成张数
        int availableCoupons = 0;
        //已生成张数
        int drawedCoupons = 0;
        if (CouponTypeEnum.COUPON_TYPE_THIRD.getType().equals(couponThemeEntity.getCouponType()) || CouponTypeEnum.COUPON_TYPE_REAL.getType().equals(couponThemeEntity.getCouponType())) {
            //还可生成张数
            availableCoupons = couponThemeStatisticEntity.getTotalCount() - couponThemeStatisticEntity.getCreatedCount();
            //还可发放张数
            canSendAmount = couponThemeStatisticEntity.getCreatedCount() - couponThemeStatisticEntity.getSendedCount();
            //已生成张数
            drawedCoupons = couponThemeStatisticEntity.getCreatedCount();
        }

        if (CouponTypeEnum.COUPON_TYPE_VIRTUAL.getType().equals(couponThemeEntity.getCouponType()) || CouponTypeEnum.COUPON_TYPE_REDENVELOPE.getType().equals(couponThemeEntity.getCouponType())) {
            //还可发放张数
            canSendAmount = couponThemeStatisticEntity.getTotalCount() - couponThemeStatisticEntity.getSendedCount();
        }

        couponThemeBaseResponse.setCanSendAmount(Math.max(canSendAmount, 0));
        couponThemeBaseResponse.setAvailableCoupons(Math.max(availableCoupons, 0));
        //发行总张数
        couponThemeBaseResponse.setTotalLimit(couponThemeStatisticEntity.getTotalCount());

        //已生成张数
        couponThemeBaseResponse.setDrawedCoupons(drawedCoupons);

        //已发放张数
        couponThemeBaseResponse.setSendedCouopns(couponThemeStatisticEntity.getSendedCount());
    }

    /**
     * 适用人群json对象转数字数组
     *
     * @param couponThemeEntity 实体对象
     */
    private List<Integer> convertCrowdScopeId(CouponThemeEntity couponThemeEntity) {
        JSONObject jsonObject = JSON.parseObject(couponThemeEntity.getApplicableUserTypes());
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
     * 初始化查询条件对象
     *
     * @param bo  从控制层转过来的查询条件
     * @param dos 转给mapper的查询条件
     */
    private void initCouponThemeListDo(CouponThemeListBo bo, CouponThemeListDo dos) {
        if (Objects.isNull(bo)) {
            return;
        }

        dos.setId(bo.getId());
        dos.setThemeTitle(bo.getThemeTitle());
        dos.setActivityName(bo.getActivityName());
        dos.setStartTime(bo.getStartTime());
        dos.setEndTime(bo.getEndTime());
        dos.setApplicableUserTypes(bo.getCrowdScopeId());
        dos.setStartItem(bo.getStartItem());
        dos.setItemsPerPage(bo.getItemsPerPage());

        if (Objects.nonNull(bo.getCouponType()) && !Objects.equals(ALL, bo.getCouponType())) {
            dos.setCouponType(bo.getCouponType());
        }

        if (Objects.nonNull(bo.getCouponGiveRule()) && !Objects.equals(ALL, bo.getCouponGiveRule())) {
            dos.setCouponGiveRule(bo.getCouponGiveRule());
        }

        if (Objects.nonNull(bo.getStatus()) && !Objects.equals(ALL, bo.getStatus())) {
            dos.setStatus(bo.getStatus());
        }

        if (Objects.nonNull(bo.getCouponDiscountType()) && !Objects.equals(ALL, bo.getCouponDiscountType())) {
            dos.setCouponDiscountType(bo.getCouponDiscountType());
        }

        if (CollectionUtils.isEmpty(bo.getOrgIds())) {
            return;
        }

        if (OrgLevelEnum.PLATFORM.getOrgLevelCode().equals(bo.getUserOrgLevelCode())) {
            return;
        }
        //拿当前用户的组织权限
        MerchantInfo merchantInfo = midPlatformLoginHelper.getMerchantInfoByUserId(bo.getUserId().toString());
        Set<Long> authMerchantSet = merchantInfo.getAuthMerchantList().stream().map(AuthMerchantDTO::getMerchantId).collect(Collectors.toSet());
        StoreInfo storeInfo = midPlatformLoginHelper.getStoreInfoByUserId(bo.getUserId().toString());
        Set<Long> authStoreSet = storeInfo.getAuthStoreList().stream().map(AuthStoreDTO::getStoreId).collect(Collectors.toSet());
        authMerchantSet.addAll(authStoreSet);

        List<Long> orgIdList = bo.getOrgIds().stream().filter(authMerchantSet::contains).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(orgIdList)) {
            return;
        }

        dos.setOrgIdList(orgIdList);
    }

    /**
     * 营销中心->优惠券管理->优惠券运营位管理->添加优惠券->查询优惠券列表
     *
     * @param bo 查询条件
     * @return
     */
    @Override
    public PageResponse<CouponThemePositionMarketingResponse> listPositionMarketingCouponTheme(CouponThemePositionMarketingBo bo) {
        //初始化查询条件
        CouponThemePositionMarketingDo dos = new CouponThemePositionMarketingDo();
        initCouponThemePositionMarketingDo(bo, dos);

        //统计总数
        int total = this.baseMapper.listPositionMarketingCouponThemeCount(dos);
        List<CouponThemeEntity> couponThemeEntityList = this.baseMapper.listPositionMarketingCouponTheme(dos);

        PageResponse<CouponThemePositionMarketingResponse> pageResponse = new PageResponse<>();
        List<CouponThemePositionMarketingResponse> listObjs = new ArrayList<>();
        pageResponse.setListObj(listObjs);
        pageResponse.setTotal(total);

        //查询结果为空直接返回
        if (CollectionUtils.isEmpty(couponThemeEntityList)) {
            return pageResponse;
        }

        List<Long> couponThemeIdList = couponThemeEntityList.stream().map(CouponThemeEntity::getId).collect(Collectors.toList());
        List<CouponThemeStatisticEntity> couponThemeStatisticEntityList = couponThemeStatisticService.listByIds(couponThemeIdList);
        Map<Long, CouponThemeStatisticEntity> couponThemeStatisticEntityMap = couponThemeStatisticEntityList.stream().collect(Collectors.toMap(CouponThemeStatisticEntity::getCouponThemeId, item -> item));

        List<CouponThemeOrgEntity> couponThemeOrgEntityList = this.couponThemeOrgService.listByCouponThemeIds(couponThemeIdList);
        Map<Long, List<CouponThemeOrgEntity>> couponThemeOrgEntityMap = couponThemeOrgEntityList.stream().collect(Collectors.groupingBy(CouponThemeOrgEntity::getCouponThemeId));
        List<Long> orgIdList = couponThemeOrgEntityList == null ? new ArrayList<>() : couponThemeOrgEntityList.stream().map(o -> o.getOrgId()).distinct().collect(Collectors.toList());
        List<OrgOutDto> orgInfoAoList = this.getOrgInfoByOrgIdsFromRemote(orgIdList);
        Map<Long, OrgOutDto> orgInfoMap = orgInfoAoList.stream().collect(Collectors.toMap(OrgOutDto::getOrgId, item -> item));

        couponThemeEntityList.stream().forEach(couponThemeEntity -> {
            CouponThemePositionMarketingResponse couponThemePositionMarketingResponse = new CouponThemePositionMarketingResponse();
            //拷贝基本数据项
            BeanUtils.copyProperties(couponThemeEntity, couponThemePositionMarketingResponse, CROWD_SCOPE_IDS);

            //券面额
            couponThemePositionMarketingResponse.setCouponAmount(couponThemeEntity.getDiscountAmount());

            //单独设置适用人群属性值
            List<Integer> crowdScopeIdList = convertCrowdScopeId(couponThemeEntity);
            couponThemePositionMarketingResponse.setCrowdScopeIds(crowdScopeIdList);

            //单独设置适用人群中文名称
            List<String> crowdScopeNameList = convertCrowdScopeName(crowdScopeIdList);
            couponThemePositionMarketingResponse.setCrowdScopedStr(String.join("、", crowdScopeNameList));

            //设置张数
            convertCount(couponThemeStatisticEntityMap, couponThemeEntity, couponThemePositionMarketingResponse);

            //设置状态
            convertStatus(couponThemePositionMarketingResponse, couponThemeEntity);

            //设置券面额
            convertCouponAmount(couponThemePositionMarketingResponse, couponThemeEntity);

            //设置公司
            convertCouponOrg(couponThemeOrgEntityMap, orgInfoMap, couponThemeEntity, couponThemePositionMarketingResponse);

            listObjs.add(couponThemePositionMarketingResponse);
        });

        return pageResponse;
    }

    /**
     * 初始化查询条件对象
     *
     * @param bo  从控制层转过来的查询条件
     * @param dos 转给mapper的查询条件
     */
    private void initCouponThemePositionMarketingDo(CouponThemePositionMarketingBo bo, CouponThemePositionMarketingDo dos) {
        if (Objects.isNull(bo)) {
            return;
        }

        dos.setStartItem(bo.getStartItem());
        dos.setItemsPerPage(bo.getItemsPerPage());
        dos.setThemeTitle(bo.getThemeTitle());
        dos.setRangeRuleType(bo.getRangeRuleType());
        dos.setApplicableUserTypes(bo.getCrowdScopeIds());

        if (Objects.nonNull(bo.getCouponGiveRule()) && !Objects.equals(ALL, bo.getCouponGiveRule())) {
            dos.setCouponGiveRule(bo.getCouponGiveRule());
        }

        if (Objects.nonNull(bo.getStatus()) && !Objects.equals(ALL, bo.getStatus())) {
            dos.setStatus(bo.getStatus());
        }

        if (OrgLevelEnum.PLATFORM.getOrgLevelCode().equals(bo.getUserOrgLevelCode())) {
            return;
        }
        //拿当前用户的组织权限
        MerchantInfo merchantInfo = midPlatformLoginHelper.getMerchantInfoByUserId(bo.getUserId().toString());
        Set<Long> authMerchantSet = merchantInfo.getAuthMerchantList().stream().map(AuthMerchantDTO::getMerchantId).collect(Collectors.toSet());
        StoreInfo storeInfo = midPlatformLoginHelper.getStoreInfoByUserId(bo.getUserId().toString());
        Set<Long> authStoreSet = storeInfo.getAuthStoreList().stream().map(AuthStoreDTO::getStoreId).collect(Collectors.toSet());
        authMerchantSet.addAll(authStoreSet);

        List<Long> orgIdList = authMerchantSet.stream().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(orgIdList)) {
            return;
        }
        dos.setOrgIdList(orgIdList);

        if (CollectionUtils.isEmpty(bo.getRangeList())) {
            return;
        }

        List<OrgRangeAo> allOrgList = getOrgRangeByUtFromRemote(bo.getUt());
        if (CollectionUtils.isEmpty(allOrgList)) {
            return;
        }

        if (!Objects.equals(bo.getRangeRuleType(), 0)) {
            return;
        }

        convertOrgIdList(bo, dos, allOrgList);

    }

    /**
     * 设置组织id
     *
     * @param bo         入参
     * @param dos        转给mapper的入参
     * @param allOrgList 当前用户的组织权限
     */
    private void convertOrgIdList(CouponThemePositionMarketingBo bo, CouponThemePositionMarketingDo dos, List<OrgRangeAo> allOrgList) {
        // 装载rangeList的orgCode
        for (OrgRangeAo r : bo.getRangeList()) {
            for (OrgRangeAo all : allOrgList) {
                if (all.getOrgId().equals(r.getOrgId())) {
                    r.setOrgCode(all.getOrgCode());
                    r.setParentCode(all.getParentCode());
                    break;
                }
            }
        }

        //构建parentcode-List<OrgRangeAo>
        Map<String, OrgRangeAo> mapCodeOrg = allOrgList.stream().collect(Collectors.toMap(OrgRangeAo::getOrgCode, item -> item));

        Map<Integer, Set<Long>> levelOrgIdsMap = new HashMap<>();
        for (OrgRangeAo range : bo.getRangeList()) {
            if (range.getLevel() == 0) {
                break;
            }
            Set<Long> setOrgIds = levelOrgIdsMap.computeIfAbsent(getUseRuleType(range.getLevel()), k -> new HashSet<>());
            setOrgIds.add(range.getOrgId());
            recurPutInfo(mapCodeOrg, levelOrgIdsMap, range);
        }

        // 从1-3层 每层勾选的大小,包括隐式勾选的
        if (levelOrgIdsMap.size() > 1) {
            Integer levelSize1 = CollectionUtils.isEmpty(levelOrgIdsMap.get(getUseRuleType(1))) ? null : levelOrgIdsMap.get(getUseRuleType(1)).size();
            Integer levelSize2 = CollectionUtils.isEmpty(levelOrgIdsMap.get(getUseRuleType(2))) ? null : levelOrgIdsMap.get(getUseRuleType(2)).size();
            Integer levelSize3 = CollectionUtils.isEmpty(levelOrgIdsMap.get(getUseRuleType(3))) ? null : levelOrgIdsMap.get(getUseRuleType(3)).size();
            if (levelSize1 != null && levelSize2 != null && levelSize1 > levelSize2) {
                levelOrgIdsMap.remove(getUseRuleType(2));
            }
            if (levelSize1 != null && levelSize3 != null && levelSize1 > levelSize3) {
                levelOrgIdsMap.remove(getUseRuleType(3));
            }
            if (levelSize2 != null && levelSize3 != null && levelSize2 > levelSize3) {
                levelOrgIdsMap.remove(getUseRuleType(3));
            }
        }

        for (Map.Entry<Integer, Set<Long>> e : levelOrgIdsMap.entrySet()) {
            Integer k = e.getKey();
            Set<Long> v = e.getValue();

            if (k.equals(1)) {
                dos.setRangeMerchantIdList(new ArrayList<>(v));
            } else if (k.equals(6)) {
                dos.setRangeStoreIdList(new ArrayList<>(v));
            } else if (k.equals(11)) {
                dos.setRangeGroupIdList(new ArrayList<>(v));
            }
        }
    }

    /**
     * 通过token调用接口拿当前登录用户的组织权限列表
     *
     * @param ut
     * @return
     */
    private List<OrgRangeAo> getOrgRangeByUtFromRemote(String ut) {
        Map<String, Object> input = new HashMap<>();
        List<OrgRangeAo> allOrgList = ouserWebFeignClient.queryMerchantTree(input, "ut=" + ut).getData().getListObj();
        return allOrgList;
    }

    /**
     * 递归查出子组织
     *
     * @param mapCodeOrg
     * @param levelOrgIdsMap
     * @param range
     */
    private void recurPutInfo(Map<String, OrgRangeAo> mapCodeOrg, Map<Integer, Set<Long>> levelOrgIdsMap, OrgRangeAo range) {
        OrgRangeAo parentOrg = mapCodeOrg.get(range.getParentCode());
        if (Objects.isNull(parentOrg) || Objects.equals(parentOrg.getLevel(), 0)) {
            return;
        }

        Set<Long> setOrgIds = levelOrgIdsMap.computeIfAbsent(getUseRuleType(parentOrg.getLevel()), k -> new HashSet<>());
        setOrgIds.add(parentOrg.getOrgId());
        recurPutInfo(mapCodeOrg, levelOrgIdsMap, parentOrg);
    }

    /**
     * @param level 级别 1集团 2商家 3店铺
     * @return
     */
    private int getUseRuleType(Integer level) {
        switch (level) {
            case 1: {
                return 11;
            }
            case 2: {
                return 1;
            }
            case 3: {
                return 6;
            }
            default: {
                return 0;
            }
        }
    }

    /**
     * 营销中心->主动营销->营销任务管理->编辑任务流->添加优惠券->查询优惠券列表
     *
     * @param bo 查询条件
     * @return
     */
    @Override
    public PageResponse<CouponThemeInitiativeMarketingResponse> listInitiativeMarketingCouponTheme(CouponThemeInitiativeMarketingBo bo) {
        //初始化查询条件
        CouponThemeInitiativeMarketingDo dos = new CouponThemeInitiativeMarketingDo();
        initCouponThemeInitiativeMarketingDo(bo, dos);

        //统计总数
        int total = this.baseMapper.listInitiativeMarketingCouponThemeCount(dos);
        List<CouponThemeEntity> couponThemeEntityList = this.baseMapper.listInitiativeMarketingCouponTheme(dos);

        PageResponse<CouponThemeInitiativeMarketingResponse> pageResponse = new PageResponse<>();
        List<CouponThemeInitiativeMarketingResponse> listObjs = new ArrayList<>();
        pageResponse.setListObj(listObjs);
        pageResponse.setTotal(total);

        //查询结果为空直接返回
        if (CollectionUtils.isEmpty(couponThemeEntityList)) {
            return pageResponse;
        }

        List<Long> couponThemeIdList = couponThemeEntityList.stream().map(CouponThemeEntity::getId).collect(Collectors.toList());
        List<CouponThemeStatisticEntity> couponThemeStatisticEntityList = couponThemeStatisticService.listByIds(couponThemeIdList);
        Map<Long, CouponThemeStatisticEntity> couponThemeStatisticEntityMap = couponThemeStatisticEntityList.stream().collect(Collectors.toMap(CouponThemeStatisticEntity::getCouponThemeId, item -> item));

        List<CouponThemeOrgEntity> couponThemeOrgEntityList = this.couponThemeOrgService.listByCouponThemeIds(couponThemeIdList);
        Map<Long, List<CouponThemeOrgEntity>> couponThemeOrgEntityMap = couponThemeOrgEntityList.stream().collect(Collectors.groupingBy(CouponThemeOrgEntity::getCouponThemeId));
        List<Long> orgIdList = couponThemeOrgEntityList == null ? new ArrayList<>() : couponThemeOrgEntityList.stream().map(o -> o.getOrgId()).distinct().collect(Collectors.toList());
        List<OrgOutDto> orgInfoAoList = this.getOrgInfoByOrgIdsFromRemote(orgIdList);
        Map<Long, OrgOutDto> orgInfoMap = orgInfoAoList.stream().collect(Collectors.toMap(OrgOutDto::getOrgId, item -> item));

        couponThemeEntityList.stream().forEach(couponThemeEntity -> {
            CouponThemeInitiativeMarketingResponse couponThemeInitiativeMarketingResponse = new CouponThemeInitiativeMarketingResponse();
            //拷贝基本数据项
            BeanUtils.copyProperties(couponThemeEntity, couponThemeInitiativeMarketingResponse, CROWD_SCOPE_IDS);

            //单独设置适用人群属性值
            List<Integer> crowdScopeIdList = convertCrowdScopeId(couponThemeEntity);
            couponThemeInitiativeMarketingResponse.setCrowdScopeIds(crowdScopeIdList);

            //单独设置适用人群中文名称
            List<String> crowdScopeNameList = convertCrowdScopeName(crowdScopeIdList);
            couponThemeInitiativeMarketingResponse.setCrowdScopedStr(String.join("、", crowdScopeNameList));

            //设置张数
            convertCount(couponThemeStatisticEntityMap, couponThemeEntity, couponThemeInitiativeMarketingResponse);

            //设置状态
            convertStatus(couponThemeInitiativeMarketingResponse, couponThemeEntity);

            //设置券面额
            convertCouponAmount(couponThemeInitiativeMarketingResponse, couponThemeEntity);

            //设置公司
            convertCouponOrg(couponThemeOrgEntityMap, orgInfoMap, couponThemeEntity, couponThemeInitiativeMarketingResponse);

            listObjs.add(couponThemeInitiativeMarketingResponse);
        });

        return pageResponse;
    }

    /**
     * 初始化查询条件对象
     *
     * @param bo  从控制层转过来的查询条件
     * @param dos 转给mapper的查询条件
     */
    private void initCouponThemeInitiativeMarketingDo(CouponThemeInitiativeMarketingBo bo, CouponThemeInitiativeMarketingDo dos) {
        if (Objects.isNull(bo)) {
            return;
        }

        dos.setId(bo.getId());
        dos.setThemeTitle(bo.getThemeTitle());
        dos.setApplicableUserTypes(bo.getCrowdScopeIds());
        dos.setStartItem(bo.getStartItem());
        dos.setItemsPerPage(bo.getItemsPerPage());

        if (Objects.nonNull(bo.getCouponGiveRule()) && !Objects.equals(ALL, bo.getCouponGiveRule())) {
            dos.setCouponGiveRule(bo.getCouponGiveRule());
        }

        if (Objects.nonNull(bo.getStatus()) && !Objects.equals(ALL, bo.getStatus())) {
            dos.setStatus(bo.getStatus());
        }

        if (CollectionUtils.isEmpty(bo.getOrgIds())) {
            return;
        }

        if (OrgLevelEnum.PLATFORM.getOrgLevelCode().equals(bo.getUserOrgLevelCode())) {
            return;
        }

        //拿当前用户的组织权限
        MerchantInfo merchantInfo = midPlatformLoginHelper.getMerchantInfoByUserId(bo.getUserId().toString());
        Set<Long> authMerchantSet = merchantInfo.getAuthMerchantList().stream().map(AuthMerchantDTO::getMerchantId).collect(Collectors.toSet());
        StoreInfo storeInfo = midPlatformLoginHelper.getStoreInfoByUserId(bo.getUserId().toString());
        Set<Long> authStoreSet = storeInfo.getAuthStoreList().stream().map(AuthStoreDTO::getStoreId).collect(Collectors.toSet());
        authMerchantSet.addAll(authStoreSet);

        List<Long> orgIdList = authMerchantSet.stream().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(orgIdList)) {
            return;
        }
        dos.setOrgIdList(orgIdList);
    }

    /**
     * 管理后台->营销中心->优惠券管理->优惠券活动列表->导出Excel
     *
     * @param bo 查询条件
     * @return
     */
    private PageResponse<CouponThemeExportResponse> listExportData(CouponThemeExportBo bo) {
        //初始化查询条件
        CouponThemeExportDo dos = new CouponThemeExportDo();
        initCouponThemeExportDo(bo, dos);

        //统计总数
        int total = this.baseMapper.listExportCouponThemeCount(dos);
        List<CouponThemeEntity> couponThemeEntityList = this.baseMapper.listExportCouponTheme(dos);

        PageResponse<CouponThemeExportResponse> pageResponse = new PageResponse<>();
        List<CouponThemeExportResponse> listObjs = new ArrayList<>();
        pageResponse.setListObj(listObjs);
        pageResponse.setTotal(total);

        //查询结果为空直接返回
        if (CollectionUtils.isEmpty(couponThemeEntityList)) {
            return pageResponse;
        }

        List<Long> couponThemeIdList = couponThemeEntityList.stream().map(CouponThemeEntity::getId).collect(Collectors.toList());
        List<CouponThemeStatisticEntity> couponThemeStatisticEntityList = couponThemeStatisticService.listByIds(couponThemeIdList);
        Map<Long, CouponThemeStatisticEntity> couponThemeStatisticEntityMap = couponThemeStatisticEntityList.stream().collect(Collectors.toMap(CouponThemeStatisticEntity::getCouponThemeId, item -> item));

        List<CouponThemeOrgEntity> couponThemeOrgEntityList = this.couponThemeOrgService.listByCouponThemeIds(couponThemeIdList);
        Map<Long, List<CouponThemeOrgEntity>> couponThemeOrgEntityMap = couponThemeOrgEntityList.stream().collect(Collectors.groupingBy(CouponThemeOrgEntity::getCouponThemeId));
        List<Long> orgIdList = couponThemeOrgEntityList == null ? new ArrayList<>() : couponThemeOrgEntityList.stream().map(o -> o.getOrgId()).distinct().collect(Collectors.toList());
        List<OrgOutDto> orgInfoAoList = this.getOrgInfoByOrgIdsFromRemote(orgIdList);
        Map<Long, OrgOutDto> orgInfoMap = orgInfoAoList.stream().collect(Collectors.toMap(OrgOutDto::getOrgId, item -> item));

        Map<Integer, List<SendedAndUsedCouponDto>> sendedAndUsedCouponDtoMap = new HashMap<>();
        List<Integer> statuses = new ArrayList<>();
        statuses.add(CouponStatusEnum.STATUS_USE.getStatus());
        statuses.add(CouponStatusEnum.STATUS_USED.getStatus());
        //循环状态，单个读取
        for (Integer status : statuses) {
            Map<String, Object> params = new HashMap<>();
            params.put("couponThemeIds", couponThemeIdList);
            params.put("status", status);
            sendedAndUsedCouponDtoMap.put(status, couponUserService.countSendedAndUsedCoupons(params));
        }

        couponThemeEntityList.stream().forEach(couponThemeEntity -> {
            CouponThemeExportResponse couponThemeExportResponse = new CouponThemeExportResponse();
            //拷贝基本数据项
            BeanUtils.copyProperties(couponThemeEntity, couponThemeExportResponse, CROWD_SCOPE_IDS);

            //券面额
            couponThemeExportResponse.setCouponAmount(couponThemeEntity.getDiscountAmount());

            //单独设置适用人群属性值
            List<Integer> crowdScopeIdList = convertCrowdScopeId(couponThemeEntity);
            couponThemeExportResponse.setCrowdScopeIds(crowdScopeIdList);

            //单独设置适用人群中文名称
            List<String> crowdScopeNameList = convertCrowdScopeName(crowdScopeIdList);
            couponThemeExportResponse.setCrowdScopedStr(String.join("、", crowdScopeNameList));

            //设置发行总张数、已经生成的张数、还可生成的张数
            convertCountExport(couponThemeStatisticEntityMap, couponThemeEntity, couponThemeExportResponse);

            //设置状态
            Integer status = convertStatus(couponThemeEntity);
            couponThemeExportResponse.setStatus(status);

            //设置券面额
            convertCouponAmountExport(couponThemeExportResponse, couponThemeEntity);
            //设置所属于公司
            convertCouponThemeOrgInfo(couponThemeExportResponse, couponThemeOrgEntityMap.get(couponThemeEntity.getId()), orgInfoMap);

            //设置已领取张数、已使用张数
            convertCouponSendedAndUsedCount(couponThemeExportResponse, sendedAndUsedCouponDtoMap);

            //填充导出额外字段
            populateExtraExportField(couponThemeExportResponse, couponThemeEntity);

            listObjs.add(couponThemeExportResponse);
        });

        return pageResponse;
    }


    private void populateExtraExportField(CouponThemeExportResponse couponThemeExportResponse, CouponThemeEntity couponThemeEntity) {
        couponThemeExportResponse.setCouponGiveRuleName(CouponGiveRuleEnum.getStrByType(couponThemeExportResponse.getCouponGiveRule()));
        couponThemeExportResponse.setCouponDiscountTypeName(CouponDiscountType.getNameByType(couponThemeExportResponse.getCouponDiscountType()));
        couponThemeExportResponse.setCouponTypeName(CouponTypeEnum.getNameByType(couponThemeExportResponse.getCouponType()));
        //券面额
        if (CouponDiscountType.DISCOUNT.getType().equals(couponThemeEntity.getCouponDiscountType())) {
            //折扣
            BigDecimal discount = new BigDecimal(couponThemeEntity.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            couponThemeExportResponse.setCouponAdmountName(MessageFormat.format("{0}折，折扣上限{1}元", CommonConstant.DECIMAL_FORMAT.format(discount), couponThemeEntity.getDiscountAmount()));
        } else {
            couponThemeExportResponse.setCouponAdmountName(CommonConstant.DECIMAL_FORMAT.format(couponThemeEntity.getDiscountAmount()) + "元");
        }

        if (couponThemeEntity.getStartTime() != null && couponThemeEntity.getEndTime() != null) {
            couponThemeExportResponse.setStartEndTime(DateUtil.format(couponThemeEntity.getStartTime(), "yyy-MM-dd HH:mm:ss") + " - " + DateUtil.format(couponThemeEntity.getEndTime(), "yyy-MM-dd HH:mm:ss"));
        }
        if (couponThemeEntity.getUseLimit() != null) {
            if (couponThemeEntity.getUseLimit().compareTo(BigDecimal.ZERO) == 0) {
                couponThemeExportResponse.setUseCondition("无限制");
            } else {
                couponThemeExportResponse.setUseCondition("满" + couponThemeEntity.getUseLimit() + "元可用");
            }
        }
        couponThemeExportResponse.setOrgNameStr(String.join(",", couponThemeExportResponse.getOrgNames()));
        couponThemeExportResponse.setStatusName(CouponStatusEnum.getStrByStatus(couponThemeExportResponse.getStatus()));

    }

    /**
     * 设置发行总张数、已经生成的张数、已领取的张数、已使用的张数、还可生成的张数
     *
     * @param couponThemeStatisticEntityMap 统计信息
     * @param couponThemeEntity             实体对象
     * @param couponThemeExportResponse     返回前端的数据对象
     */
    private void convertCountExport(Map<Long, CouponThemeStatisticEntity> couponThemeStatisticEntityMap, CouponThemeEntity couponThemeEntity, CouponThemeExportResponse couponThemeExportResponse) {
        CouponThemeStatisticEntity couponThemeStatisticEntity = couponThemeStatisticEntityMap.get(couponThemeEntity.getId());

        //还可生成张数
        int availableCoupons = couponThemeStatisticEntity.getTotalCount() - couponThemeStatisticEntity.getCreatedCount();
        //还可发放张数
        int canSendAmount = couponThemeStatisticEntity.getCreatedCount() - couponThemeStatisticEntity.getSendedCount();
        //已生成张数
        int drawedCoupons = couponThemeStatisticEntity.getCreatedCount();

        couponThemeExportResponse.setCanSendAmount(Math.max(canSendAmount, 0));
        couponThemeExportResponse.setAvailableCoupons(Math.max(availableCoupons, 0));
        //发行总张数
        couponThemeExportResponse.setTotalLimit(couponThemeStatisticEntity.getTotalCount());

        //已生成张数
        couponThemeExportResponse.setDrawedCoupons(drawedCoupons);

        //已发放张数
        couponThemeExportResponse.setSendedCouopns(couponThemeStatisticEntity.getSendedCount());
    }

    /**
     * 初始化查询条件对象
     *
     * @param bo  从控制层转过来的查询条件
     * @param dos 转给mapper的查询条件
     */
    private void initCouponThemeExportDo(CouponThemeExportBo bo, CouponThemeExportDo dos) {
        if (Objects.isNull(bo)) {
            return;
        }

        dos.setId(bo.getId());
        dos.setThemeTitle(bo.getThemeTitle());
        dos.setActivityName(bo.getActivityName());
        dos.setStartTime(bo.getStartTime());
        dos.setEndTime(bo.getEndTime());
        dos.setCrowdScopeId(bo.getCrowdScopeId());
        dos.setStartItem(bo.getStartItem());
        dos.setItemsPerPage(bo.getItemsPerPage());

        if (Objects.nonNull(bo.getCouponType()) && !Objects.equals(ALL, bo.getCouponType())) {
            dos.setCouponType(bo.getCouponType());
        }

        if (Objects.nonNull(bo.getCouponGiveRule()) && !Objects.equals(ALL, bo.getCouponGiveRule())) {
            dos.setCouponGiveRule(bo.getCouponGiveRule());
        }

        if (Objects.nonNull(bo.getStatus()) && !Objects.equals(ALL, bo.getStatus())) {
            dos.setStatus(bo.getStatus());
        }

        if (Objects.nonNull(bo.getCouponDiscountType()) && !Objects.equals(ALL, bo.getCouponDiscountType())) {
            dos.setCouponDiscountType(bo.getCouponDiscountType());
        }

        if (CollectionUtils.isEmpty(bo.getOrgIds())) {
            return;
        }

        if (OrgLevelEnum.PLATFORM.getOrgLevelCode().equals(bo.getUserOrgLevelCode())) {
            return;
        }
        //拿当前用户的组织权限
        MerchantInfo merchantInfo = midPlatformLoginHelper.getMerchantInfoByUserId(bo.getUserId().toString());
        Set<Long> authMerchantSet = merchantInfo.getAuthMerchantList().stream().map(AuthMerchantDTO::getMerchantId).collect(Collectors.toSet());
        StoreInfo storeInfo = midPlatformLoginHelper.getStoreInfoByUserId(bo.getUserId().toString());
        Set<Long> authStoreSet = storeInfo.getAuthStoreList().stream().map(AuthStoreDTO::getStoreId).collect(Collectors.toSet());
        authMerchantSet.addAll(authStoreSet);

        List<Long> orgIdList = bo.getOrgIds().stream().filter(authMerchantSet::contains).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(orgIdList)) {
            return;
        }
        dos.setOrgIdList(orgIdList);
    }

    /**
     * 转换券面额
     *
     * @param couponThemeExportResponse
     * @param entity
     */
    private void convertCouponAmountExport(CouponThemeExportResponse couponThemeExportResponse, CouponThemeEntity entity) {
        //券折扣
        if (Objects.equals(CouponDiscountType.DISCOUNT.getType(), entity.getCouponDiscountType())) {
            BigDecimal discount = new BigDecimal(entity.getDiscountValue() != null ? entity.getDiscountValue() : 0);
            BigDecimal divisor = new BigDecimal(100);

            couponThemeExportResponse.setCouponAmount(discount.divide(divisor, 2, BigDecimal.ROUND_HALF_UP));
            couponThemeExportResponse.setUseUpLimit(entity.getDiscountAmount());
            couponThemeExportResponse.setCouponUnit(CouponConstant.COUPON_UNIT_DISCOUNT);
            couponThemeExportResponse.setRuleType(CouponRuleType.DISCOUNT.getType());
        }

        //券金额
        if (Objects.equals(CouponDiscountType.CASH.getType(), entity.getCouponDiscountType())) {
            couponThemeExportResponse.setCouponAmount(entity.getDiscountAmount());
            couponThemeExportResponse.setCouponAmountExt1(BigDecimal.ZERO);
            couponThemeExportResponse.setCouponUnit(CouponConstant.COUPON_UNIT_AMOUNT);
            couponThemeExportResponse.setRuleType(CouponRuleType.AMOUNT.getType());
        }

        //是否可赠送
        if (Objects.equals(CouponDiscountType.WELFARE_CARD.getType(), entity.getCouponDiscountType())) {
            if (Objects.equals(entity.getCanDonation(), YesNoEnum.YES.getValue())) {
                couponThemeExportResponse.setCanDonation(Boolean.TRUE);
            } else if (Objects.equals(entity.getCanDonation(), YesNoEnum.NO.getValue())) {
                couponThemeExportResponse.setCanDonation(Boolean.FALSE);
            }

        }

    }

    /**
     * 设置券所属组织
     *
     * @param couponThemeExportResponse
     * @param couponThemeOrgEntityList
     */
    private void convertCouponThemeOrgInfo(CouponThemeExportResponse couponThemeExportResponse, List<CouponThemeOrgEntity> couponThemeOrgEntityList, Map<Long, OrgOutDto> orgInfoMap) {
        List<Long> orgIdList = couponThemeOrgEntityList.stream().map(o -> o.getOrgId()).distinct().collect(Collectors.toList());
        couponThemeExportResponse.setOrgIds(orgIdList);

        List<String> orgNameList = orgIdList.stream().map(o -> {
            if (Objects.isNull(orgInfoMap)) {
                return org.apache.commons.lang.StringUtils.EMPTY;
            }
            return Objects.isNull(orgInfoMap.get(o)) ? org.apache.commons.lang.StringUtils.EMPTY : orgInfoMap.get(o).getOrgName();
        }).collect(Collectors.toList());
        couponThemeExportResponse.setOrgNames(orgNameList);
    }

    /**
     * 通过orgIds调用接口拿组织列表
     *
     * @param orgIdList
     * @return
     */
    private List<OrgOutDto> getOrgInfoByOrgIdsFromRemote(List<Long> orgIdList) {
        if (CollectionUtils.isEmpty(orgIdList)) {
            return new ArrayList<>();
        }

        OrgIdsInput orgIdsInput = new OrgIdsInput();
        orgIdsInput.setIds(orgIdList);
        InputDto<OrgIdsInput> inputDto = new InputDto<>();
        inputDto.setData(orgIdsInput);
        OutputDto<List<OrgOutDto>> orgOutInfo = ouserWebFeignClient.findByOrgId(inputDto);
        return orgOutInfo.getData();
    }

    /**
     * 设置 已领取的张数、已使用的张数
     *
     * @param couponThemeExportResponse
     * @param sendedAndUsedCouponDtoMap
     */
    private void convertCouponSendedAndUsedCount(CouponThemeExportResponse couponThemeExportResponse, Map<Integer, List<SendedAndUsedCouponDto>> sendedAndUsedCouponDtoMap) {

        List<SendedAndUsedCouponDto> sendedCouponDtoList = sendedAndUsedCouponDtoMap.get(CouponStatusEnum.STATUS_USE.getStatus());
        List<SendedAndUsedCouponDto> usededCouponDtoList = sendedAndUsedCouponDtoMap.get(CouponStatusEnum.STATUS_USED.getStatus());
        if (CollectionUtils.isEmpty(sendedCouponDtoList) && CollectionUtils.isEmpty(usededCouponDtoList)) {
            return;
        }

        //已经领取张数
        sendedCouponDtoList.stream().forEach(item -> {
            if (Objects.equals(item.getCouponThemeId(), couponThemeExportResponse.getId())) {
                couponThemeExportResponse.setSendedCouopns(item.getTotal());
                return;
            }
        });

        //已经使用张数
        usededCouponDtoList.stream().forEach(item -> {
            if (Objects.equals(item.getCouponThemeId(), couponThemeExportResponse.getId())) {
                couponThemeExportResponse.setUsedCouopns(item.getTotal());
                return;
            }
        });


    }


    /**
     * 通过组织id关联优惠券活动id
     *
     * @param orgIds
     * @return
     */
    @Override
    public List<Long> queryAuthThemeId(List<Long> orgIds) {
        return this.getBaseMapper().queryAuthThemeId(orgIds);
    }


    @Override
    public Long exportCouponThemeListAsync(CouponThemeExportBo bo) {
        //判断有没有记录
        bo.setCurrentPage(1);
        bo.setItemsPerPage(Integer.MAX_VALUE);
        PageResponse<CouponThemeExportResponse> exportResponse = listExportData(bo);
        if (exportResponse.getTotal() == 0) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_NOT_EXIST);
        }
        Long generateBatchId = saveCouponGenerateBatch(exportResponse.getTotal());
        //开始异步导出
        String traceId = MDC.get(InfraConstant.TRACE_ID);
        couponBatchExecutor.execute(() -> {
            MDC.put(InfraConstant.TRACE_ID, traceId);
            try {
                //导出并上次excel文件
                FileUploadResultDto exportResult = exportCouponThemeAndUpload(exportResponse.getListObj());
                //更新到导出记录
                updateCouponGenerateBatch(generateBatchId, exportResult);
            } catch (Exception ex) {
                log.error("导出券活动列表异常", ex);
            } finally {
                MDC.remove(InfraConstant.TRACE_ID);
            }
        });
        return generateBatchId;
    }

    private Long saveCouponGenerateBatch(Integer total) {
        CouponGenerateBatchEntity couponGenerateBatchEntity = new CouponGenerateBatchEntity();
        couponGenerateBatchEntity.setType(CouponBatchTypeEnum.LOG_TYPE_EXPORT_COUPON_THEME.getType());
        couponGenerateBatchEntity.setThemeId(0L);
        couponGenerateBatchEntity.setTotalRecord(total);
        couponGenerateBatchEntity.setSendCouponStatus(AsyncStatusEnum.SENDING.getStatus());
        couponGenerateBatchEntity.setGenerateNums(0);
        UserInfo userInfo = AuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        couponGenerateBatchEntity.setCreateUserid(userInfo.getUserId());
        couponGenerateBatchEntity.setCreateUsername(userInfo.getUsername());
        couponGenerateBatchService.save(couponGenerateBatchEntity);
        return couponGenerateBatchEntity.getId();
    }

    private void updateCouponGenerateBatch(Long generateBatchId, FileUploadResultDto exportResult) {
        CouponGenerateBatchEntity generateBatchEntity = new CouponGenerateBatchEntity();
        generateBatchEntity.setId(generateBatchId);
        generateBatchEntity.setSendCouponStatus(exportResult.getStatusEnum().getStatus());
        generateBatchEntity.setUploadFile(exportResult.getUploadFile());
        generateBatchEntity.setFailReason(exportResult.getErrorReason());
        generateBatchEntity.setFinishTime(new Date());
        couponGenerateBatchService.updateById(generateBatchEntity);
    }

    private FileUploadResultDto exportCouponThemeAndUpload(List<CouponThemeExportResponse> couponThemeExportResponses) {
        ByteArrayOutputStream outputStream = null;
        ByteArrayInputStream inputStream = null;
        try {
            //导出到文件
            outputStream = new ByteArrayOutputStream();
            List<Object> rowDatas = couponThemeExportResponses.stream().map(m -> (Object) m).collect(Collectors.toList());
            excelExporter.export(outputStream, CouponThemeExportResponse.class, rowDatas);
            //上传文件
            byte[] barray = outputStream.toByteArray();
            inputStream = new ByteArrayInputStream(barray);
            String fileName = UUID.randomUUID().toString().replace("-", "") + ImportConstant.EXCEL_2003_SUFFIX;
            MultipartFile multipartFile = new CommonMultipartFile("file", fileName, ImportConstant.EXCEL_2007_CONTENT_TYPE, inputStream);
            ResponseDto<String> responseDto = commonFileClient.uploadFileByFixedFileName(multipartFile, "/excel/export");
            if (!CouponConstant.SUCCESS_CODE.equals(responseDto.getCode())) {
                log.error("导出优惠券活动上传失败");
                return FileUploadResultDto.builder().statusEnum(AsyncStatusEnum.FAIL).errorReason("导出文件上传失败").build();
            }
            return FileUploadResultDto.builder().statusEnum(AsyncStatusEnum.FINISHED).uploadFile(responseDto.getData()).build();
        } catch (ImportException ex) {
            log.error("导出优惠券活动生成excel文件异常", ex);
            return FileUploadResultDto.builder().statusEnum(AsyncStatusEnum.FAIL).errorReason(ex.getMessage()).build();
        } catch (Exception ex) {
            log.error("导出优惠券活动异常", ex);
            return FileUploadResultDto.builder().statusEnum(AsyncStatusEnum.FAIL).errorReason("导出异常").build();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    log.error("导出优惠券活动IO关闭异常", ex);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ex) {
                    log.error("导出优惠券活动IO关闭异常", ex);
                }
            }
        }
    }

    /**
     * 管理后台->营销中心->优惠券管理->查看优惠券活动详情
     *
     * @param bo
     * @return
     */
    @Override
    public CouponThemeDetailResponse getCouponThemeDetailById(CouponThemeDetailBo bo) {
        CouponThemeDetailResponse couponThemeDetailResponse = new CouponThemeDetailResponse();
        CouponThemeEntity couponThemeEntity = this.baseMapper.selectById(bo.getId());
        CouponThemeStatisticEntity couponThemeStatisticEntity = couponThemeStatisticService.getById(bo.getId());

        List<Long> couponThemeIdList = Arrays.asList(bo.getId());

        List<CouponThemeOrgEntity> couponThemeOrgEntityList = this.couponThemeOrgService.listByCouponThemeIds(couponThemeIdList);
        Map<Long, List<CouponThemeOrgEntity>> couponThemeOrgEntityMap = couponThemeOrgEntityList.stream().collect(Collectors.groupingBy(CouponThemeOrgEntity::getCouponThemeId));
        List<Long> orgIdList = couponThemeOrgEntityList == null ? new ArrayList<>() : couponThemeOrgEntityList.stream().map(o -> o.getOrgId()).distinct().collect(Collectors.toList());
        List<OrgOutDto> orgInfoAoList = this.getOrgInfoByOrgIdsFromRemote(orgIdList);
        Map<Long, OrgOutDto> orgInfoMap = orgInfoAoList.stream().collect(Collectors.toMap(OrgOutDto::getOrgId, item -> item));

        Map<Integer, List<SendedAndUsedCouponDto>> sendedAndUsedCouponDtoMap = new HashMap<>();
        List<Integer> statuses = new ArrayList<>();
        statuses.add(CouponStatusEnum.STATUS_USE.getStatus());
        statuses.add(CouponStatusEnum.STATUS_USED.getStatus());
        //循环状态，单个读取
        for (Integer status : statuses) {
            Map<String, Object> params = new HashMap<>();
            params.put("couponThemeIds", couponThemeIdList);
            params.put("status", status);
            sendedAndUsedCouponDtoMap.put(status, couponUserService.countSendedAndUsedCoupons(params));
        }

        MktUseRuleCouponThemeDetailBo mktUseRuleCouponThemeDetailBo = new MktUseRuleCouponThemeDetailBo();
        mktUseRuleCouponThemeDetailBo.setThemeRef(bo.getId());
        Map<Integer, List<Long>> mktUseRuleMap = this.mktUseRuleService.getMktUseRuleMap(mktUseRuleCouponThemeDetailBo);

        //拷贝基本数据项
        BeanUtils.copyProperties(couponThemeEntity, couponThemeDetailResponse, CROWD_SCOPE_IDS);

        //单独设置适用人群属性值
        List<Integer> crowdScopeIdList = convertCrowdScopeId(couponThemeEntity);
        couponThemeDetailResponse.setCrowdIds(crowdScopeIdList);

        //单独设置适用人群中文名称
        List<String> crowdScopeNameList = convertCrowdScopeName(crowdScopeIdList);
        couponThemeDetailResponse.setCrowdScopedStr(String.join("、", crowdScopeNameList));

        //设置张数
        convertCountDetail(couponThemeStatisticEntity, couponThemeDetailResponse);

        //设置状态
        Integer status = convertStatus(couponThemeEntity);
        couponThemeDetailResponse.setStatus(status);

        //设置券面额
        convertCouponAmountDetail(couponThemeDetailResponse, couponThemeEntity);

        //设置所属于公司
        convertCouponThemeOrgInfoDetail(couponThemeDetailResponse, couponThemeOrgEntityMap.get(couponThemeEntity.getId()), orgInfoMap);

        //设置已领取张数、已使用张数
        convertCouponSendedAndUsedCountDetail(couponThemeDetailResponse, sendedAndUsedCouponDtoMap);

        //设置rule
        couponThemeDetailResponse.setRuleTypeMap(mktUseRuleMap);
        return couponThemeDetailResponse;
    }

    /**
     * 设置发行总张数、已经生成的张数、已领取的张数、已使用的张数、还可生成的张数
     *
     * @param couponThemeStatisticEntity 统计信息
     * @param couponThemeDetailResponse  返回前端的数据对象
     */
    private void convertCountDetail(CouponThemeStatisticEntity couponThemeStatisticEntity, CouponThemeDetailResponse couponThemeDetailResponse) {

        //还可生成张数
        int availableCoupons = couponThemeStatisticEntity.getTotalCount() - couponThemeStatisticEntity.getCreatedCount();
        //还可发放张数
        int canSendAmount = couponThemeStatisticEntity.getCreatedCount() - couponThemeStatisticEntity.getSendedCount();
        //已生成张数
        int drawedCoupons = couponThemeStatisticEntity.getCreatedCount();

        couponThemeDetailResponse.setCanSendAmount(Math.max(canSendAmount, 0));
        couponThemeDetailResponse.setAvailableCoupons(Math.max(availableCoupons, 0));
        //发行总张数
        couponThemeDetailResponse.setTotalLimit(couponThemeStatisticEntity.getTotalCount());

        //已生成张数
        couponThemeDetailResponse.setDrawedCoupons(drawedCoupons);

        //已发放张数
        couponThemeDetailResponse.setSendedCouopns(couponThemeStatisticEntity.getSendedCount());
    }

    /**
     * 转换券面额
     *
     * @param couponThemeDtailResponse
     * @param entity
     */
    private void convertCouponAmountDetail(CouponThemeDetailResponse couponThemeDtailResponse, CouponThemeEntity entity) {
        //券折扣
        if (Objects.equals(CouponDiscountType.DISCOUNT.getType(), entity.getCouponDiscountType())) {
            BigDecimal discount = new BigDecimal(entity.getDiscountValue());
            BigDecimal divisor = new BigDecimal(100);

            couponThemeDtailResponse.setCouponAmount(entity.getDiscountAmount());
            couponThemeDtailResponse.setCouponAmountExt1(BigDecimal.ZERO);

            couponThemeDtailResponse.setCouponDiscount(discount.divide(divisor, 2, BigDecimal.ROUND_HALF_UP).intValue());
            couponThemeDtailResponse.setUseUpLimit(entity.getDiscountAmount());
            couponThemeDtailResponse.setCouponUnit(CouponConstant.COUPON_UNIT_DISCOUNT);
            couponThemeDtailResponse.setRuleType(CouponRuleType.DISCOUNT.getType());
        }

        //券金额
        if (Objects.equals(CouponDiscountType.CASH.getType(), entity.getCouponDiscountType())) {
            couponThemeDtailResponse.setCouponAmount(entity.getDiscountAmount());
            couponThemeDtailResponse.setCouponAmountExt1(BigDecimal.ZERO);
            couponThemeDtailResponse.setCouponUnit(CouponConstant.COUPON_UNIT_AMOUNT);
            couponThemeDtailResponse.setRuleType(CouponRuleType.AMOUNT.getType());
        }

    }

    /**
     * 设置券所属组织
     *
     * @param couponThemeDetailResponse
     * @param couponThemeOrgEntityList
     */
    private void convertCouponThemeOrgInfoDetail(CouponThemeDetailResponse couponThemeDetailResponse, List<CouponThemeOrgEntity> couponThemeOrgEntityList, Map<Long, OrgOutDto> orgInfoMap) {
        List<Long> orgIdList = couponThemeOrgEntityList.stream().filter(item -> Objects.equals(item.getCouponThemeId(), couponThemeDetailResponse.getId())).map(o -> o.getOrgId()).distinct().collect(Collectors.toList());

        //优惠券的所属组织信息
        List<DetailCouponThemeOrgInfoAo> orgList = orgIdList.stream().map(id -> {
            DetailCouponThemeOrgInfoAo detailCouponThemeOrgInfoAo = new DetailCouponThemeOrgInfoAo();
            if (Objects.isNull(orgInfoMap)) {
                return detailCouponThemeOrgInfoAo;
            }
            OrgOutDto couponThemeOrgInfoAo = orgInfoMap.get(id);
            detailCouponThemeOrgInfoAo.setOrgId(id);
            detailCouponThemeOrgInfoAo.setOrgName(Objects.isNull(couponThemeOrgInfoAo) ? org.apache.commons.lang.StringUtils.EMPTY : couponThemeOrgInfoAo.getOrgName());
            detailCouponThemeOrgInfoAo.setOrgLevelCode(couponThemeOrgInfoAo.getOrgCode());
            return detailCouponThemeOrgInfoAo;
        }).collect(Collectors.toList());
        couponThemeDetailResponse.setOrgList(orgList);

        //费用归属所属组织
        List<OrgOutDto> themeOrgInfoAoList = this.getOrgInfoByOrgIdsFromRemote(Arrays.asList(couponThemeDetailResponse.getBelongingOrgId()));
        List<DetailCouponThemeOrgInfoAo> belongingOrgList = themeOrgInfoAoList.stream().map(item -> {
            DetailCouponThemeOrgInfoAo detailCouponThemeOrgInfoAo = new DetailCouponThemeOrgInfoAo();
            detailCouponThemeOrgInfoAo.setOrgId(item.getOrgId());
            detailCouponThemeOrgInfoAo.setOrgName(item.getOrgName());
            detailCouponThemeOrgInfoAo.setOrgLevelCode(item.getOrgCode());
            return detailCouponThemeOrgInfoAo;
        }).collect(Collectors.toList());
        ;
        couponThemeDetailResponse.setBelongingOrgList(belongingOrgList);
    }

    /**
     * 设置 已领取的张数、已使用的张数
     *
     * @param couponThemeDetailResponse
     * @param sendedAndUsedCouponDtoMap
     */
    private void convertCouponSendedAndUsedCountDetail(CouponThemeDetailResponse couponThemeDetailResponse, Map<Integer, List<SendedAndUsedCouponDto>> sendedAndUsedCouponDtoMap) {

        List<SendedAndUsedCouponDto> sendedCouponDtoList = sendedAndUsedCouponDtoMap.get(CouponStatusEnum.STATUS_USE.getStatus());
        List<SendedAndUsedCouponDto> usededCouponDtoList = sendedAndUsedCouponDtoMap.get(CouponStatusEnum.STATUS_USED.getStatus());
        if (CollectionUtils.isEmpty(sendedCouponDtoList) && CollectionUtils.isEmpty(usededCouponDtoList)) {
            return;
        }

        //已经领取张数
        sendedCouponDtoList.stream().forEach(item -> {
            if (Objects.equals(item.getCouponThemeId(), couponThemeDetailResponse.getId())) {
                couponThemeDetailResponse.setSendedCouopns(item.getTotal());
                return;
            }
        });

        //已经使用张数
        usededCouponDtoList.stream().forEach(item -> {
            if (Objects.equals(item.getCouponThemeId(), couponThemeDetailResponse.getId())) {
                couponThemeDetailResponse.setUsedCouopns(item.getTotal());
                return;
            }
        });

    }


    /*
    cms查询优惠券活动列表
     */
    @Override
    public CouponThemeListCmsResponse listByCms(CouponThemeListCmsRequest request) {
        CouponThemeListCmsResponse response = new CouponThemeListCmsResponse();

        CouponThemeListCmsRequest.CouponThemeListCmsData requestData = request.getData();
        CouponThemeCmsPageQuery<CouponThemeEntity> query = new CouponThemeCmsPageQuery<>(requestData.getCurrentPage(), requestData.getItemsPerPage());
        query.setIds(requestData.getIds());
        query.setCouponGiveRule(requestData.getCouponGiveRule());
        query.setThemeType(requestData.getThemeType());
        query.setCrowdScope(requestData.getCrowdScope());
        query.setStatus(requestData.getStatus());
        query.setLimitFlag(requestData.getLimitFlag());
        query.setMerchantList(requestData.getMerchantList());

        CouponThemeCmsPageQuery<CouponThemeEntity> couponThemePage = this.baseMapper.listByCms(query);
        if (couponThemePage.getTotal() == 0) {
            response.setTotal(0);
            response.setListObj(Collections.EMPTY_LIST);
            return response;
        }

        //查询总库存
        Set<Long> themeIds = couponThemePage.getRecords().stream().map(m -> m.getId()).collect(Collectors.toSet());
        List<CouponThemeStatisticEntity> statisticEntities = couponThemeStatisticService.listByIds(new ArrayList<>(themeIds));
        Map<Long, CouponThemeStatisticEntity> statisticEntityMap = statisticEntities.stream().collect(Collectors.toMap(m -> m.getCouponThemeId(), m -> m));

        List<CouponThemeListCmsResponse.CouponThemeListCmsObj> cmsObjs = new ArrayList<>();
        for (CouponThemeEntity couponThemeEntity : couponThemePage.getRecords()) {
            CouponThemeListCmsResponse.CouponThemeListCmsObj cmsObj = new CouponThemeListCmsResponse.CouponThemeListCmsObj();
            cmsObj.setId(couponThemeEntity.getId());
            cmsObj.setThemeTitle(couponThemeEntity.getThemeTitle());
            cmsObj.setStartTime(couponThemeEntity.getStartTime());
            cmsObj.setEndTime(couponThemeEntity.getEndTime());
            cmsObj.setStatus(couponThemeEntity.getStatus());
            cmsObj.setCouponType(couponThemeEntity.getCouponType());
            CouponThemeStatisticEntity statisticEntity = statisticEntityMap.get(couponThemeEntity.getId());
            if (statisticEntity != null) {
                cmsObj.setTotalLimit(statisticEntity.getTotalCount());
                cmsObj.setDrawedCoupons(statisticEntity.getCreatedCount());
                cmsObj.setSendedCouopns(statisticEntity.getSendedCount());
            }
            cmsObj.setCreateTime(couponThemeEntity.getCreateTime());
            cmsObj.setCouponGiveRule(couponThemeEntity.getCouponGiveRule());
            cmsObj.setIndividualLimit(couponThemeEntity.getIndividualLimit());
            cmsObj.setUseLimit(couponThemeEntity.getUseLimit());
            cmsObj.setRemark(couponThemeEntity.getRemark());
            cmsObj.setThemeType(couponThemeEntity.getThemeType());
            if (CouponDiscountType.DISCOUNT.getType().equals(couponThemeEntity.getCouponDiscountType())) {
                cmsObj.setCouponAmount(BigDecimal.valueOf(couponThemeEntity.getDiscountValue()));
            } else {
                cmsObj.setCouponAmount(couponThemeEntity.getDiscountAmount());
            }
            cmsObj.setCouponDiscountType(couponThemeEntity.getCouponDiscountType());
            cmsObjs.add(cmsObj);
        }
        response.setListObj(cmsObjs);

        return response;
    }


}
