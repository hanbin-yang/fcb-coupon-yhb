package com.fcb.coupon.backend.business.couponCreate.impl;

import cn.hutool.core.date.DateUtil;
import com.fcb.coupon.backend.business.couponCreate.CouponBatchCreateBusiness;
import com.fcb.coupon.backend.exception.CouponThemeErrorCode;
import com.fcb.coupon.backend.model.bo.CouponImportBo;
import com.fcb.coupon.backend.model.bo.GenerateCouponBo;
import com.fcb.coupon.backend.model.cache.CouponThemeCache;
import com.fcb.coupon.backend.model.dto.*;
import com.fcb.coupon.backend.model.entity.*;
import com.fcb.coupon.backend.model.param.request.CouponThirdImportRequest;
import com.fcb.coupon.backend.model.param.response.CouponImportErrorResponse;
import com.fcb.coupon.backend.model.query.LambdaFieldNameSelector;
import com.fcb.coupon.backend.remote.client.CommonFileClient;
import com.fcb.coupon.backend.service.*;
import com.fcb.coupon.common.constant.CouponConstant;
import com.fcb.coupon.common.constant.InfraConstant;
import com.fcb.coupon.common.constant.RedisCacheKeyConstant;
import com.fcb.coupon.common.constant.RedisLockKeyConstant;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.enums.*;
import com.fcb.coupon.common.excel.constant.ImportConstant;
import com.fcb.coupon.common.excel.excetption.ImportException;
import com.fcb.coupon.common.excel.exporter.ExcelExporter;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.file.CommonMultipartFile;
import com.fcb.coupon.common.util.AESPromotionUtil;
import com.fcb.coupon.common.util.CodeUtil;
import com.fcb.coupon.common.util.RedisUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月09日 20:06:00
 */
@Slf4j
@Service
public class CouponBatchCreateBusinessImpl implements CouponBatchCreateBusiness {

    @Autowired
    private ThreadPoolTaskExecutor couponBatchExecutor;
    @Autowired
    private CouponThemeService couponThemeService;
    @Autowired
    private CouponGenerateBatchService couponGenerateBatchService;
    @Autowired
    private CouponThemeStatisticService couponThemeStatisticService;
    @Autowired
    private CouponService couponService;
    @Autowired
    private CouponThirdService couponThirdService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private CouponThemeCacheService couponThemeCacheService;
    @Autowired
    @Qualifier("couponCommonExecutor")
    private ThreadPoolTaskExecutor couponCommonExecutor;
    @Autowired
    private CouponOprLogService couponOprLogService;
    @Autowired
    private ExcelExporter excelExporter;
    @Autowired
    private CommonFileClient commonFileClient;


    @Override
    public Long batchImportThird(CouponImportBo couponImportBo) {
        List<CouponThirdImportRequest> couponThirdImportRequests = couponImportBo.getThirdImportRequestList();
        //检查券号、密码是否重复
        validateDataRepeat(couponThirdImportRequests);

        //判断还能不能导入
        validateImportCount(couponImportBo.getThemeId(), couponThirdImportRequests.size());

        //校验优惠券活动
        CouponThemeEntity couponThemeEntity = couponThemeService.getById(couponImportBo.getThemeId());
        if (couponThemeEntity == null) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_NOT_EXIST);
        }
        if (!CouponTypeEnum.COUPON_TYPE_THIRD.getType().equals(couponThemeEntity.getCouponType())) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_IMPORT_NOT_ALLOW);
        }

        //密码加密
        couponThirdImportRequests.forEach(m -> {
            m.setThirdCouponPassword(AESPromotionUtil.encrypt(m.getThirdCouponPassword()));
        });

        //异步导入前添加基于券活动的分布式锁 同一时间一个优惠券活动只允许一个导入操作
        final String lockKey = RedisLockKeyConstant.getThirdImportLockKey(couponImportBo.getThemeId());
        //未获取到分布式锁，5分钟过期
        if (!RedisUtil.tryLock(lockKey, couponImportBo.getThemeId().toString(), 5 * 60)) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_IMPORT_LOCK_FAIL);
        }

        try {
            //查询存在的第三方券
            validateDataExists(couponImportBo.getThemeId(), couponThirdImportRequests);
            //导入上下文
            List<CouponImportContext> couponImportContexts = buildImportContexts(couponImportBo);
            //写入导入记录
            Long generateBatchId = saveCouponGenerateBatch(couponImportBo);
            //开始异步导入
            String traceId = MDC.get(InfraConstant.TRACE_ID);
            couponBatchExecutor.execute(() -> {
                MDC.put(InfraConstant.TRACE_ID, traceId);
                try {
                    CouponImportResultDto couponImportResultDto = batchImportThirdCoupons(couponImportContexts, couponThemeEntity);
                    //上传导入结果文件
                    FileUploadResultDto fileUploadResult = exportImportResultAndUpload(couponImportResultDto, couponThemeEntity);
                    //更新导入记录
                    updateCouponGenerateBatch(generateBatchId, couponImportResultDto, fileUploadResult);
                } catch (Exception ex) {
                    log.error("异步导入优惠券异常", ex);
                } finally {
                    MDC.remove(InfraConstant.TRACE_ID);
                    RedisUtil.tryUnLock(lockKey, couponImportBo.getThemeId().toString());
                }
            });
            return generateBatchId;
        } catch (BusinessException ex) {
            RedisUtil.tryUnLock(lockKey, couponImportBo.getThemeId().toString());
            throw ex;
        } catch (Exception ex) {
            log.error("第三方券导入异常", ex);
            RedisUtil.tryUnLock(lockKey, couponImportBo.getThemeId().toString());
            throw new BusinessException(CouponThemeErrorCode.COUPON_IMPORT_ERROR);
        }
    }


    @Override
    public boolean batchGenerateCoupon(GenerateCouponBo bo) {
        // 准备couponTheme相关信息
        CouponThemeCache couponThemeCache = prepareCouponThemeCacheForGenerateCoupons(bo.getCouponThemeId());
        if (Objects.isNull(couponThemeCache)) {
            log.error("券活动不存在 couponThemeCache null: couponThemeId={}", bo.getCouponThemeId());
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_NOT_EXIST);
        }

        // 还可生券数
        Integer canGenerateCount = couponThemeCache.getTotalCount() - couponThemeCache.getCreatedCount();
        // 生券数量超限
        if (canGenerateCount.compareTo(bo.getGenerateAmount()) < 0) {
            log.error("生券数量超限, couponThemeId={}, canGenerateCount={}, needGenerateCount={}", bo.getCouponThemeId(), canGenerateCount, bo.getGenerateAmount());
            throw new BusinessException(CouponThemeErrorCode.GENERATE_COUPONS_OUT_OF_LIMIT);
        }

        Integer effDateCalcMethod = couponThemeCache.getEffDateCalcMethod();

        Date couponStartTime = null;
        Date couponEndTime = null;
        // 如果指定有效期，设置coupon表startTime和endTime
        if (CouponEffDateCalType.FIXED.getType().equals(effDateCalcMethod)) {
            couponStartTime = couponThemeCache.getEffDateStartTime();
            couponEndTime = couponThemeCache.getEffDateEndTime();
        }

        Queue<Long> couponIds = RedisUtil.generateIds(bo.getGenerateAmount());
        List<CouponEntity> couponEntityList = new ArrayList<>();
        for (int i = 0; i < bo.getGenerateAmount(); i++) {
            CouponEntity couponEntity = prepareCouponBeanForGenerateCoupons(bo, couponThemeCache, couponIds);
            couponEntity.setStartTime(couponStartTime);
            couponEntity.setEndTime(couponEndTime);
            couponEntityList.add(couponEntity);
        }

        couponService.generateCouponsWithTx(couponEntityList);

        String traceId = MDC.get(InfraConstant.TRACE_ID);
        couponCommonExecutor.execute(() -> {
            try {
                MDC.put(InfraConstant.TRACE_ID, traceId);
                couponService.syncCouponEs(couponEntityList);
                // 异步日志
                OprLogDo oprLogDo = OprLogDo.builder()
                        .oprUserId(bo.getUserId())
                        .oprUserName(bo.getUsername())
                        .oprContent(String.format("生券数量: %s张", bo.getGenerateAmount()))
                        .refId(bo.getCouponThemeId())
                        .oprThemeType(LogOprThemeType.COUPON_THEME)
                        .oprType(LogOprType.GENERATE_COUPONS)
                        .build();
                couponOprLogService.saveOprLog(oprLogDo);
            } finally {
                MDC.remove(InfraConstant.TRACE_ID);
            }
        });

        return true;
    }


    private List<CouponImportContext> buildImportContexts(CouponImportBo couponImportBo) {
        List<CouponImportContext> couponImportContexts = new ArrayList<>(couponImportBo.getThirdImportRequestList().size());
        for (int i = 0; i < couponImportBo.getThirdImportRequestList().size(); i++) {
            CouponThirdImportRequest importRequest = couponImportBo.getThirdImportRequestList().get(i);
            CouponImportContext importContext = CouponImportContext.builder()
                    .index(i)
                    .thirdCouponCode(importRequest.getThirdCouponCode())
                    .thirdCouponPassword(importRequest.getThirdCouponPassword())
                    .createUserid(couponImportBo.getUserId())
                    .createUsername(couponImportBo.getUsername())
                    .isFailure(false)
                    .build();
            couponImportContexts.add(importContext);
        }
        return couponImportContexts;
    }

    private void validateDataRepeat(List<CouponThirdImportRequest> couponThirdImportRequests) {
        Set<String> thirdCouponCodeSet = new HashSet<>();
        Set<String> thirdCouponPasswordSet = new HashSet<>();
        for (CouponThirdImportRequest couponThirdImportRequest : couponThirdImportRequests) {
            if (StringUtils.isNotBlank(couponThirdImportRequest.getThirdCouponCode()) && thirdCouponCodeSet.contains(couponThirdImportRequest.getThirdCouponCode())) {
                throw new BusinessException(CouponThemeErrorCode.COUPON_IMPORT_DATA_REPEAT_ERROR.getCode(), "第三方卡号/优惠券码【" + couponThirdImportRequest.getThirdCouponCode() + "】重复，请检查");
            }
            if (thirdCouponPasswordSet.contains(couponThirdImportRequest.getThirdCouponPassword())) {
                throw new BusinessException(CouponThemeErrorCode.COUPON_IMPORT_DATA_REPEAT_ERROR.getCode(), "密码【" + couponThirdImportRequest.getThirdCouponPassword() + "】存在重复，请检查");
            }
            thirdCouponCodeSet.add(couponThirdImportRequest.getThirdCouponCode());
            thirdCouponPasswordSet.add(couponThirdImportRequest.getThirdCouponPassword());
        }
    }

    private void validateImportCount(Long themeId, int importCount) {
        CouponThemeStatisticEntity statisticEntity = couponThemeStatisticService.getById(themeId);
        if (statisticEntity == null) {
            return;
        }
        if (statisticEntity.getCreatedCount() + importCount > statisticEntity.getTotalCount()) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_IMPORT_OUT_TOTAL_ERROR);
        }
    }

    private void validateDataExists(Long themeId, List<CouponThirdImportRequest> couponThirdImportRequests) {
        //先查询数据库存在的第三方券码和密码
        List<CouponThirdEntity> couponThirdEntities = couponThirdService.listByThemeId(themeId);
        if (CollectionUtils.isEmpty(couponThirdEntities)) {
            return;
        }
        //校验券码是否存在
        Set<String> thirdCouponCodeSet = couponThirdEntities.stream()
                .filter(m -> StringUtils.isNotBlank(m.getThirdCouponCode()))
                .map(m -> m.getThirdCouponCode()).collect(Collectors.toSet());
        Set<String> thirdCouponPasswordSet = couponThirdEntities.stream()
                .filter(m -> StringUtils.isNotBlank(m.getThirdCouponPassword()))
                .map(m -> m.getThirdCouponPassword()).collect(Collectors.toSet());
        //检查券码是否重复
        for (int i = 0; i < couponThirdImportRequests.size(); i++) {
            CouponThirdImportRequest couponImport = couponThirdImportRequests.get(i);
            if (StringUtils.isNotBlank(couponImport.getThirdCouponCode()) && thirdCouponCodeSet.contains(couponImport.getThirdCouponCode())) {
                throw new BusinessException(CouponThemeErrorCode.COUPON_IMPORT_DATA_REPEAT_ERROR.getCode(), "第【" + (i + 1) + "】行第三方卡号/优惠券码已存在，不能重复导入");
            }
            if (thirdCouponPasswordSet.contains(couponImport.getThirdCouponPassword())) {
                throw new BusinessException(CouponThemeErrorCode.COUPON_IMPORT_DATA_REPEAT_ERROR.getCode(), "第【" + (i + 1) + "】行密码存在重复，不能重复导入");
            }
        }

    }

    private Long saveCouponGenerateBatch(CouponImportBo bo) {
        CouponGenerateBatchEntity couponGenerateBatchEntity = new CouponGenerateBatchEntity();
        couponGenerateBatchEntity.setType(CouponBatchTypeEnum.LOG_TYPE_IMPORT_COUPON.getType());
        couponGenerateBatchEntity.setThemeId(bo.getThemeId());
        couponGenerateBatchEntity.setTotalRecord(bo.getThirdImportRequestList().size());
        couponGenerateBatchEntity.setSendCouponStatus(AsyncStatusEnum.SENDING.getStatus());
        couponGenerateBatchEntity.setGenerateNums(couponGenerateBatchEntity.getTotalRecord());
        couponGenerateBatchEntity.setCreateUserid(bo.getUserId());
        couponGenerateBatchEntity.setCreateUsername(bo.getUsername());
        couponGenerateBatchService.save(couponGenerateBatchEntity);
        return couponGenerateBatchEntity.getId();
    }

    private List<CouponThirdMergedDto> buildCouponThirdMergedDtos(List<CouponImportContext> couponImportContexts, CouponThemeEntity couponThemeEntity) {
        // 如果券有效期计算方式为固定有效期，从coupon_theme_config表获取固定有效期
        Date startDate = null;
        Date endDate = null;
        if (CouponEffDateCalType.FIXED.getType().equals(couponThemeEntity.getEffDateCalcMethod())) {
            startDate = couponThemeEntity.getEffDateStartTime();
            endDate = couponThemeEntity.getEffDateEndTime();
        }

        List<CouponThirdMergedDto> couponThirdMergedDtos = new ArrayList<>(couponImportContexts.size());
        Queue<Long> idList = RedisUtil.generateIds(couponImportContexts.size());
        for (CouponImportContext couponImportContext : couponImportContexts) {
            CouponThirdMergedDto couponThirdMergedDto = new CouponThirdMergedDto();
            CouponEntity couponEntity = new CouponEntity();
            couponEntity.setId(idList.poll());
            couponEntity.setCouponCode(AESPromotionUtil.encrypt(CodeUtil.generateCouponCode()));
            couponEntity.setSource(CouponSourceTypeEnum.COUPON_SOURCE_THIRD_PARTY.getSource());
            couponEntity.setCouponDiscountType(couponThemeEntity.getCouponDiscountType());
            if (CouponDiscountType.DISCOUNT.getType().equals(couponThemeEntity.getCouponDiscountType())) {
                couponEntity.setCouponValue(BigDecimal.valueOf(couponThemeEntity.getDiscountValue()));
            } else {
                couponEntity.setCouponValue(couponThemeEntity.getDiscountAmount());
            }
            couponEntity.setCouponThemeId(couponThemeEntity.getId());
            couponEntity.setThemeTitle(couponThemeEntity.getThemeTitle());
            couponEntity.setCouponType(CouponTypeEnum.COUPON_TYPE_THIRD.getType());
            couponEntity.setStatus(CouponStatusEnum.STATUS_ISSUE.getStatus());
            couponEntity.setStartTime(startDate);
            couponEntity.setEndTime(endDate);
            //设置第三方券码和密码
            couponEntity.setIsDeleted(YesNoEnum.NO.getValue());
            couponEntity.setCreateTime(new Date());
            couponEntity.setCreateUserid(couponImportContext.getCreateUserid());
            couponEntity.setCreateUsername(couponImportContext.getCreateUsername());
            couponThirdMergedDto.setCouponEntity(couponEntity);

            CouponThirdEntity couponThirdEntity = new CouponThirdEntity();
            couponThirdEntity.setCouponId(couponEntity.getId());
            couponThirdEntity.setCouponThemeId(couponEntity.getCouponThemeId());
            couponThirdEntity.setThirdCouponCode(couponImportContext.getThirdCouponCode());
            couponThirdEntity.setThirdCouponPassword(couponImportContext.getThirdCouponPassword());
            couponThirdMergedDto.setCouponThirdEntity(couponThirdEntity);

            couponThirdMergedDtos.add(couponThirdMergedDto);
        }

        return couponThirdMergedDtos;
    }


    private CouponImportResultDto batchImportThirdCoupons(List<CouponImportContext> couponImportContexts, CouponThemeEntity couponThemeEntity) {
        try {
            //导入数据分批，select in 查询数据库不易太大
            List<List<CouponImportContext>> partList = Lists.partition(couponImportContexts, 500);
            //并行导入数据，里面是并发执行
            long startTime = System.currentTimeMillis();
            //成功数量
            AtomicInteger successCount = new AtomicInteger(0);
            //导入异常数量
            AtomicInteger errorCount = new AtomicInteger(0);
            //并行导入券码
            String traceId = MDC.get(InfraConstant.TRACE_ID);
            partList.parallelStream().forEach((partCouponImportContexts) -> {
                MDC.put(InfraConstant.TRACE_ID, traceId);
                try {
                    doBatchImportThirdCoupons(partCouponImportContexts, couponThemeEntity);
                    successCount.getAndAdd(partCouponImportContexts.size());
                    log.info("导入第三方券码子任务完成,导入成功数量={}", partCouponImportContexts.size());
                } catch (BusinessException ex) {
                    log.error("导入第三方券码子任务异常,导入失败数量=" + partCouponImportContexts.size(), ex);
                    errorCount.getAndAdd(partCouponImportContexts.size());
                    partCouponImportContexts.forEach(m -> {
                        m.setIsFailure(true);
                        m.setFailureReason(ex.getMessage());
                    });
                } catch (Exception ex) {
                    log.error("导入第三方券码子任务异常,导入失败数量=" + partCouponImportContexts.size(), ex);
                    errorCount.getAndAdd(partCouponImportContexts.size());
                    partCouponImportContexts.forEach(m -> {
                        m.setIsFailure(true);
                        m.setFailureReason("导入异常");
                    });
                } finally {
                    MDC.remove(InfraConstant.TRACE_ID);
                }
            });
            long endTime = System.currentTimeMillis();
            log.info("导入第三方券码完成，导入数量={},成功数量={},失败数量={},导入耗时={}ms", couponImportContexts.size(), successCount.get(), errorCount.get(), endTime - startTime);

            return CouponImportResultDto.builder()
                    .couponImportContexts(couponImportContexts)
                    .asyncStatusEnum(AsyncStatusEnum.FINISHED)
                    .totalCount(couponImportContexts.size())
                    .successCount(successCount.get())
                    .errorCount(errorCount.get()).build();
        } catch (Exception ex) {
            log.error("第三方优惠券导入异常", ex);
            return CouponImportResultDto.builder()
                    .couponImportContexts(couponImportContexts)
                    .asyncStatusEnum(AsyncStatusEnum.FAIL)
                    .totalCount(couponImportContexts.size())
                    .successCount(0)
                    .errorCount(couponImportContexts.size()).build();
        }
    }


    private void doBatchImportThirdCoupons(List<CouponImportContext> couponImportContexts, CouponThemeEntity couponThemeEntity) {
        //构建保存数据
        List<CouponThirdMergedDto> couponThirdMergedDtos = buildCouponThirdMergedDtos(couponImportContexts, couponThemeEntity);
        List<CouponEntity> couponEntities = new ArrayList<>(couponThirdMergedDtos.size());
        List<CouponThirdEntity> couponThirdEntities = new ArrayList<>(couponThirdMergedDtos.size());
        for (CouponThirdMergedDto couponThirdMergedDto : couponThirdMergedDtos) {
            couponEntities.add(couponThirdMergedDto.getCouponEntity());
            couponThirdEntities.add(couponThirdMergedDto.getCouponThirdEntity());
        }

        //生成券到数据库
        couponService.generateThirdCouponsWithTx(couponEntities, couponThirdEntities);
        //同步券到redis队列
        syncCouponIdToRedisList(couponEntities, couponThemeEntity);
        //同步es
        couponService.syncCouponEs(couponEntities);
    }

    private void updateCouponGenerateBatch(Long generateBatchId, CouponImportResultDto couponImportResultDto, FileUploadResultDto fileUploadResult) {
        CouponGenerateBatchEntity generateBatchEntity = new CouponGenerateBatchEntity();
        generateBatchEntity.setId(generateBatchId);
        generateBatchEntity.setSendCouponStatus(couponImportResultDto.getAsyncStatusEnum().getStatus());
        generateBatchEntity.setTotalRecord(couponImportResultDto.getTotalCount());
        generateBatchEntity.setSuccessRecord(couponImportResultDto.getSuccessCount());
        generateBatchEntity.setFailRecord(couponImportResultDto.getErrorCount());
        generateBatchEntity.setFinishTime(new Date());
        generateBatchEntity.setUploadFile(fileUploadResult.getUploadFile());
        generateBatchEntity.setFailReason(fileUploadResult.getErrorReason());
        couponGenerateBatchService.updateById(generateBatchEntity);
    }

    /*
    同步券Id到redis的list，todo 这里后期需要优化成异步
     */
    private void syncCouponIdToRedisList(List<CouponEntity> couponEntities, CouponThemeEntity couponThemeEntity) {
        String listKey = RedisCacheKeyConstant.COUPON_THIRD_COUPON_LIST + couponThemeEntity.getId();
        List<String> couponIds = couponEntities.stream().map(m -> m.getId().toString()).collect(Collectors.toList());
        stringRedisTemplate.opsForList().rightPushAll(listKey, couponIds);
        stringRedisTemplate.expireAt(listKey, couponThemeEntity.getEndTime());
    }


    private CouponThemeCache prepareCouponThemeCacheForGenerateCoupons(Long couponThemeId) {
        LambdaFieldNameSelector<CouponThemeCache> selector = new LambdaFieldNameSelector<>();
        selector.select(CouponThemeCache::getTotalCount)
                .select(CouponThemeCache::getCreatedCount)
                .select(CouponThemeCache::getEffDateCalcMethod)
                .select(CouponThemeCache::getEffDateStartTime)
                .select(CouponThemeCache::getEffDateEndTime)
                .select(CouponThemeCache::getEffDateDays)
                .select(CouponThemeCache::getCouponDiscountType)
                .select(CouponThemeCache::getDiscountAmount)
                .select(CouponThemeCache::getDiscountValue)
                .select(CouponThemeCache::getCouponType)
                .select(CouponThemeCache::getThemeType)
        ;
        return couponThemeCacheService.getById(couponThemeId, selector);
    }


    private CouponEntity prepareCouponBeanForGenerateCoupons(GenerateCouponBo bo, CouponThemeCache couponThemeCache, Queue<Long> couponIds) {
        CouponEntity couponEntity = new CouponEntity();
        couponEntity.setId(couponIds.poll());
        couponEntity.setCouponThemeId(bo.getCouponThemeId());
        couponEntity.setThemeTitle(couponThemeCache.getThemeTitle());

        couponEntity.setStatus(CouponStatusEnum.STATUS_ISSUE.getStatus());
        couponEntity.setSource(bo.getSource());

        couponEntity.setCouponType(couponThemeCache.getCouponType());
        String couponCode = CodeUtil.generateCouponCode();
        couponEntity.setCouponCode(AESPromotionUtil.encrypt(couponCode));

        couponEntity.setCreateUserid(bo.getUserId());
        couponEntity.setCreateUsername(bo.getUsername());
        couponEntity.setIsDeleted(CouponConstant.NO);

        if (CouponDiscountType.DISCOUNT.getType().equals(couponThemeCache.getCouponDiscountType())) {
            couponEntity.setCouponValue(BigDecimal.valueOf(couponThemeCache.getDiscountValue()));
        } else {
            couponEntity.setCouponValue(couponThemeCache.getDiscountAmount());
        }

        return couponEntity;
    }


    public FileUploadResultDto exportImportResultAndUpload(CouponImportResultDto couponImportResultDto, CouponThemeEntity couponTheme) {
        //构造导出结果
        List<CouponImportErrorResponse> errorResponses = new ArrayList<>();
        for (int i = 0; i < couponImportResultDto.getCouponImportContexts().size(); i++) {
            CouponImportContext couponImportContext = couponImportResultDto.getCouponImportContexts().get(i);
            CouponImportErrorResponse errorResponse = new CouponImportErrorResponse();
            errorResponse.setIndex(i + 1);
            errorResponse.setCouponName(couponTheme.getThemeTitle());
            errorResponse.setOprtName("导入券码");
            errorResponse.setStartEndTime(DateUtil.format(couponTheme.getStartTime(), "yyyy-MM-dd HH:mm:ss") + " - " + DateUtil.format(couponTheme.getEndTime(), "yyyy-MM-dd HH:mm:ss"));
            if (StringUtils.isNotBlank(couponImportContext.getThirdCouponCode()) && couponImportContext.getThirdCouponCode().length() > 3) {
                String left = StringUtils.left(couponImportContext.getThirdCouponCode(), 3);
                int size = couponImportContext.getThirdCouponCode().length() - 3;
                String right = StringUtils.rightPad("", size, '*');
                errorResponse.setThirdCouponCode(left + right);
            }
            errorResponse.setStatusName(couponImportContext.getIsFailure() ? "失败" : "成功");
            errorResponse.setReason(couponImportContext.getFailureReason());
            errorResponses.add(errorResponse);
        }

        //上传文件
        return doExportImportResultAndUpload(errorResponses);
    }

    private FileUploadResultDto doExportImportResultAndUpload(List<CouponImportErrorResponse> errorResponses) {
        ByteArrayOutputStream outputStream = null;
        ByteArrayInputStream inputStream = null;
        try {
            //导出到文件
            outputStream = new ByteArrayOutputStream();
            excelExporter.export(outputStream, CouponImportErrorResponse.class, errorResponses);
            //上传文件
            byte[] barray = outputStream.toByteArray();
            inputStream = new ByteArrayInputStream(barray);
            String fileName = UUID.randomUUID().toString().replace("-", "") + ImportConstant.EXCEL_2003_SUFFIX;
            MultipartFile multipartFile = new CommonMultipartFile("file", fileName, ImportConstant.EXCEL_2007_CONTENT_TYPE, inputStream);
            ResponseDto<String> responseDto = commonFileClient.uploadFileByFixedFileName(multipartFile, "/excel/couponImport");
            if (!CouponConstant.SUCCESS_CODE.equals(responseDto.getCode())) {
                log.error("导入券码结果文件上传失败");
                return FileUploadResultDto.builder().statusEnum(AsyncStatusEnum.FAIL).errorReason("导入券码结果文件上传失败").build();
            }
            return FileUploadResultDto.builder().statusEnum(AsyncStatusEnum.FINISHED).uploadFile(responseDto.getData()).build();
        } catch (ImportException ex) {
            log.error("导入券码结果excel文件异常", ex);
            return FileUploadResultDto.builder().statusEnum(AsyncStatusEnum.FAIL).errorReason(ex.getMessage()).build();
        } catch (Exception ex) {
            log.error("导入券码结果文件上传异常", ex);
            return FileUploadResultDto.builder().statusEnum(AsyncStatusEnum.FAIL).errorReason("导入券码结果文件上传异常").build();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    log.error("导入券码结果IO关闭异常", ex);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ex) {
                    log.error("导入券码结果IO关闭异常", ex);
                }
            }
        }
    }


}
