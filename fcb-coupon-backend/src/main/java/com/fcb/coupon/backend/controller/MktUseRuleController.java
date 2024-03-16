package com.fcb.coupon.backend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.fcb.coupon.backend.exception.MktUseRuleErrorCode;
import com.fcb.coupon.backend.infra.inteceptor.IgnoreAuthorityPath;
import com.fcb.coupon.backend.model.bo.*;
import com.fcb.coupon.backend.model.param.request.*;
import com.fcb.coupon.backend.model.param.response.MktUseRuleByIdsResponse;
import com.fcb.coupon.backend.model.param.response.MktUseRuleOrgListResponse;
import com.fcb.coupon.backend.model.param.response.MktUseRuleSelectionResponse;
import com.fcb.coupon.backend.model.param.response.PageResponse;
import com.fcb.coupon.backend.service.MktUseRuleService;
import com.fcb.coupon.backend.uitls.BackendResponseUtil;
import com.fcb.coupon.common.constant.CouponConstant;
import com.fcb.coupon.common.constant.RedisLockKeyConstant;
import com.fcb.coupon.common.dto.ResponseDto;
import com.fcb.coupon.common.enums.MktUseRuleInputType;
import com.fcb.coupon.common.enums.MktUseRuleTypeEnum;
import com.fcb.coupon.common.excel.bean.RowParseResult;
import com.fcb.coupon.common.excel.bean.SheetParseResult;
import com.fcb.coupon.common.excel.constant.ImportConstant;
import com.fcb.coupon.common.excel.exporter.Exporter;
import com.fcb.coupon.common.excel.importer.Importer;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.util.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 适用组织
 * @author HanBin_Yang
 * @since 2021/6/21 8:48
 */
@RestController
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@Api(tags = {"券活动--适用组织"})
public class MktUseRuleController {
    private final MktUseRuleService mktUseRuleService;

    @Resource(name = "excelImporter")
    private Importer excelImporter;

    @Resource(name = "excelExporter")
    private Exporter excelExporter;

    @PostMapping(value="/couponSelectionWrite/addMerchantList.do")
    @ApiOperation(value = "添加", httpMethod = "POST")
    public ResponseDto<String> addOrg(@RequestBody MktUseRuleAddOrgRequest in){
        CouponThemeAddOrgBo bo = in.convert();

        Map<String, Integer> map = mktUseRuleService.addOrgBatch(bo);

        StringBuilder sb = new StringBuilder();
        String orgName = MktUseRuleTypeEnum.getOrgLevelNameByType(bo.getRuleType());
        sb.append("成功添加").append(map.get(CouponConstant.SUCCESS_MESSAGE)).append("个").append(orgName).append("。");
        if (map.containsKey(CouponConstant.FAIL_MESSAGE)) {
            sb
            .append(map.get(CouponConstant.FAIL_MESSAGE)).append("个").append(orgName)
            .append("未添加成功，原因为").append(orgName).append("已存在。");
        }

        return BackendResponseUtil.successObj(sb.toString());
    }

    @PostMapping(value = "/couponSelectionWrite/importMerchantList.do")
    @ApiOperation(value = "批量导入添加", httpMethod = "POST")
    public ResponseDto<Object> importBatch(MktUseRuleImportAddOrgRequest in, @RequestParam(value="fileData", required=false) MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return BackendResponseUtil.fail(MktUseRuleErrorCode.IMPORT_TEMPLATE_FORMAT_ERROR);
        }

        Class<?> clazz;
        switch (MktUseRuleInputType.of(in.getMerchantType())) {
            case STORE:
                clazz = AddStoreImportRequest.class;
                break;
            case MERCHANT:
                clazz = AddMerchantImportRequest.class;
                break;
            default:
                throw new BusinessException(MktUseRuleErrorCode.ORG_TYPE_NOT_SUPPORT);
        }

        SheetParseResult sheetParseResult = excelImporter.parse(file.getInputStream(), clazz);
        Map<Integer, RowParseResult> rowParseResultMap = sheetParseResult.getRowParseResultMap();
        List<Object> importDataList = rowParseResultMap.values().stream().map(RowParseResult::getRowBean).collect(Collectors.toList());
        MktUseRuleImportAddOrgBo bo = in.convert();
        List<AddOrgImportBo> dataList = new ArrayList<>();
        importDataList.forEach(bean -> {
            AddOrgImportBo addOrgImportBo = new AddOrgImportBo();
            BeanUtil.copyProperties(bean, addOrgImportBo);
            dataList.add(addOrgImportBo);
        });
        bo.setImportDataList(dataList);

        Map<String, String> returnMap = mktUseRuleService.importOrgBatch(bo);

        String orgLevelName = MktUseRuleTypeEnum.getOrgLevelNameByType(bo.getRuleType());
        // 操作到了数据库返回
        if (returnMap.containsKey(CouponConstant.SUCCESS_MESSAGE)) {
            StringBuilder sb = new StringBuilder();
            sb.append("成功导入").append(returnMap.get(CouponConstant.SUCCESS_MESSAGE)).append("个").append(orgLevelName).append("。");
            if (returnMap.containsKey(CouponConstant.FAIL_MESSAGE)) {
                sb.append(returnMap.get(CouponConstant.FAIL_MESSAGE)).append("个")
                        .append(orgLevelName).append("未导入，原因为").append(orgLevelName).append("已存在");
            }
            // 校验通过，操作到了数据库返回信息
            return BackendResponseUtil.success(CouponConstant.SUCCESS_MESSAGE, sb.toString());
        }
        // 没操作到数据库，校验失败返回
        else {
            return BackendResponseUtil.success(CouponConstant.FAIL_MESSAGE, returnMap);
        }
    }

    @GetMapping(value = "/couponSelectionRead/downloadOrgTemplate.do")
    @ApiOperation(value = "下载导入模板", httpMethod = "GET")
    public void downloadOrgTemplate(@ApiParam(value = "类型，2店铺 1商家 3集团", example = "1")Integer merchantType, HttpServletResponse resp) throws IOException {
        String fileName = "";
        Class<?> clazz;
        String dateStr = DateUtil.format(new Date(), DatePattern.PURE_DATETIME_PATTERN);

        switch (MktUseRuleInputType.of(merchantType)) {
            case STORE:
                clazz = AddStoreImportRequest.class;
                fileName = String.format("import-store-%s.xls", dateStr);
                break;
            case MERCHANT:
                clazz = AddMerchantImportRequest.class;
                fileName = String.format("import-merchant-%s.xls", dateStr);
                break;
            default:
                throw new BusinessException(MktUseRuleErrorCode.ORG_TYPE_NOT_SUPPORT);
        }
        resp.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        resp.setContentType(ImportConstant.FILE_CONTENT_TYPE);
        excelExporter.export(resp.getOutputStream(), clazz, Collections.emptyList());
    }


    @PostMapping(value="/couponSelectionWrite/delMerchantList.do")
    @ApiOperation(value = "删除", httpMethod = "POST")
    public ResponseDto<Boolean> delete(@RequestBody MktUseRuleDeleteOrgRequest in) {
        MktUseRuleDeleteOrgBo bo = in.convert();
        String keyName = RedisLockKeyConstant.OPERATE_MKT_USE_RULE + bo.getCouponThemeId();
        Boolean result = RedisUtil.executeLock(keyName, 60, TimeUnit.SECONDS, () -> mktUseRuleService.removeOrgBatch(bo));
        return BackendResponseUtil.successObj(result);
    }

    @PostMapping(value="/couponSelectionRead/querySelectedMerchantList.do")
    @ApiOperation(value = "查询已选集团、商家、店铺列表", httpMethod = "POST")
    @IgnoreAuthorityPath
    public ResponseDto<PageResponse> querySelectedMerchantList(@RequestBody MktUseRuleOrgListRequest in) {
        MktUseRuleOrgListBo bo = in.convert();
        bo.setCurrentPage(in.getCurrentPage());
        bo.setItemsPerPage(in.getItemsPerPage());

        PageResponse<MktUseRuleOrgListResponse> pageResponse = mktUseRuleService.listMktUseRule(bo);
        return BackendResponseUtil.successObj(pageResponse);
    }

    @PostMapping(value="/couponSelectionRead/querySelectedMerchantIds.do")
    @ApiOperation(value = "查询已选商家Ids", httpMethod = "POST")
    @IgnoreAuthorityPath
    public ResponseDto<PageResponse> getMktUseRuleByIds(@RequestBody MktUseRuleByIdsRequest in) {
        MktUseRuleByIdsBo bo = in.convert();
        bo.setCurrentPage(in.getCurrentPage());
        bo.setItemsPerPage(in.getItemsPerPage());

        PageResponse<MktUseRuleByIdsResponse> pageResponse = mktUseRuleService.getMktUseRuleByIds(bo);
        return BackendResponseUtil.successObj(pageResponse);
    }

    @PostMapping(value="/couponSelectionRead/querySelectedSelectionList.do")
    @ApiOperation(value = "查询已选商家Ids", httpMethod = "POST")
    @IgnoreAuthorityPath
    public ResponseDto<PageResponse> getSelectedSelectionList(@RequestBody MktUseRuleSelectionRequest in) {
        MktUseRuleSelectionBo bo = in.convert();
        bo.setCurrentPage(in.getCurrentPage());
        bo.setItemsPerPage(in.getItemsPerPage());

        PageResponse<MktUseRuleSelectionResponse> pageResponse = mktUseRuleService.getSelectedSelectionList(bo);
        return BackendResponseUtil.successObj(pageResponse);
    }
}
