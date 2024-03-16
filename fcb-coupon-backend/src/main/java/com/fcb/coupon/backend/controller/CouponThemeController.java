package com.fcb.coupon.backend.controller;

import com.fcb.coupon.backend.business.couponCreate.CouponBatchCreateBusiness;
import com.fcb.coupon.backend.business.couponSend.CouponSendBusiness;
import com.fcb.coupon.backend.business.couponTheme.CouponThemeBusiness;
import com.fcb.coupon.backend.business.couponTheme.CouponThemeStatisticBusiness;
import com.fcb.coupon.backend.exception.CouponThemeErrorCode;
import com.fcb.coupon.backend.infra.inteceptor.IgnoreAuthorityPath;
import com.fcb.coupon.backend.model.bo.*;
import com.fcb.coupon.backend.model.dto.CouponBatchSendImportBrokerDto;
import com.fcb.coupon.backend.model.dto.CouponBatchSendImportCustomerDto;
import com.fcb.coupon.backend.model.dto.CouponBatchSendImportMemberDto;
import com.fcb.coupon.backend.model.param.request.*;
import com.fcb.coupon.backend.model.param.response.*;
import com.fcb.coupon.backend.service.CouponThemeService;
import com.fcb.coupon.backend.uitls.BackendResponseUtil;
import com.fcb.coupon.common.constant.CouponConstant;
import com.fcb.coupon.common.constant.RedisLockKeyConstant;
import com.fcb.coupon.common.dto.AuthorityHolder;
import com.fcb.coupon.common.dto.RedisLockResult;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.dto.UserInfo;
import com.fcb.coupon.common.enums.CouponSourceTypeEnum;
import com.fcb.coupon.common.enums.UserTypeEnum;
import com.fcb.coupon.common.excel.constant.ImportConstant;
import com.fcb.coupon.common.excel.excetption.ImportException;
import com.fcb.coupon.common.excel.exporter.Exporter;
import com.fcb.coupon.common.excel.importer.Importer;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.exception.CommonErrorCode;
import com.fcb.coupon.common.util.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

/**
 * @author mashiqiong
 * @date 2021-06-16 10:12
 */
@RestController
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@Api(tags = {"优惠券活动接口"})
@Slf4j
public class CouponThemeController {

    private final Exporter csvExporter;
    private final CouponThemeService couponThemeService;
    private final CouponBatchCreateBusiness couponBatchCreateBusiness;
    private final CouponSendBusiness couponSendBusiness;
    private final CouponThemeBusiness couponThemeBusiness;
    private final CouponThemeStatisticBusiness couponThemeStatisticBusiness;

    @Resource(name = "failFastCSVImporter")
    private Importer failFastCSVImporter;

    @PostMapping(value = "/couponActivityRead/list.do")
    @ApiOperation(value = "优惠券活动列表-优惠券管理", httpMethod = "POST")
    public ResponseDto<PageResponse> list(@RequestBody @Validated PageRequest<CouponThemeListRequest> in) {
        CouponThemeListBo bo = in.getObj().convert();
        bo.setItemsPerPage(in.getItemsPerPage());
        bo.setCurrentPage(in.getCurrentPage());
        PageResponse<CouponThemeListResponse> pageResponse = couponThemeService.listCouponTheme(bo);
        return BackendResponseUtil.successObj(pageResponse);
    }

    @PostMapping(value = "/couponActivityRead/queryCouponActivityPG.do")
    @ApiOperation(value = "优惠券活动列表-运营位", httpMethod = "POST")
    public ResponseDto<PageResponse> listByMarketPosition(@RequestBody @Validated PageRequest<CouponThemePositionMarketingRequest> in) {
        CouponThemePositionMarketingBo bo = in.getObj().convert();
        bo.setItemsPerPage(in.getItemsPerPage());
        bo.setCurrentPage(in.getCurrentPage());
        PageResponse<CouponThemePositionMarketingResponse> pageResponse = couponThemeService.listPositionMarketingCouponTheme(bo);
        return BackendResponseUtil.successObj(pageResponse);
    }

    @PostMapping(value = "/couponActivityRead/listInitiativeMarketingCouponTheme.do")
    @ApiOperation(value = "优惠券活动列表-主动营销", httpMethod = "POST")
    public ResponseDto<PageResponse> listByMarketTask(@RequestBody @Validated PageRequest<CouponThemeInitiativeMarketingRequest> in) {
        CouponThemeInitiativeMarketingBo bo = in.getObj().convert();
        bo.setItemsPerPage(in.getItemsPerPage());
        bo.setCurrentPage(in.getCurrentPage());
        PageResponse<CouponThemeInitiativeMarketingResponse> pageResponse = couponThemeService.listInitiativeMarketingCouponTheme(bo);
        return BackendResponseUtil.successObj(pageResponse);
    }

    @PostMapping(value = "/inner-api/couponActivityRead/queryCouponActivityPG.do")
    @ApiOperation(value = "优惠券活动列表-使用未知", httpMethod = "POST")
    public ResponseDto<PageResponse> listPageCouponTheme(@RequestBody @Validated PageRequest<CouponThemeListRequest> in) {
        CouponThemeListBo bo = in.getObj().convert();
        bo.setItemsPerPage(in.getItemsPerPage());
        bo.setCurrentPage(in.getCurrentPage());
        PageResponse<CouponThemeListResponse> pageResponse = couponThemeService.listCouponTheme(bo);
        return BackendResponseUtil.successObj(pageResponse);
    }


    @PostMapping(value = "/couponActivityRead/exportCouponThemeList.do")
    @ApiOperation(value = "优惠券活动列表异步导出", httpMethod = "POST")
    public ResponseDto<CouponThemeExportAsyncResponse> exportThemeListAsync(@RequestBody @Valid CouponThemeExportRequest request) {
        CouponThemeExportBo bo = request.convert();
        Long generateBatchId = couponThemeService.exportCouponThemeListAsync(bo);
        CouponThemeExportAsyncResponse response = new CouponThemeExportAsyncResponse();
        response.setGenerateBatchId(generateBatchId);
        return BackendResponseUtil.success("操作成功", response);
    }

    @PostMapping(value = "/couponActivityRead/queryCouponDetailById.action")
    @ApiOperation(value = "优惠券活动详情", httpMethod = "POST")
    public ResponseDto<CouponThemeDetailResponse> getById(@RequestBody @Validated CouponThemeDetailRequest in) {
        CouponThemeDetailResponse couponThemeDetailResponse = couponThemeService.getCouponThemeDetailById(in.convert());
        return BackendResponseUtil.successObj(couponThemeDetailResponse);
    }


    @IgnoreAuthorityPath
    @GetMapping(value = "/couponActivityRead/downloadCSVTemplateCommon")
    @ApiOperation(value = "导出-发券模板", httpMethod = "POST")
    public void exportSendCouponTemplate(@RequestParam(value = "crowdId") @ApiParam(value = "发券对象") Integer crowdId, HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.setHeader("Content-Disposition", "attachment; filename=send-coupon-template.csv");
        httpServletResponse.setContentType(ImportConstant.FILE_CONTENT_TYPE_UTF8);
        if (UserTypeEnum.C.getUserType().equals(crowdId)) {
            csvExporter.export(httpServletResponse.getOutputStream(), CouponBatchSendImportCustomerDto.class, Collections.EMPTY_LIST);
        } else if (UserTypeEnum.B.getUserType().equals(crowdId)) {
            csvExporter.export(httpServletResponse.getOutputStream(), CouponBatchSendImportMemberDto.class, Collections.EMPTY_LIST);
        } else {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_SEND_TYPE_NOT_SUPPORT);
        }
    }

    @IgnoreAuthorityPath
    @GetMapping(value = "/couponActivityRead/downloadImportCouponCSVTemplate.action")
    @ApiOperation(value = "导出-第三方券导入模板", httpMethod = "POST")
    public void exportImportCouponTemplate(HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.setHeader("Content-Disposition", "attachment; filename=import-coupon-template.csv");
        httpServletResponse.setContentType(ImportConstant.FILE_CONTENT_TYPE);
        csvExporter.export(httpServletResponse.getOutputStream(), CouponThirdImportRequest.class, Collections.EMPTY_LIST, Charset.forName("GBK"));
    }

    @PostMapping(value = "/couponActivityRead/queryCouponActivityStatistics")
    @ApiOperation(value = "查询券活动的统计信息", httpMethod = "POST")
    public ResponseDto<List<CouponThemeStatisticsResponse>> listStatisticsByThemeIds(@RequestBody @Validated CouponThemeStatisticsRequest in) {
        List<CouponThemeStatisticsResponse> themeStatisticsResponses = couponThemeStatisticBusiness.listByThemeIds(in.getCouponThemeIdList());
        return BackendResponseUtil.successObj(themeStatisticsResponses);
    }


    @PostMapping(value = "/couponActivityWrite/saveCouponActivity.do")
    @ApiOperation(value = "保存", httpMethod = "POST")
    public ResponseDto<Long> save(@RequestBody @Valid CouponThemeSaveRequest in) {
        CouponThemeSaveBo bo = in.convert();
        Long couponThemeId = couponThemeBusiness.save(bo);
        return BackendResponseUtil.successObj(couponThemeId);
    }

    @PostMapping(value = "/couponActivityWrite/updateCouponActivity.do")
    @ApiOperation(value = "编辑", httpMethod = "POST")
    public ResponseDto<Void> edit(@RequestBody @Valid CouponThemeUpdateRequest in) {
        CouponThemeUpdateBo bo = in.convert();
        String lockName = RedisLockKeyConstant.OPERATE_COUPON_THEME + bo.getCouponThemeId();
        RedisLockResult<Boolean> redisLockResult = RedisUtil.executeTryLock(lockName, CouponConstant.NO, () -> couponThemeBusiness.edit(bo));
        if (redisLockResult.isFailure()) {
            throw new BusinessException(CommonErrorCode.OPERATE_FREQUENTLY);
        }
        return BackendResponseUtil.success();
    }

    @PostMapping(value = "/couponActivityWrite/couponActivitySubmitAudit.do")
    @ApiOperation(value = "提交审核", httpMethod = "POST")
    public ResponseDto<Void> submitAudit(@RequestBody @Valid CouponThemeSubmitAuditRequest in) {
        Long couponThemeId = in.getCouponThemeId();
        String lockName = RedisLockKeyConstant.OPERATE_COUPON_THEME + couponThemeId;
        RedisLockResult<Boolean> redisLockResult = RedisUtil.executeTryLock(lockName, CouponConstant.NO, () -> couponThemeService.submitAudit(couponThemeId));
        if (redisLockResult.isFailure()) {
            throw new BusinessException(CommonErrorCode.OPERATE_FREQUENTLY);
        }
        return BackendResponseUtil.success();
    }

    @PostMapping(value = "/couponActivityWrite/couponActivityAuditPass.do")
    @ApiOperation(value = "审核通过", httpMethod = "POST")
    public ResponseDto<Void> auditPass(@RequestBody @Valid CouponThemeAuditPassRequest in) {
        Long couponThemeId = in.getCouponThemeId();
        String lockName = RedisLockKeyConstant.OPERATE_COUPON_THEME + couponThemeId;
        RedisLockResult<Boolean> redisLockResult = RedisUtil.executeTryLock(lockName, CouponConstant.NO, () -> couponThemeBusiness.auditPass(couponThemeId, in.getRemark()));
        if (redisLockResult.isFailure()) {
            throw new BusinessException(CommonErrorCode.OPERATE_FREQUENTLY);
        }
        return BackendResponseUtil.success();
    }

    @PostMapping(value = "/couponActivityWrite/couponActivityAuditNotPass.do")
    @ApiOperation(value = "审核不通过", httpMethod = "POST")
    public ResponseDto<Void> auditNotPass(@RequestBody @Valid CouponThemeAuditNotPassRequest in) {
        Long couponThemeId = in.getCouponThemeId();
        String lockName = RedisLockKeyConstant.OPERATE_COUPON_THEME + couponThemeId;
        RedisLockResult<Boolean> redisLockResult = RedisUtil.executeTryLock(lockName, CouponConstant.NO, () -> couponThemeService.auditNotPass(couponThemeId, in.getRemark()));
        if (redisLockResult.isFailure()) {
            throw new BusinessException(CommonErrorCode.OPERATE_FREQUENTLY);
        }
        return BackendResponseUtil.success();
    }

    @PostMapping(value = "/couponActivityWrite/copyCouponTheme.do")
    @ApiOperation(value = "复制", httpMethod = "POST")
    public ResponseDto<Void> copy(@RequestBody @NotNull Long couponThemeId) {
        String lockName = RedisLockKeyConstant.OPERATE_COUPON_THEME + couponThemeId;
        RedisLockResult<Boolean> redisLockResult = RedisUtil.executeTryLock(lockName, CouponConstant.NO, () -> couponThemeBusiness.copy(couponThemeId));
        if (redisLockResult.isFailure()) {
            throw new BusinessException(CommonErrorCode.OPERATE_FREQUENTLY);
        }
        return BackendResponseUtil.success();
    }

    @PostMapping(value = "/couponActivityWrite/updateThemeAfterCheck.action")
    @ApiOperation(value = "更新规则", httpMethod = "POST")
    public ResponseDto<Void> updateAfterCheck(@RequestBody @Valid CouponThemeUpdateAfterCheckRequest in) {
        CouponThemeUpdateAfterCheckBo bo = in.convert();

        String lockName = RedisLockKeyConstant.OPERATE_COUPON_THEME + bo.getCouponThemeId();
        RedisLockResult<Boolean> redisLockResult = RedisUtil.executeTryLock(lockName, CouponConstant.NO, () -> couponThemeBusiness.updateAfterCheck(bo));
        if (redisLockResult.isFailure()) {
            throw new BusinessException(CommonErrorCode.OPERATE_FREQUENTLY);
        }
        return BackendResponseUtil.success();
    }

    @PostMapping(value = "/couponActivityWrite/closeCouponTheme.do")
    @ApiOperation(value = "关闭", httpMethod = "POST")
    public ResponseDto<Void> close(@RequestBody Long couponThemeId) {
        String lockName = RedisLockKeyConstant.OPERATE_COUPON_THEME + couponThemeId;
        RedisLockResult<Boolean> redisLockResult = RedisUtil.executeTryLock(lockName, CouponConstant.NO, () -> couponThemeBusiness.close(couponThemeId));
        if (redisLockResult.isFailure()) {
            throw new BusinessException(CommonErrorCode.OPERATE_FREQUENTLY);
        }
        return BackendResponseUtil.success();
    }

    @PostMapping(value = "/couponActivityWrite/deleteCouponTheme.do")
    @ApiOperation(value = "删除", httpMethod = "POST")
    public ResponseDto<Void> delete(@RequestBody @NotNull Long couponThemeId) {
        String lockName = RedisLockKeyConstant.OPERATE_COUPON_THEME + couponThemeId;
        RedisLockResult<Boolean> redisLockResult = RedisUtil.executeTryLock(lockName, CouponConstant.NO, () -> couponThemeService.delete(couponThemeId));
        if (redisLockResult.isFailure()) {
            throw new BusinessException(CommonErrorCode.OPERATE_FREQUENTLY);
        }
        return BackendResponseUtil.success();
    }

    @PostMapping(value = "/couponActivityWrite/generateCoupons.do")
    @ApiOperation(value = "生券-线下预制券", httpMethod = "POST")
    public ResponseDto<Boolean> generateCoupons(@RequestBody GenerateCouponRequest in) {
        GenerateCouponBo bo = in.convert();
        if (bo.getSource() == null) {
            bo.setSource(CouponSourceTypeEnum.COUPON_SOURCE_OFFLINE.getSource());
        }
        boolean result = couponBatchCreateBusiness.batchGenerateCoupon(bo);

        return BackendResponseUtil.successObj(result);
    }

    @PostMapping(value = "/couponActivityWrite/provideCoupons.do")
    @ApiOperation(value = "批量发券", httpMethod = "POST")
    public ResponseDto<Void> issuingCoupons(
            @RequestParam(value = "fileData", required = false) MultipartFile file,
            @RequestParam(value = "id") @ApiParam(value = "券活动Id") Long id,
            @RequestParam(value = "sendCouponUserType", defaultValue = "0") @ApiIgnore Integer sendCouponUserType,
            @RequestParam(value = "phoneNumbers", required = false) @ApiIgnore String phoneNumbers,
            @RequestParam(value = "userIds", required = false) @ApiParam(value = "多个用户 , 号拼接起来") String sendUserIds
    ) throws Exception {
        CouponBatchSendBo bo = new CouponBatchSendBo();
        //登录信息
        UserInfo userInfo = AuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        bo.setUserId(userInfo.getUserId());
        bo.setUsername(userInfo.getUsername());
        //入参
        bo.setThemeId(id);
        bo.setSendUserType(sendCouponUserType);
        bo.setSource(CouponSourceTypeEnum.COUPON_SOURCE_NAMED_USER.getSource());
        bo.setPhones(phoneNumbers);
        bo.setUserIds(sendUserIds);
        //解析excel文件
        if (file != null && !file.isEmpty()) {
            try {
                if (UserTypeEnum.C.getUserType().equals(sendCouponUserType)) {
                    List<CouponBatchSendImportCustomerDto> customerDtos = failFastCSVImporter.parseSimple(file.getInputStream(), CouponBatchSendImportCustomerDto.class);
                    bo.setCustomers(customerDtos);
                } else if (UserTypeEnum.B.getUserType().equals(sendCouponUserType)) {
                    List<CouponBatchSendImportMemberDto> merberDtos = failFastCSVImporter.parseSimple(file.getInputStream(), CouponBatchSendImportMemberDto.class);
                    bo.setMembers(merberDtos);
                } else if (UserTypeEnum.SAAS.getUserType().equals(sendCouponUserType)) {
                    List<CouponBatchSendImportBrokerDto> brokerDtos = failFastCSVImporter.parseSimple(file.getInputStream(), CouponBatchSendImportBrokerDto.class);
                    bo.setBrokers(brokerDtos);
                }
            } catch (ImportException ex) {
                throw new BusinessException(CommonErrorCode.PARAMS_ERROR.getCode(), ex.getMessage());
            }
        }
        if (CollectionUtils.isEmpty(bo.getSendUserList())) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_SEND_USER_EMPTY);
        }

        couponSendBusiness.manualBatchSend(bo);
        return BackendResponseUtil.success();
    }


    @PostMapping(value = "/couponActivityWrite/importCoupons.do")
    @ApiOperation(value = "导入第三方券", httpMethod = "POST")
    public ResponseDto importCoupons(@RequestParam(value = "fileData4importCoupon") @ApiParam(value = "CSV文件", name = "fileData4importCoupon") MultipartFile file,
                                     @RequestParam(value = "id") @ApiParam(value = "券活动Id") Long id) throws IOException {
        //文件校验
        if (file != null && file.getSize() == 0L) {
            return BackendResponseUtil.fail(CouponThemeErrorCode.COUPON_IMPORT_FILE_NOT_EXISTS);
        }
        //文件后缀验证
        if (!file.getOriginalFilename().toLowerCase().endsWith(ImportConstant.CSV_SUFFIX)) {
            return BackendResponseUtil.fail(CouponThemeErrorCode.COUPON_IMPORT_FILE_ERROR);
        }
        //解析数据
        List<CouponThirdImportRequest> thirdImportRequests = null;
        try {
            thirdImportRequests = failFastCSVImporter.parseSimple(file.getInputStream(), CouponThirdImportRequest.class, Charset.forName("GBK"));
        } catch (ImportException ex) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_IMPORT_PRASE_ERROR.getCode(), ex.getMessage());
        }
        if (CollectionUtils.isEmpty(thirdImportRequests)) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_IMPORT_NOT_DATA_ERROR);
        }

        CouponImportBo bo = new CouponImportBo();
        bo.setThemeId(id);
        bo.setThirdImportRequestList(thirdImportRequests);
        UserInfo userInfo = AuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        bo.setUserId(userInfo.getUserId());
        bo.setUsername(userInfo.getUsername());
        couponBatchCreateBusiness.batchImportThird(bo);
        return BackendResponseUtil.success();
    }

}
