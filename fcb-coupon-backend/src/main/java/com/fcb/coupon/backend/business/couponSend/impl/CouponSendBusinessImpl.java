package com.fcb.coupon.backend.business.couponSend.impl;

import cn.hutool.core.date.DateUtil;
import com.fcb.coupon.backend.business.couponSend.CouponSendBusiness;
import com.fcb.coupon.backend.business.couponSend.CouponSendHandler;
import com.fcb.coupon.backend.exception.CouponThemeErrorCode;
import com.fcb.coupon.backend.model.bo.CouponBatchSendBo;
import com.fcb.coupon.backend.model.dto.CouponSendContext;
import com.fcb.coupon.backend.model.dto.CouponSendResult;
import com.fcb.coupon.backend.model.dto.FileUploadResultDto;
import com.fcb.coupon.backend.model.entity.CouponGenerateBatchEntity;
import com.fcb.coupon.backend.model.entity.CouponThemeEntity;
import com.fcb.coupon.backend.model.param.response.CouponSendErrorResponse;
import com.fcb.coupon.backend.remote.client.CommonFileClient;
import com.fcb.coupon.backend.service.CouponGenerateBatchService;
import com.fcb.coupon.backend.service.CouponThemeService;
import com.fcb.coupon.common.constant.CouponConstant;
import com.fcb.coupon.common.constant.InfraConstant;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.enums.AsyncStatusEnum;
import com.fcb.coupon.common.enums.CouponBatchTypeEnum;
import com.fcb.coupon.common.enums.CouponGiveRuleEnum;
import com.fcb.coupon.common.enums.CouponSourceTypeEnum;
import com.fcb.coupon.common.excel.constant.ImportConstant;
import com.fcb.coupon.common.excel.excetption.ImportException;
import com.fcb.coupon.common.excel.exporter.ExcelExporter;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.file.CommonMultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 121272100
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
public class CouponSendBusinessImpl implements CouponSendBusiness {

    private final CouponThemeService couponThemeService;
    private final CouponGenerateBatchService couponGenerateBatchService;
    private final ThreadPoolTaskExecutor couponBatchExecutor;
    private final List<CouponSendHandler> handlers;
    private final ExcelExporter excelExporter;
    private final CommonFileClient commonFileClient;


    /*
    指定用户发放
     */
    @Override
    public void manualBatchSend(CouponBatchSendBo bo) {
        if (CollectionUtils.isEmpty(bo.getSendUserList())) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_SEND_USER_EMPTY);
        }
        CouponThemeEntity couponTheme = couponThemeService.getById(bo.getThemeId());
        if (couponTheme == null) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_NOT_EXIST);
        }

        bo.setSource(CouponSourceTypeEnum.COUPON_SOURCE_NAMED_USER.getSource());
        CouponSendHandler handler = getHandler(bo.getSendUserType());
        //验证券活动信息
        handler.validate(bo, couponTheme);
        //如果手工发券数量比较少，走同步
        if (bo.getSendUserList().size() < CouponSendHandler.BATCH_SIZE) {
            batchSendSync(bo, couponTheme, handler);
            return;
        }
        //开始异步批量发券
        batchSendAsync(bo, couponTheme, handler);
    }

    /*
    主动营销批量发券
     */
    @Override
    public CouponSendResult marketingBatchSend(CouponBatchSendBo bo) {
        CouponThemeEntity couponTheme = couponThemeService.getById(bo.getThemeId());
        if (couponTheme == null) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_NOT_EXIST);
        }
        //判断是主动营销券
        if (CouponGiveRuleEnum.COUPON_GIVE_RULE_MARKTEING_VOUCHER.getType().equals(couponTheme.getCouponGiveRule())) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_TYPE_ERROR);
        }

        bo.setSource(CouponSourceTypeEnum.COUPON_SOURCE_MARKTEING_VOUCHER.getSource());
        CouponSendHandler handler = getHandler(bo.getSendUserType());
        //验证券活动信息
        handler.validate(bo, couponTheme);
        //开始批量发券
        return doBatchSend(bo, couponTheme, handler);
    }


    /*
     * 活动发券
     */
    @Override
    public CouponSendResult activityBatchSend(CouponBatchSendBo bo) {
        CouponThemeEntity couponTheme = couponThemeService.getById(bo.getThemeId());
        if (couponTheme == null) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_NOT_EXIST);
        }
        //判断是活动规则券
        if (CouponGiveRuleEnum.COUPON_GIVE_RULE_ACTIVY_RULE.getType().equals(couponTheme.getCouponGiveRule())) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_TYPE_ERROR);
        }

        bo.setSource(CouponSourceTypeEnum.COUPON_SOURCE_ACTIVY_RULE.getSource());
        CouponSendHandler handler = getHandler(bo.getSendUserType());
        //验证券活动信息
        handler.validate(bo, couponTheme);
        //开始批量发券
        return doBatchSend(bo, couponTheme, handler);
    }

    private CouponSendResult doBatchSend(CouponBatchSendBo couponBatchSendBo, CouponThemeEntity couponTheme, CouponSendHandler handler) {
        return handler.batchSend(couponBatchSendBo, couponTheme);
    }

    private void batchSendSync(CouponBatchSendBo couponBatchSendBo, CouponThemeEntity couponTheme, CouponSendHandler handler) {
        //生成导入记录
        Long generateBatchId = saveCouponGenerateBatch(couponBatchSendBo);
        //开始异步任务
        //发券
        CouponSendResult couponSendResult = doBatchSend(couponBatchSendBo, couponTheme, handler);
        //生产发券结果文件
        FileUploadResultDto exportResult = exportSendResultAndUpload(generateBatchId, couponBatchSendBo, couponSendResult, couponTheme);
        //更新发券任务
        updateCouponGenerateBatch(generateBatchId, couponSendResult, exportResult);

        if (couponSendResult.getSuccessCount() == 0) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_SEND_ERROR.getCode(), "发券失败，请查看发券结果");
        }
        if (couponSendResult.getErrorCount() > 0) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_SEND_ERROR.getCode(), "发券部分用户失败，请查看发券结果");
        }
    }

    private void batchSendAsync(CouponBatchSendBo couponBatchSendBo, CouponThemeEntity couponTheme, CouponSendHandler handler) {
        //生成导入记录
        Long generateBatchId = saveCouponGenerateBatch(couponBatchSendBo);
        //开始异步任务
        try {
            //开始异步导入
            String traceId = MDC.get(InfraConstant.TRACE_ID);
            couponBatchExecutor.execute(() -> {
                MDC.put(InfraConstant.TRACE_ID, traceId);
                try {
                    //发券
                    CouponSendResult couponSendResult = doBatchSend(couponBatchSendBo, couponTheme, handler);
                    //生产发券结果文件
                    FileUploadResultDto exportResult = exportSendResultAndUpload(generateBatchId, couponBatchSendBo, couponSendResult, couponTheme);
                    //更新发券任务
                    updateCouponGenerateBatch(generateBatchId, couponSendResult, exportResult);
                } finally {
                    MDC.remove(InfraConstant.TRACE_ID);
                }
            });
        } catch (Exception ex) {
            log.error("异步批量发送优惠券异常", ex);
        }
    }

    private CouponSendHandler getHandler(Integer sendUserType) {
        for (CouponSendHandler handler : handlers) {
            if (handler.supports(sendUserType)) {
                return handler;
            }
        }
        throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_SEND_TYPE_NOT_SUPPORT);
    }

    private Long saveCouponGenerateBatch(CouponBatchSendBo bo) {
        CouponGenerateBatchEntity couponGenerateBatchEntity = new CouponGenerateBatchEntity();
        couponGenerateBatchEntity.setType(CouponBatchTypeEnum.LOG_TYPE_SEND_COUPON.getType());
        couponGenerateBatchEntity.setThemeId(bo.getThemeId());
        int totalSendCount = bo.getSendUserList().stream().mapToInt(m -> m.getCount()).sum();
        couponGenerateBatchEntity.setTotalRecord(totalSendCount);
        couponGenerateBatchEntity.setSendCouponStatus(AsyncStatusEnum.SENDING.getStatus());
        couponGenerateBatchEntity.setGenerateNums(couponGenerateBatchEntity.getTotalRecord());
        couponGenerateBatchEntity.setCreateUserid(bo.getUserId());
        couponGenerateBatchEntity.setCreateUsername(bo.getUsername());
        couponGenerateBatchService.save(couponGenerateBatchEntity);
        return couponGenerateBatchEntity.getId();
    }

    private void updateCouponGenerateBatch(Long generateBatchId, CouponSendResult couponSendResult, FileUploadResultDto exportResult) {
        CouponGenerateBatchEntity generateBatchEntity = new CouponGenerateBatchEntity();
        generateBatchEntity.setId(generateBatchId);
        generateBatchEntity.setSendCouponStatus(couponSendResult.getStatus().getStatus());
        generateBatchEntity.setTotalRecord(couponSendResult.getTotalCount());
        generateBatchEntity.setSuccessRecord(couponSendResult.getSuccessCount());
        generateBatchEntity.setFailRecord(couponSendResult.getErrorCount());
        generateBatchEntity.setFinishTime(new Date());
        generateBatchEntity.setUploadFile(exportResult.getUploadFile());
        generateBatchEntity.setFailReason(exportResult.getErrorReason());
        couponGenerateBatchService.updateById(generateBatchEntity);
    }

    public FileUploadResultDto exportSendResultAndUpload(Long generateBatchId, CouponBatchSendBo couponBatchSendBo, CouponSendResult couponSendResult, CouponThemeEntity couponTheme) {
        //对电话号码分组
        Map<String, List<CouponSendContext>> sendCouponContextGroup = couponSendResult.getSendContexts().stream().collect(Collectors.groupingBy(CouponSendContext::getBindTel));
        //构造导出结果
        List<CouponSendErrorResponse> errorResponses = new ArrayList<>();
        int index = 1;
        for (Map.Entry<String, List<CouponSendContext>> couponSendContextEntry : sendCouponContextGroup.entrySet()) {
            CouponSendErrorResponse errorResponse = new CouponSendErrorResponse();
            errorResponse.setIndex(index);
            errorResponse.setCouponName(couponTheme.getThemeTitle());
            errorResponse.setOprtName("发放");
            errorResponse.setStartEndTime(DateUtil.format(couponTheme.getStartTime(), "yyyy-MM-dd HH:mm:ss") + " - " + DateUtil.format(couponTheme.getEndTime(), "yyyy-MM-dd HH:mm:ss"));
            errorResponse.setPhone(couponSendContextEntry.getKey());

            List<CouponSendContext> failContexts = couponSendContextEntry.getValue().stream().filter(m -> m.getIsFailure()).collect(Collectors.toList());
            if (failContexts.size() > 0) {
                errorResponse.setStatusName("失败");
                String remark = String.join(",", failContexts.stream().map(m -> m.getFailureReason()).collect(Collectors.toList()));
                errorResponse.setRemark(remark);
            } else {
                errorResponse.setStatusName("成功");
            }

            errorResponse.setCount(couponSendContextEntry.getValue().size());
            errorResponse.setCreateTime(new Date());
            errorResponse.setCreateUserName(couponBatchSendBo.getUsername());
            errorResponse.setGenerateBatchId(generateBatchId.toString());
            errorResponses.add(errorResponse);
            index++;
        }

        //上传文件
        return doExportSendResultAndUpload(errorResponses);
    }

    private FileUploadResultDto doExportSendResultAndUpload(List<CouponSendErrorResponse> errorResponses) {
        ByteArrayOutputStream outputStream = null;
        ByteArrayInputStream inputStream = null;
        try {
            //导出到文件
            outputStream = new ByteArrayOutputStream();
            excelExporter.export(outputStream, CouponSendErrorResponse.class, errorResponses);
            //上传文件
            byte[] barray = outputStream.toByteArray();
            inputStream = new ByteArrayInputStream(barray);
            String fileName = UUID.randomUUID().toString().replace("-", "") + ImportConstant.EXCEL_2003_SUFFIX;
            MultipartFile multipartFile = new CommonMultipartFile("file", fileName, ImportConstant.EXCEL_2007_CONTENT_TYPE, inputStream);
            ResponseDto<String> responseDto = commonFileClient.uploadFileByFixedFileName(multipartFile, "/excel/couponSend");
            if (!CouponConstant.SUCCESS_CODE.equals(responseDto.getCode())) {
                log.error("发券结果文件上传失败");
                return FileUploadResultDto.builder().statusEnum(AsyncStatusEnum.FAIL).errorReason("发券结果文件上传失败").build();
            }
            return FileUploadResultDto.builder().statusEnum(AsyncStatusEnum.FINISHED).uploadFile(responseDto.getData()).build();
        } catch (ImportException ex) {
            log.error("生成发券结果excel文件异常", ex);
            return FileUploadResultDto.builder().statusEnum(AsyncStatusEnum.FAIL).errorReason(ex.getMessage()).build();
        } catch (Exception ex) {
            log.error("生成发券结果文件上传异常", ex);
            return FileUploadResultDto.builder().statusEnum(AsyncStatusEnum.FAIL).errorReason("发券结果文件上传异常").build();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    log.error("生成发券结果IO关闭异常", ex);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ex) {
                    log.error("生成发券结果IO关闭异常", ex);
                }
            }
        }
    }


}