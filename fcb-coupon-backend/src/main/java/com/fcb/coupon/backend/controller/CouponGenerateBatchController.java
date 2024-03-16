package com.fcb.coupon.backend.controller;

import cn.hutool.core.date.DateUtil;
import com.fcb.coupon.backend.infra.inteceptor.IgnoreAuthorityPath;
import com.fcb.coupon.backend.model.entity.CouponGenerateBatchEntity;
import com.fcb.coupon.backend.model.entity.CouponThemeEntity;
import com.fcb.coupon.backend.model.param.request.PageRequest;
import com.fcb.coupon.backend.model.param.response.CouponGenerateBatchExportResponse;
import com.fcb.coupon.backend.model.param.response.CouponGenerateBatchResponse;
import com.fcb.coupon.backend.model.param.response.PageResponse;
import com.fcb.coupon.backend.service.CouponGenerateBatchService;
import com.fcb.coupon.backend.service.CouponThemeService;
import com.fcb.coupon.backend.uitls.BackendResponseUtil;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.enums.AsyncStatusEnum;
import com.fcb.coupon.common.excel.exporter.Exporter;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.exception.CommonErrorCode;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年09月03日 09:55:00
 */
@RestController
@Api(tags = "优惠券批量操作日志")
public class CouponGenerateBatchController {

    @Autowired
    private CouponGenerateBatchService couponGenerateBatchService;
    @Autowired
    private Exporter csvExporter;
    @Autowired
    private CouponThemeService couponThemeService;

    @ApiOperation(value = "查询异步任务列表")
    @PostMapping(value = "/couponActivityRead/querySendAndImportCouponTaskList.action")
    @ResponseBody
    @IgnoreAuthorityPath
    public ResponseDto<PageResponse<CouponGenerateBatchResponse>> list(@RequestBody @Valid PageRequest request) {
        PageResponse<CouponGenerateBatchResponse> pageResponse = couponGenerateBatchService.listPage(request);
        return BackendResponseUtil.successObj(pageResponse);
    }

    @GetMapping(value = "/couponActivityRead/exportImportCouponTask.do")
    @ResponseBody
    @ApiOperation(value = "导出第三方券导入结果")
    public void exportImportCouponTask(@RequestParam("couponGenerateBatchId") String couponGenerateBatchIdParm, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/vnd.ms-excel");
        resp.setHeader("content-disposition", "attachment;filename=import-result.xls");

        Long couponGenerateBatchId = Long.parseLong(couponGenerateBatchIdParm);
        CouponGenerateBatchEntity entity = couponGenerateBatchService.getById(couponGenerateBatchId);
        if (entity == null) {
            throw new BusinessException(CommonErrorCode.PARAMS_ERROR);
        }

        CouponThemeEntity couponThemeEntity = couponThemeService.getById(entity.getThemeId());

        CouponGenerateBatchExportResponse response = new CouponGenerateBatchExportResponse();
        response.setIndex(1);
        response.setTypeName("导入券码");
        response.setStatusName(AsyncStatusEnum.of(entity.getSendCouponStatus()).getDesc());
        response.setTotalCount(entity.getTotalRecord());
        response.setSuccessCount(entity.getSuccessRecord());
        response.setErrorCount(entity.getFailRecord());
        response.setErrorReason(entity.getFailReason());
        response.setCreateTime(entity.getCreateTime());
        response.setCreateUsername(entity.getCreateUsername());
        response.setGenerateId(entity.getId().toString());
        if (couponThemeEntity != null) {
            response.setThemeTitle(couponThemeEntity.getThemeTitle());
            response.setStartEnd(DateUtil.format(couponThemeEntity.getStartTime(), "yyyy-MM-dd HH:mm:ss") + " - " + couponThemeEntity.getStartTime());
        }

        csvExporter.export(resp.getOutputStream(), CouponGenerateBatchExportResponse.class, Lists.newArrayList(response));
    }

}
