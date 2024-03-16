package com.fcb.coupon.backend.controller;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.fcb.coupon.backend.business.verification.CouponVerificationBusiness;
import com.fcb.coupon.backend.exception.CouponVerificationErrorCode;
import com.fcb.coupon.backend.model.bo.*;
import com.fcb.coupon.backend.model.entity.AsyncTaskEntity;
import com.fcb.coupon.backend.model.param.request.*;
import com.fcb.coupon.backend.model.param.response.AsyncTaskListResponse;
import com.fcb.coupon.backend.model.param.response.CouponVerificationDetailResponse;
import com.fcb.coupon.backend.model.param.response.CouponVerificationListResponse;
import com.fcb.coupon.backend.model.param.response.PageResponse;
import com.fcb.coupon.backend.service.AsyncTaskService;
import com.fcb.coupon.backend.service.CouponVerificationService;
import com.fcb.coupon.backend.uitls.BackendResponseUtil;
import com.fcb.coupon.common.constant.InfraConstant;
import com.fcb.coupon.common.constant.RedisLockKeyConstant;
import com.fcb.coupon.common.dto.AuthorityHolder;
import com.fcb.coupon.common.dto.RedisLockResult;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.dto.UserInfo;
import com.fcb.coupon.common.enums.YesNoEnum;
import com.fcb.coupon.common.excel.bean.RowParseResult;
import com.fcb.coupon.common.excel.bean.SheetParseResult;
import com.fcb.coupon.common.excel.constant.ImportConstant;
import com.fcb.coupon.common.excel.exporter.Exporter;
import com.fcb.coupon.common.excel.importer.Importer;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.util.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * @author HanBin_Yang
 * @since 2021/6/23 8:55
 */
@RestController
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@Api(tags = {"优惠券核销相关"})
@RequestMapping("/couponVerification")
@Slf4j
public class CouponVerificationController {
    private final CouponVerificationService couponVerificationService;
    private final AsyncTaskService asyncTaskService;
    private final Importer excelImporter;
    private final Exporter excelExporter;
    private final ThreadPoolTaskExecutor couponVerificationExecutor;
    private final CouponVerificationBusiness couponVerificationBusiness;

    @PostMapping(value = "/list.do")
    @ApiOperation(value = "查询核销列表", httpMethod = "POST")
    public ResponseDto<PageResponse<CouponVerificationListResponse>> list(@RequestBody PageRequest<CouponVerificationListRequest> in) {
        CouponVerificationListBo bo = in.getObj().convert();
        bo.setItemsPerPage(in.getItemsPerPage());
        bo.setCurrentPage(in.getCurrentPage());
        PageResponse<CouponVerificationListResponse> pageResponse = couponVerificationService.list(bo);
        return BackendResponseUtil.successObj(pageResponse);
    }

    @RequestMapping(value = "/count.do", consumes = "application/json", method = RequestMethod.POST)
    @ResponseBody
    public ResponseDto<PageResponse<Object>> count(@RequestBody PageRequest<CouponVerificationListRequest> in) {
        CouponVerificationListBo bo = in.getObj().convert();
        bo.setItemsPerPage(in.getItemsPerPage());
        bo.setCurrentPage(in.getCurrentPage());
        int total = couponVerificationService.conditionalCount(bo);
        PageResponse<Object> result = new PageResponse<>();
        result.setTotal(total);
        return BackendResponseUtil.successObj(result);
    }

    @RequestMapping(value = "/queryCouponVerificationDetailById.do", consumes = "application/json", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "查询单个已核销优惠券详情")
    public ResponseDto<CouponVerificationDetailResponse> queryCouponVerificationDetailById(@RequestBody CouponVerificationDetailRequest in) {
        CouponVerificationDetailResponse out = couponVerificationService.getDetailById(in.getId());
        return BackendResponseUtil.successObj(out);
    }

    @PostMapping("/verification.do")
    @ApiOperation(value = "单个优惠券输入核销", httpMethod = "POST")
    public ResponseDto<Void> singleVerify(@RequestBody CouponSingleVerifyRequest in) {
        SingleVerifyBo bo = in.convert();
        // redis锁
        String keyName = String.format("%s%s", RedisLockKeyConstant.SINGLE_COUPON_VERIFICATION, bo.getCouponCode());
        RedisLockResult<Void> redisLockResult = RedisUtil.executeTryLock(keyName, YesNoEnum.NO.getValue(), () -> couponVerificationBusiness.singleVerify(bo));
        //获取不到锁
        if(redisLockResult.isFailure()) {
            throw new BusinessException(CouponVerificationErrorCode.REPEAT_VERIFY);
        }

        return BackendResponseUtil.success();
    }


    @ApiOperation(value = "批量导入核销", httpMethod = "POST")
    @RequestMapping(value="/import.do", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseDto<Void> importVerifyBatch(@RequestParam(value="fileData", required=false) MultipartFile file) throws IOException {
        SheetParseResult parse = excelImporter.parse(file.getInputStream(), CouponVerifyImportRequest.class);
        Map<Integer, RowParseResult> rowParseResultMap = parse.getRowParseResultMap();
        if (MapUtils.isEmpty(rowParseResultMap)) {
            throw new BusinessException(CouponVerificationErrorCode.VERIFY_TEMPLATE_DATA_EMPTY);
        }

        UserInfo userInfo = AuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        AsyncTaskEntity asyncTaskEntity = new AsyncTaskEntity();
        Long asyncTaskId = RedisUtil.generateId();
        asyncTaskEntity.setId(asyncTaskId);
        asyncTaskEntity.setTaskType("importCouponVerification");
        asyncTaskEntity.setAsyncStatus(0);
        asyncTaskEntity.setCreateUserid(userInfo.getUserId());
        asyncTaskEntity.setCreateUsername(userInfo.getUsername());
        asyncTaskService.getBaseMapper().insert(asyncTaskEntity);

        BatchVerifyBo bo = new BatchVerifyBo();
        bo.setUserId(userInfo.getUserId());
        bo.setUsername(userInfo.getUsername());
        bo.setAsyncTaskId(asyncTaskId);
        bo.setImportDataMap(rowParseResultMap);
        String traceId = MDC.get(InfraConstant.TRACE_ID);
        couponVerificationExecutor.execute(() -> {
            try {
                MDC.put(InfraConstant.TRACE_ID, traceId);
                couponVerificationBusiness.batchVerify(bo);
            } finally {
                MDC.remove(InfraConstant.TRACE_ID);
            }
        });

        return BackendResponseUtil.success();
    }


    @RequestMapping(value = "/exportVerificationTemplate.do", method = {RequestMethod.POST, RequestMethod.GET})
    @ApiOperation(value = "下载核销模板", httpMethod = "POST")
    public void templateForVerifyBatch(HttpServletResponse resp) throws IOException {
        resp.setHeader("Content-Disposition", "attachment; filename=verification-import-template-"+ DateUtil.format(new Date(), DatePattern.PURE_DATETIME_PATTERN) + ".xls");
        resp.setContentType(ImportConstant.FILE_CONTENT_TYPE);
        excelExporter.export(resp.getOutputStream(), CouponVerifyImportRequest.class, Collections.emptyList());
    }

    @ApiOperation(value = "导出核销列表", httpMethod = "POST")
    @RequestMapping(value = "/export.do")
    public Object exportCoupons(@RequestBody CouponVerificationExportRequest in) {
        CouponVerificationListBo bo = in.convert();
        Long id = couponVerificationService.exportCouponVerificationListAsync(bo);
        return BackendResponseUtil.successObj(id);
    }

    @ApiOperation(value="查询导入核销任务列表", httpMethod = "POST")
    @RequestMapping(value = "/listAsyncTask.do", consumes = "application/json", method = RequestMethod.POST)
    @ResponseBody
    public ResponseDto<PageResponse> listAsyncTask(@RequestBody PageRequest<AsyncTaskListRequest> in) {
        AsyncTaskListBo bo = in.getObj().convert();
        bo.setCurrentPage(in.getCurrentPage());
        bo.setItemsPerPage(in.getItemsPerPage());

        PageResponse<AsyncTaskListResponse> pageResponse = asyncTaskService.list(bo);
        return BackendResponseUtil.successObj(pageResponse);
    }
}
