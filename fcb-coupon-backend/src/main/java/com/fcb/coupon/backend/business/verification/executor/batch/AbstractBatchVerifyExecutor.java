package com.fcb.coupon.backend.business.verification.executor.batch;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.fcb.coupon.backend.business.verification.context.BatchVerifyContext;
import com.fcb.coupon.backend.business.verification.context.VerifyServiceContext;
import com.fcb.coupon.backend.business.verification.executor.AbstractVerifyExecutor;
import com.fcb.coupon.backend.elasticsearch.CouponEsDoc;
import com.fcb.coupon.backend.model.cache.CouponThemeCache;
import com.fcb.coupon.backend.model.dto.CouponImportVerifyResultDto;
import com.fcb.coupon.backend.model.dto.StoreInfoOutDto;
import com.fcb.coupon.backend.model.dto.VerifyUserInfoDto;
import com.fcb.coupon.backend.model.entity.AsyncTaskEntity;
import com.fcb.coupon.backend.model.entity.CouponEntity;
import com.fcb.coupon.backend.model.param.request.CouponVerifyImportRequest;
import com.fcb.coupon.common.constant.CouponConstant;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.enums.AsyncStatusEnum;
import com.fcb.coupon.common.enums.AsyncTaskStatusEnum;
import com.fcb.coupon.common.enums.LogOprType;
import com.fcb.coupon.common.excel.bean.RowParseResult;
import com.fcb.coupon.common.excel.constant.ImportConstant;
import com.fcb.coupon.common.exception.ResponseErrorCode;
import com.fcb.coupon.common.file.CommonMultipartFile;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 批量核销执行器
 * @author YangHanBin
 * @date 2021-09-09 15:10
 */
public abstract class AbstractBatchVerifyExecutor extends AbstractVerifyExecutor {
    public AbstractBatchVerifyExecutor(BatchVerifyContext verifyContext, VerifyServiceContext serviceContext) {
        super(serviceContext, verifyContext.getVerifyUserId(), verifyContext.getVerifyUsername());
        this.verifyContext = verifyContext;
    }
    // 异步任务主键id
    @Getter
    private final BatchVerifyContext verifyContext;
    // 原始数据长度
    @Setter
    @Getter
    private int originalDataSize;
    // k-couponCode v-List<CouponEsDoc> , 由BatchVerifyExecutor生成
    @Setter
    private Map<String, List<CouponEsDoc>> couponCodeEsDocsMap;
    public Map<String, List<CouponEsDoc>> getCouponCodeEsDocsMap() {
        if (couponCodeEsDocsMap == null) {
            throw new UnsupportedOperationException("请先执行BatchVerifyExecutor");
        }
        return couponCodeEsDocsMap;
    }
    // k-dbCouponCode v-CouponEntity, 由BatchVerifyExecutor生成
    @Setter
    private Map<String, CouponEntity> dbCouponCodeMap;
    public Map<String, CouponEntity> getDbCouponCodeMap() {
        if (dbCouponCodeMap == null) {
            throw new UnsupportedOperationException("请先执行BatchVerifyExecutor");
        }
        return dbCouponCodeMap;
    }
    // k-couponThemeId v-CouponThemeCache,由CouponThemeBatchVerifyExecutor生成
    @Setter
    private Map<Long, CouponThemeCache> couponThemeIdCacheMap;
    public Map<Long, CouponThemeCache> getCouponThemeIdCacheMap() {
        if (couponThemeIdCacheMap == null) {
            throw new UnsupportedOperationException("请先执行CouponBatchVerifyExecutor");
        }
        return couponThemeIdCacheMap;
    }
    // k-userType v-Map(k-手机号 v-VerifyUserInfoDto), 由UserInfoBatchVerifyExecutor生成
    @Setter
    private Map<Integer, Map<String, VerifyUserInfoDto>> userTypeInfoMap;
    public Map<Integer, Map<String, VerifyUserInfoDto>> getUserTypeInfoMap() {
        if (userTypeInfoMap == null) {
            throw new UnsupportedOperationException("请先执行UserInfoBatchVerifyExecutor");
        }
        return userTypeInfoMap;
    }
    // k-buildCode v-StoreInfoOutDto, 由StoreInfoBatchVerifyExecutor生成
    @Setter
    private Map<String, StoreInfoOutDto> buildCodeStoreInfoMap;
    public Map<String, StoreInfoOutDto> getBuildCodeStoreInfoMap() {
        if (buildCodeStoreInfoMap == null) {
            throw new UnsupportedOperationException("请先执行StoreInfoBatchVerifyExecutor");
        }
        return buildCodeStoreInfoMap;
    }

    protected void before() {

    }

    @Override
    public void execute() {
        before();
        doExecute();
        after();
    }

    protected abstract void doExecute();

    protected void after() {
    }

    protected Iterator<RowParseResult> getImportDataIterator() {
        return this.getVerifyContext().getRowParseResults().iterator();
    }

    protected CouponImportVerifyResultDto prepareImportVerifyErrorCodeResultBean(String status, CouponVerifyImportRequest bean, ResponseErrorCode errorCode) {
        String message = null;
        if (Objects.nonNull(errorCode)) {
            message = errorCode.getMessage();
        }
        return prepareImportVerifyResultBean(status, bean, message);
    }

    protected CouponImportVerifyResultDto prepareImportVerifyResultBean(String status, CouponVerifyImportRequest bean, String errorMessage) {
        CouponImportVerifyResultDto resultDto = new CouponImportVerifyResultDto();
        resultDto.setCouponCode(bean.getCouponCode());
        resultDto.setOperationType(LogOprType.VERIFICATION.getDesc());
        resultDto.setSubscribeCode(bean.getSubscribeCode());
        resultDto.setStatus(status);
        resultDto.setTaskId(getVerifyContext().getAsyncTaskId());
        resultDto.setOperateTime(DateUtil.format(new Date(), DatePattern.NORM_DATETIME_PATTERN));
        resultDto.setOprUserName(getVerifyContext().getVerifyUsername());
        resultDto.setRemark(errorMessage);
        return resultDto;
    }

    protected void exportVerifyResult(Long asyncTaskId, TreeMap<Integer, CouponImportVerifyResultDto> verifyResultMap) {
        List<CouponImportVerifyResultDto> importResultList = new ArrayList<>(verifyResultMap.values());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        getServiceContext().getExcelExporter().export(byteArrayOutputStream, CouponImportVerifyResultDto.class, importResultList);
        InputStream fsInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        MultipartFile multipartFile = null;
        // 上传文件系统
        try {
            String fileName = String.format("verify-result-%s%s",DateUtil.format(new Date(), DatePattern.PURE_DATETIME_PATTERN), ImportConstant.EXCEL_2003_SUFFIX);
            multipartFile = new CommonMultipartFile("file", fileName, ImportConstant.EXCEL_2007_CONTENT_TYPE, fsInputStream);
        } catch (IOException e) {
            log.error("导入批量核销结果解析出错 asyncTaskId={}", asyncTaskId);
            throw new RuntimeException("导入批量核销结果解析出错", e);
        }

        String url;
        try {
            ResponseDto<String> responseDto = getServiceContext().getCommonFileClient().uploadFileByFixedFileName(multipartFile, "/excel/export/backendVerifyResult");
            url = responseDto.getData();
        } catch (Exception e) {
            log.error("recordImportVerifyResult上传文件系统异常 asyncTaskId={}", asyncTaskId, e);
            throw e;
        }

        Map<String, Long> statusCountMap = verifyResultMap.values().parallelStream().collect(Collectors.groupingBy(CouponImportVerifyResultDto::getStatus, Collectors.counting()));

        AsyncTaskEntity asyncTaskEntity = new AsyncTaskEntity();
        asyncTaskEntity.setId(asyncTaskId);

        int failRecode = 0;
        int successRecord = 0;
        Integer status = null;
        if (statusCountMap.containsKey(CouponConstant.FAIL_MESSAGE)) {
            failRecode = statusCountMap.get(CouponConstant.FAIL_MESSAGE).intValue();
            status = AsyncTaskStatusEnum.FAIL.getStatus();
        } else {
            status = AsyncTaskStatusEnum.SUCCESS.getStatus();
        }
        if (statusCountMap.containsKey(CouponConstant.SUCCESS_MESSAGE)) {
            successRecord = statusCountMap.get(CouponConstant.SUCCESS_MESSAGE).intValue();
        }
        asyncTaskEntity.setAsyncStatus(status);
        asyncTaskEntity.setSuccessRecord(successRecord);
        asyncTaskEntity.setFailRecord(failRecode);
        asyncTaskEntity.setRecords(importResultList.size());
        asyncTaskEntity.setDownPath(url);
        asyncTaskEntity.setCreateFileTime(new Date());

        getServiceContext().getAsyncTaskService().updateById(asyncTaskEntity);
    }

    protected void executeFail() {
        CouponImportVerifyResultDto importVerifyResultDto = new CouponImportVerifyResultDto();
        importVerifyResultDto.setRowNum(1);
        importVerifyResultDto.setOperationType(LogOprType.VERIFICATION.getDesc());
        importVerifyResultDto.setOperateTime(DateUtil.format(new Date(), DatePattern.NORM_DATETIME_PATTERN));
        importVerifyResultDto.setStatus(CouponConstant.FAIL_MESSAGE);
        importVerifyResultDto.setTaskId(getVerifyContext().getAsyncTaskId());
        importVerifyResultDto.setOprUserName(getVerifyContext().getVerifyUsername());
        importVerifyResultDto.setRemark("任务执行异常，核销失败，请重试");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        getServiceContext().getExcelExporter().export(byteArrayOutputStream, CouponImportVerifyResultDto.class, Collections.singletonList(importVerifyResultDto));
        InputStream fsInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        MultipartFile multipartFile = null;
        // 上传文件系统
        try {
            String fileName = String.format("verify-result-fail-%s%s",DateUtil.format(new Date(), DatePattern.PURE_DATETIME_PATTERN), ImportConstant.EXCEL_2003_SUFFIX);
            multipartFile = new CommonMultipartFile("file", fileName, ImportConstant.EXCEL_2007_CONTENT_TYPE, fsInputStream);
        } catch (IOException e) {
            throw new RuntimeException("导入批量核销结果解析出错", e);
        }

        ResponseDto<String> responseDto = getServiceContext().getCommonFileClient().uploadFileByFixedFileName(multipartFile, "/excel/export/backendVerifyResult");
        String url = responseDto.getData();
        AsyncTaskEntity asyncTaskEntity = new AsyncTaskEntity();
        asyncTaskEntity.setId(getVerifyContext().getAsyncTaskId());
        asyncTaskEntity.setSuccessRecord(CouponConstant.NO);
        asyncTaskEntity.setDownPath(url);
        asyncTaskEntity.setFailRecord(getOriginalDataSize());
        asyncTaskEntity.setAsyncStatus(AsyncStatusEnum.FAIL.getStatus());
        getServiceContext().getAsyncTaskService().getBaseMapper().updateById(asyncTaskEntity);
    }

    protected boolean hasResult() {
        if (CollectionUtils.isEmpty(getVerifyContext().getRowParseResults())) {
            // 导出核销结果
            exportVerifyResult(getVerifyContext().getAsyncTaskId(), getVerifyContext().getVerifyResultMap());
            return true;
        }
        return false;
    }

}
