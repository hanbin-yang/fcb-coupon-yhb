package com.fcb.coupon.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fcb.coupon.backend.elasticsearch.CouponEsDoc;
import com.fcb.coupon.backend.exception.CouponVerificationErrorCode;
import com.fcb.coupon.backend.exception.MktUseRuleErrorCode;
import com.fcb.coupon.backend.listener.event.MinCouponEvent;
import com.fcb.coupon.backend.mapper.CouponVerificationMapper;
import com.fcb.coupon.backend.model.bo.CouponSingleVerifyBo;
import com.fcb.coupon.backend.model.bo.CouponVerificationListBo;
import com.fcb.coupon.backend.model.bo.BatchVerifyBo;
import com.fcb.coupon.backend.model.dto.*;
import com.fcb.coupon.backend.model.entity.*;
import com.fcb.coupon.backend.model.param.request.CouponVerifyImportRequest;
import com.fcb.coupon.backend.model.param.response.CouponVerificationDetailResponse;
import com.fcb.coupon.backend.model.param.response.CouponVerificationExportResponse;
import com.fcb.coupon.backend.model.param.response.CouponVerificationListResponse;
import com.fcb.coupon.backend.model.param.response.PageResponse;
import com.fcb.coupon.backend.model.query.LambdaFieldNameSelector;
import com.fcb.coupon.backend.remote.client.OuserWebFeignClient;
import com.fcb.coupon.backend.remote.client.CustomerClient;
import com.fcb.coupon.backend.remote.client.BrokerClient;
import com.fcb.coupon.backend.remote.client.CommonFileClient;
import com.fcb.coupon.backend.remote.dto.input.*;
import com.fcb.coupon.backend.remote.dto.out.AgencyInfoOutputDto;
import com.fcb.coupon.backend.remote.dto.out.BrokerInfoSimpleDto;
import com.fcb.coupon.backend.remote.dto.out.CustomerInfoSimpleOutput;
import com.fcb.coupon.backend.remote.dto.out.OutputDto;
import com.fcb.coupon.backend.service.*;
import com.fcb.coupon.common.constant.*;
import com.fcb.coupon.common.dto.*;
import com.fcb.coupon.common.enums.*;
import com.fcb.coupon.common.excel.bean.RowParseResult;
import com.fcb.coupon.common.excel.constant.ImportConstant;
import com.fcb.coupon.common.excel.excetption.ImportException;
import com.fcb.coupon.common.excel.exporter.ExcelExporter;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.exception.CommonErrorCode;
import com.fcb.coupon.common.exception.ResponseErrorCode;
import com.fcb.coupon.common.file.CommonMultipartFile;
import com.fcb.coupon.common.midplatform.MidPlatformLoginHelper;
import com.fcb.coupon.common.util.AESPromotionUtil;
import com.fcb.coupon.backend.model.cache.CouponThemeCache;
import com.fcb.coupon.common.util.RedisUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author HanBin_Yang
 * @since 2021/6/23 9:16
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@RefreshScope
public class CouponVerificationServiceImpl extends ServiceImpl<CouponVerificationMapper, CouponVerificationEntity> implements CouponVerificationService {
    private final CouponThemeCacheService couponThemeCacheService;
    private final CouponService couponService;
    private final ExcelExporter excelExporter;
    private final MidPlatformLoginHelper midPlatformLoginHelper;
    private final CouponGenerateBatchService couponGenerateBatchService;
    private final CouponUserService couponUserService;
    @Autowired
    CouponVerificationService couponVerificationService;
    @Autowired
    private ThreadPoolTaskExecutor couponBatchExecutor;

    @Override
    @Transactional
    public void couponVerifyWithTx(CouponVerificationEntity entity, Integer oldCouponVersionNo, Boolean isOfflineCoupon) {
        // 更新coupon表的状态为已使用
        updateCouponStatusForVerification(entity, oldCouponVersionNo, isOfflineCoupon);
        // coupon_verification表
        baseMapper.insertOrUpdate(entity);
    }

    private void updateCouponStatusForVerification(CouponVerificationEntity entity, Integer oldCouponVersionNo, Boolean isOfflineCoupon) {
        CouponEntity couponEntity = new CouponEntity();
        couponEntity.setId(entity.getCouponId());
        couponEntity.setStatus(entity.getStatus());
        couponEntity.setUpdateUserid(entity.getCreateUserid());
        couponEntity.setUpdateUsername(entity.getCreateUsername());
        // 版本校验
        couponEntity.setVersionNo(oldCouponVersionNo);
        // coupon表
        boolean couponResult = couponService.updateById(couponEntity);
        if (!couponResult) {
            log.error("核销操作coupon表，版本校验失败 entity={}, oldCouponVersionNo={}", JSON.toJSONString(entity), oldCouponVersionNo);
            throw new BusinessException(CouponVerificationErrorCode.COUPON_VERSION_NOT_MATCH);
        }

        // 非线下预制券，更新coupon_user表
        if (!Boolean.TRUE.equals(isOfflineCoupon)) {
            LambdaUpdateWrapper<CouponUserEntity> updateWrapper = Wrappers.lambdaUpdate(CouponUserEntity.class);
            updateWrapper.set(CouponUserEntity::getStatus, entity.getStatus())
                    .eq(CouponUserEntity::getCouponId, entity.getCouponId())
                    .eq(CouponUserEntity::getStatus, CouponStatusEnum.STATUS_USE.getStatus());
            boolean couponUserResult = couponUserService.update(updateWrapper);
            if (!couponUserResult) {
                log.error("核销操作couponUser表，行锁校验失败 entity={}, oldCouponVersionNo={}", JSON.toJSONString(entity), oldCouponVersionNo);
                throw new BusinessException(CouponVerificationErrorCode.COUPON_VERSION_NOT_MATCH);
            }
        }
    }

    /**
     * 查询核销列表
     * @param bo
     * @return
     */
    @Override
    public PageResponse<CouponVerificationListResponse> list(CouponVerificationListBo bo) {
        PageResponse<CouponVerificationListResponse> pageResponse = new PageResponse<>();

        LambdaQueryWrapper<CouponVerificationEntity> queryWrapper = prepareQueryWrapperForSelective(bo);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<CouponVerificationEntity> pg = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>();
        pg.setCurrent(bo.getCurrentPage());
        pg.setSize(bo.getItemsPerPage());
        // 不查总数
        pg.setSearchCount(false);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<CouponVerificationEntity> resultPage = baseMapper.selectPage(pg, queryWrapper);
        List<CouponVerificationEntity> couponVerificationEntityList = resultPage.getRecords();
        //查询结果为空直接返回
        if (CollectionUtils.isEmpty(couponVerificationEntityList)) {
            return pageResponse;
        }

        List<CouponVerificationListResponse> listObj = new ArrayList<>();
        pageResponse.setListObj(listObj);
        couponVerificationEntityList.forEach(entity->{
            CouponVerificationListResponse couponVerificationListResponse = new CouponVerificationListResponse();
            //拷贝基本数据项
            BeanUtils.copyProperties(entity, couponVerificationListResponse);

            couponVerificationListResponse.setId(entity.getCouponId());

            couponVerificationListResponse.setCreateTime(entity.getCouponCreateTime());
            couponVerificationListResponse.setCrowdScopeId(entity.getUserType());

            // 设置优惠券价值返回
            BigDecimal couponValue = entity.getCouponValue();
            if (Objects.nonNull(couponValue)) {
                if (CouponDiscountType.DISCOUNT.getType().equals(entity.getCouponDiscountType())) {
                    String discount = couponValue.divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).toString().replaceAll("0+?$", "").replaceAll("[.]$", "");
                    couponVerificationListResponse.setCouponAmount(discount + "折");
                } else {
                    String value = couponValue.setScale(2, BigDecimal.ROUND_HALF_UP).toString().replaceAll("0+?$", "").replaceAll("[.]$", "");
                    couponVerificationListResponse.setCouponAmount(value + "元");
                }
            }

            couponVerificationListResponse.setUpdateUsername(entity.getVerifyUsername());
            couponVerificationListResponse.setCrowdScopedIdStr(UserTypeEnum.of(entity.getUserType()).getUserTypeStr());

            String functionCodes = midPlatformLoginHelper.getFunctionInfoByUt(bo.getUt()).getFunctionCodes();
            boolean codeTuoMing = !functionCodes.contains(FunctionCodeConstant.COUPONVERIFICATION_CODE_MASK);
            //第三方券码是否脱敏
            boolean thirdCodeTuoming = !functionCodes.contains(FunctionCodeConstant.COUPONVERIFICATION_THIRD_CODE_MASK);
            LambdaFieldNameSelector<CouponThemeCache> selector = new LambdaFieldNameSelector<>();
            selector.select(CouponThemeCache::getCouponType);
            CouponThemeCache couponThemeCache = couponThemeCacheService.getById(entity.getCouponThemeId(), selector);
            String couponCode = AESPromotionUtil.decrypt(entity.getCouponCode());
            if(!Objects.equals(CouponTypeEnum.COUPON_TYPE_THIRD.getType(), couponThemeCache.getCouponType()) && (codeTuoMing || thirdCodeTuoming)){
                //券码脱敏查看
                couponVerificationListResponse.setCouponCode(tuoMing(couponCode));
            } else {
                couponVerificationListResponse.setCouponCode(couponCode);
            }

            //手机号脱敏查看
            if(!functionCodes.contains(FunctionCodeConstant.COUPONVERIFICATION_PHONE_CODE_MASK)){
                couponVerificationListResponse.setCellNo(maskCouponcell(entity.getBindTel()));
            } else {
                couponVerificationListResponse.setCellNo(entity.getBindTel());
            }

            listObj.add(couponVerificationListResponse);
        });

        return pageResponse;
    }

    private String maskCouponcell(String cellNo) {
        if(StringUtils.isBlank(cellNo)){
            return "";
        }
        if(cellNo.length() >= 6) {
            char[] chars = cellNo.toCharArray();
            StringBuffer sb = new StringBuffer();
            int begin = cellNo.length()/2-2;
            int end =  cellNo.length()/2+1;
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                if (i >= begin && i <= end) {
                    c = '*';
                }
                sb.append(c);
            }
            return sb.toString();
        }
        return cellNo;
    }

    //前四位后四位明文
    private String tuoMing(String s){
        if(StringUtils.isBlank(s)){
            return "";
        }
        if(s.length()>8){
            char[] chars = s.toCharArray();
            StringBuffer sb = new StringBuffer();
            int begin = 4;
            int end = s.length()-5;
            for (int i=0;i<chars.length;i++){
                char c = chars[i];
                if(i>=begin&&i<=end){
                    c = '*';
                }
                sb.append(c);
            }
            return sb.toString();
        }
        return s;
    }

    /**
     * 查询核销列表 导出excel
     * @param bo
     * @return
     */
    @Override
    public PageResponse<CouponVerificationExportResponse> export(CouponVerificationListBo bo) {
        PageResponse<CouponVerificationExportResponse> pageResponse = new PageResponse<>();
        LambdaQueryWrapper<CouponVerificationEntity> queryWrapper = prepareQueryWrapperForSelective(bo);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<CouponVerificationEntity> pg = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>();
        pg.setCurrent(bo.getCurrentPage());
        pg.setSize(bo.getItemsPerPage());
        // 不查总数
        pg.setSearchCount(true);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<CouponVerificationEntity> resultPage = baseMapper.selectPage(pg, queryWrapper);
        List<CouponVerificationEntity> couponVerificationEntityList = resultPage.getRecords();

        List<CouponVerificationExportResponse> listObj = new ArrayList<>();
        pageResponse.setListObj(listObj);
        pageResponse.setTotal(Long.valueOf(resultPage.getTotal()).intValue());

        //查询结果为空直接返回
        if (CollectionUtils.isEmpty(couponVerificationEntityList)) {
            return pageResponse;
        }

        couponVerificationEntityList.stream().forEach(entity->{
            CouponVerificationExportResponse couponVerificationExportResponse = new CouponVerificationExportResponse();
            //拷贝基本数据项
            BeanUtils.copyProperties(entity, couponVerificationExportResponse);

            couponVerificationExportResponse.setId(entity.getCouponId());
            couponVerificationExportResponse.setCreateTime(entity.getCouponCreateTime());
            couponVerificationExportResponse.setCrowdScopeId(entity.getUserType());

            // 设置优惠券价值返回
            BigDecimal couponValue = entity.getCouponValue();
            if (Objects.nonNull(couponValue)) {
                if (CouponDiscountType.DISCOUNT.getType().equals(entity.getCouponDiscountType())) {
                    String discount = couponValue.divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).toString().replaceAll("0+?$", "").replaceAll("[.]$", "");
                    couponVerificationExportResponse.setCouponAmount(discount + "折");
                } else {
                    String value = couponValue.setScale(2, BigDecimal.ROUND_HALF_UP).toString().replaceAll("0+?$", "").replaceAll("[.]$", "");
                    couponVerificationExportResponse.setCouponAmount(value + "元");
                }
            }

            couponVerificationExportResponse.setUpdateUsername(entity.getVerifyUsername());
            couponVerificationExportResponse.setCrowdScopedIdStr(UserTypeEnum.of(entity.getUserType()).getUserTypeStr());

            String functionCodes = midPlatformLoginHelper.getFunctionInfoByUt(bo.getUt()).getFunctionCodes();
            boolean codeTuoMing = !functionCodes.contains(FunctionCodeConstant.COUPONVERIFICATION_CODE_MASK);
            //第三方券码是否脱敏
            boolean thirdCodeTuoming = !functionCodes.contains(FunctionCodeConstant.COUPONVERIFICATION_THIRD_CODE_MASK);
            LambdaFieldNameSelector<CouponThemeCache> selector = new LambdaFieldNameSelector<>(CouponThemeCache.class);
            selector.select(CouponThemeCache::getCouponType);
            CouponThemeCache couponThemeCache = couponThemeCacheService.getById(entity.getCouponThemeId(), selector);
            String couponCode = AESPromotionUtil.decrypt(entity.getCouponCode());
            if(!Objects.equals(CouponTypeEnum.COUPON_TYPE_THIRD.getType(), couponThemeCache.getCouponType()) && (codeTuoMing || thirdCodeTuoming)){
                //券码脱敏查看
                couponVerificationExportResponse.setCouponCode(tuoMing(couponCode));
            } else {
                couponVerificationExportResponse.setCouponCode(couponCode);
            }

            //手机号脱敏查看
            if(!functionCodes.contains(FunctionCodeConstant.COUPONVERIFICATION_PHONE_CODE_MASK)){
                couponVerificationExportResponse.setCellNo(maskCouponcell(entity.getBindTel()));
            } else {
                couponVerificationExportResponse.setCellNo(entity.getBindTel());
            }

            listObj.add(couponVerificationExportResponse);
        });

        return pageResponse;
    }

    /**
     * 异步将核销列表导出excel
     * @param bo
     */
    @Override
    public Long exportCouponVerificationListAsync(CouponVerificationListBo bo){
        PageResponse<CouponVerificationExportResponse> exportResponse = this.export(bo);
        if (exportResponse.getTotal() == 0) {
            throw new BusinessException(CouponVerificationErrorCode.COUPON_VERIFICATION_NOT_EXIST);
        }

        Long generateBatchId = saveCouponGenerateBatch(exportResponse.getTotal());

        //开始异步导出
        String traceId = MDC.get(InfraConstant.TRACE_ID);
        couponBatchExecutor.execute(() -> {
            MDC.put(InfraConstant.TRACE_ID, traceId);
            try {
                //导出并上次excel文件
                CouponVerifationExportResultDto exportResult = exportCouponVerificationAndUpload(exportResponse.getListObj());
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

    @Override
    public int conditionalCount(CouponVerificationListBo bo) {
        LambdaQueryWrapper<CouponVerificationEntity> queryWrapper = prepareQueryWrapperForSelective(bo);
        return baseMapper.selectCount(queryWrapper);
    }

    @Override
    public CouponVerificationDetailResponse getDetailById(Long id) {
        CouponVerificationEntity couponVerificationEntity = baseMapper.selectById(id);
        CouponVerificationDetailResponse response = new CouponVerificationDetailResponse();
        BeanUtil.copyProperties(couponVerificationEntity, response);

        String functionCodes = midPlatformLoginHelper.getFunctionInfoByUt(AuthorityHolder.AuthorityThreadLocal.get().getUserInfo().getUt()).getFunctionCodes();
        boolean codeTuoMing = !functionCodes.contains(FunctionCodeConstant.COUPONVERIFICATION_CODE_MASK);
        //第三方券码是否脱敏
        boolean thirdCodeTuoming = !functionCodes.contains(FunctionCodeConstant.COUPONVERIFICATION_THIRD_CODE_MASK);
        LambdaFieldNameSelector<CouponThemeCache> selector = new LambdaFieldNameSelector<>();
        selector.select(CouponThemeCache::getCouponType);
        CouponThemeCache couponThemeCache = couponThemeCacheService.getById(couponVerificationEntity.getCouponThemeId(), selector);
        String couponCode = AESPromotionUtil.decrypt(couponVerificationEntity.getCouponCode());
        if(!Objects.equals(CouponTypeEnum.COUPON_TYPE_THIRD.getType(), couponThemeCache.getCouponType()) && (codeTuoMing || thirdCodeTuoming)){
            //券码脱敏查看
            response.setCouponCode(tuoMing(couponCode));
        } else {
            response.setCouponCode(couponCode);
        }

        //手机号脱敏查看
        if(!functionCodes.contains(FunctionCodeConstant.COUPONVERIFICATION_PHONE_CODE_MASK)){
            response.setBindTel(maskCouponcell(couponVerificationEntity.getBindTel()));
        } else {
            response.setBindTel(couponVerificationEntity.getBindTel());
        }
        return response;
    }

    @Override
    public List<CouponVerificationStatisticDo> listVerificationCount(List<Long> couponThemeIds) {
        return baseMapper.listVerificationCount(couponThemeIds);
    }

    private LambdaQueryWrapper<CouponVerificationEntity> prepareQueryWrapperForSelective(CouponVerificationListBo bo) {
        LambdaQueryWrapper<CouponVerificationEntity> queryWrapper = Wrappers.lambdaQuery(CouponVerificationEntity.class);
        queryWrapper.eq(CouponVerificationEntity::getStatus, CouponStatusEnum.STATUS_USED.getStatus());

        if (StringUtils.isNotBlank(bo.getCouponCode())) {
            queryWrapper.eq(CouponVerificationEntity::getCouponCode, AESPromotionUtil.encrypt(bo.getCouponCode()));
        }
        if (StringUtils.isNotBlank(bo.getCouponActivityName())) {
            queryWrapper.like(CouponVerificationEntity::getThemeTitle, bo.getCouponActivityName() + "%");
        }
        if (Objects.nonNull(bo.getCrowdScopeId())) {
            queryWrapper.eq(CouponVerificationEntity::getUserType, bo.getCrowdScopeId());
        }
        if (StringUtils.isNotBlank(bo.getCellNo())) {
            queryWrapper.eq(CouponVerificationEntity::getBindTel, bo.getCellNo());
        }
        if (Objects.nonNull(bo.getCouponCreateStartTime())) {
            queryWrapper.gt(CouponVerificationEntity::getCreateTime, bo.getCouponCreateStartTime());
        }
        if (Objects.nonNull(bo.getCouponCreateEndTime())) {
            queryWrapper.lt(CouponVerificationEntity::getCreateTime, bo.getCouponCreateEndTime());
        }
        if (Objects.nonNull(bo.getCouponEffectiveStartTime())) {
            queryWrapper.ge(CouponVerificationEntity::getEndTime, bo.getCouponEffectiveStartTime());
        }
        if (Objects.nonNull(bo.getCouponEffectiveEndTime())) {
            queryWrapper.le(CouponVerificationEntity::getEndTime, bo.getCouponEffectiveEndTime());
        }
        if (Objects.nonNull(bo.getUsedStartTime())) {
            queryWrapper.ge(CouponVerificationEntity::getUsedTime, bo.getUsedStartTime());
        }
        if (Objects.nonNull(bo.getUsedEndTime())) {
            queryWrapper.le(CouponVerificationEntity::getUsedTime, bo.getUsedEndTime());
        }
        if (Objects.nonNull(bo.getCouponActivityId())) {
            queryWrapper.eq(CouponVerificationEntity::getCouponThemeId, bo.getCouponActivityId());
        }
        if (StringUtils.isNotBlank(bo.getUpdateUsername())) {
            queryWrapper.eq(CouponVerificationEntity::getVerifyUsername, bo.getUpdateUsername());
        }
        if (StringUtils.isNotBlank(bo.getOrderCode())) {
            queryWrapper.eq(CouponVerificationEntity::getSubscribeCode, bo.getOrderCode());
        }
        if (Objects.nonNull(bo.getUsedStoreId())) {
            queryWrapper.eq(CouponVerificationEntity::getUsedStoreId, bo.getUsedStoreId());
        }
        if (StringUtils.isNoneBlank(bo.getUsedStoreName())) {
            queryWrapper.eq(CouponVerificationEntity::getVerifyUsername, bo.getUsedStoreName());
        }

        if (Objects.nonNull(bo.getUsedStoreId())) {
            queryWrapper.eq(CouponVerificationEntity::getUsedStoreId, bo.getUsedStoreId());
        }else if (!OrgLevelEnum.PLATFORM.getOrgLevelCode().equals(bo.getUserOrgLevelCode())) {
            // 数据权限
            StoreInfo authStoreInfo = midPlatformLoginHelper.getStoreInfoByUserId(String.valueOf(bo.getUserId()));
            List<AuthStoreDTO> authStoreList = authStoreInfo.getAuthStoreList();
            List<Long> authStoreIds = Collections.singletonList(-1L);
            if (CollectionUtils.isNotEmpty(authStoreList)) {
                authStoreIds = authStoreList.stream().map(AuthStoreDTO::getStoreId).distinct().collect(Collectors.toList());
            }

            queryWrapper.in(CouponVerificationEntity::getUsedStoreId, authStoreIds);
        }

        return queryWrapper;
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

    private void updateCouponGenerateBatch(Long generateBatchId, CouponVerifationExportResultDto exportResult) {
        CouponGenerateBatchEntity generateBatchEntity = new CouponGenerateBatchEntity();
        generateBatchEntity.setId(generateBatchId);
        generateBatchEntity.setSendCouponStatus(exportResult.getStatusEnum().getStatus());
        generateBatchEntity.setUploadFile(exportResult.getUploadFile());
        generateBatchEntity.setFailReason(exportResult.getErrorReason());
        generateBatchEntity.setFinishTime(new Date());
        couponGenerateBatchService.updateById(generateBatchEntity);
    }

    private CouponVerifationExportResultDto exportCouponVerificationAndUpload(List<CouponVerificationExportResponse> couponVerificationExportResponse) {
        ByteArrayOutputStream outputStream = null;
        try {
            //导出到文件
            outputStream = new ByteArrayOutputStream();
            List<Object> rowDatas = couponVerificationExportResponse.stream().map(m -> (Object) m).collect(Collectors.toList());
            excelExporter.export(outputStream, CouponVerificationExportResponse.class, rowDatas);
            //上传文件 todo 文件服务器是否需要改为房车宝的 可以参考运营位

            return CouponVerifationExportResultDto.builder().statusEnum(AsyncStatusEnum.FINISHED).uploadFile("").build();
        } catch (ImportException ex) {
            log.error("导出优惠券活动生成excel文件异常", ex);
            return CouponVerifationExportResultDto.builder().statusEnum(AsyncStatusEnum.FAIL).errorReason(ex.getMessage()).build();
        } catch (Exception ex) {
            log.error("导出优惠券活动异常", ex);
            return CouponVerifationExportResultDto.builder().statusEnum(AsyncStatusEnum.FAIL).errorReason("导出异常").build();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ex) {
                    log.error("导出优惠券活动IO关闭异常", ex);
                }
            }
        }
    }
}
