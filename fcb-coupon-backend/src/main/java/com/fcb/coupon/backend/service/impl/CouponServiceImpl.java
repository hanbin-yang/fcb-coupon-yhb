package com.fcb.coupon.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fcb.coupon.backend.elasticsearch.CouponEsDoc;
import com.fcb.coupon.backend.elasticsearch.helper.QueryBuildHelper;
import com.fcb.coupon.backend.exception.CouponErrorCode;
import com.fcb.coupon.backend.exception.CouponThemeErrorCode;
import com.fcb.coupon.backend.listener.event.MinCouponEvent;
import com.fcb.coupon.backend.mapper.CouponMapper;
import com.fcb.coupon.backend.model.bo.*;
import com.fcb.coupon.backend.model.dto.CouponMergedDto;
import com.fcb.coupon.backend.model.dto.FileUploadResultDto;
import com.fcb.coupon.backend.model.dto.OprLogDo;
import com.fcb.coupon.backend.model.entity.*;
import com.fcb.coupon.backend.model.param.request.CouponQueryRequest;
import com.fcb.coupon.backend.model.param.response.*;
import com.fcb.coupon.backend.remote.client.CommonFileClient;
import com.fcb.coupon.backend.remote.client.OuserWebFeignClient;
import com.fcb.coupon.backend.remote.dto.input.InputDto;
import com.fcb.coupon.backend.remote.dto.input.OrgIdsInput;
import com.fcb.coupon.backend.remote.dto.out.OrgOutDto;
import com.fcb.coupon.backend.remote.dto.out.OutputDto;
import com.fcb.coupon.backend.service.*;
import com.fcb.coupon.common.constant.CouponConstant;
import com.fcb.coupon.common.constant.FunctionCodeConstant;
import com.fcb.coupon.common.dto.*;
import com.fcb.coupon.common.enums.*;
import com.fcb.coupon.common.excel.constant.ImportConstant;
import com.fcb.coupon.common.excel.excetption.ImportException;
import com.fcb.coupon.common.excel.exporter.ExcelExporter;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.file.CommonMultipartFile;
import com.fcb.coupon.common.midplatform.MidPlatformLoginHelper;
import com.fcb.coupon.common.util.AESPromotionUtil;
import com.fcb.coupon.common.util.DateUtils;
import com.fcb.coupon.common.util.DesensitizationUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CouponServiceImpl extends ServiceImpl<CouponMapper, CouponEntity> implements CouponService {

    @Autowired
    private CouponUserService couponUserService;
    @Autowired
    private CouponThemeStatisticService couponThemeStatisticService;
    @Autowired
    private CouponUserStatisticService couponUserStatisticService;
    @Autowired
    private CouponSendLogService couponSendLogService;
    @Autowired
    private CouponThemeCacheService couponThemeCacheService;

    // 分页最大数
    private final static int EXPORT_DATA_PAGE_SIZE = 10000;

    @Resource
    private CouponThemeService couponThemeService;
    @Resource
    private CouponThemeOrgService couponThemeOrgService;
    @Resource
    private OuserWebFeignClient ouserWebFeignClient;
    @Resource
    private MidPlatformLoginHelper midPlatformLoginHelper;
    @Resource
    private CouponGenerateBatchService couponGenerateBatchService;
    @Resource
    private ExcelExporter excelExporter;
    @Resource
    private ThreadPoolTaskExecutor couponBatchExecutor;
    @Resource
    private CommonFileClient commonFileClient;
    @Resource
    private CouponEsDocService couponEsDocService;
    @Resource
    private CouponThirdService couponThirdService;

    @Resource
    private CouponOprLogService couponOprLogService;
    @Resource
    private CouponEsManageService couponEsManageService;
    @Resource
    private ApplicationEventPublisher publisher;


    @Transactional(rollbackFor = Throwable.class)
    @Override
    public void batchSendGenerateCoupon(List<CouponMergedDto> couponMergedDtos) {
        if (CollectionUtils.isEmpty(couponMergedDtos)) {
            return;
        }

        List<CouponEntity> couponEntities = new ArrayList<>(couponMergedDtos.size());
        List<CouponUserEntity> couponUserEntities = new ArrayList<>(couponMergedDtos.size());
        List<CouponSendLogEntity> couponSendLogEntities = new ArrayList<>(couponMergedDtos.size());
        for (CouponMergedDto couponMergedDto : couponMergedDtos) {
            if (couponMergedDto.getCouponEntity() != null) {
                couponEntities.add(couponMergedDto.getCouponEntity());
            }
            if (couponMergedDto.getCouponUserEntity() != null) {
                couponUserEntities.add(couponMergedDto.getCouponUserEntity());
            }
            if (couponMergedDto.getCouponSendLogEntity() != null) {
                couponSendLogEntities.add(couponMergedDto.getCouponSendLogEntity());
            }
        }

        //添加防重表记录
        if (CollectionUtils.isEmpty(couponSendLogEntities)) {
            couponSendLogService.saveBatch(couponSendLogEntities);
        }

        //更新总库存
        Long themeId = couponEntities.get(0).getCouponThemeId();
        int updateRows = couponThemeStatisticService.updateSendedCount(themeId, couponEntities.size());
        if (updateRows == 0) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_CAN_SEND_COUNT_ERROR);
        }
        //保存用户优惠券信息
        couponUserService.batchSave(couponUserEntities);

        //批量保存优惠券明细
        this.baseMapper.insertBatch(couponEntities);

        //更新用户领券明细
        couponUserStatisticService.batchSaveOrUpdate(couponUserEntities);
    }


    @Transactional(rollbackFor = Throwable.class)
    @Override
    public void batchSendThirdPartCoupon(List<CouponMergedDto> couponMergedDtos) {
        if (CollectionUtils.isEmpty(couponMergedDtos)) {
            return;
        }

        List<CouponEntity> couponEntities = new ArrayList<>(couponMergedDtos.size());
        List<CouponUserEntity> couponUserEntities = new ArrayList<>(couponMergedDtos.size());
        List<CouponSendLogEntity> couponSendLogEntities = new ArrayList<>(couponMergedDtos.size());
        for (CouponMergedDto couponMergedDto : couponMergedDtos) {
            if (couponMergedDto.getCouponEntity() != null) {
                couponEntities.add(couponMergedDto.getCouponEntity());
            }
            if (couponMergedDto.getCouponUserEntity() != null) {
                couponUserEntities.add(couponMergedDto.getCouponUserEntity());
            }
            if (couponMergedDto.getCouponSendLogEntity() != null) {
                couponSendLogEntities.add(couponMergedDto.getCouponSendLogEntity());
            }
        }

        //添加防重表记录
        if (CollectionUtils.isEmpty(couponSendLogEntities)) {
            couponSendLogService.saveBatch(couponSendLogEntities);
        }

        //更新总库存
        Long themeId = couponUserEntities.get(0).getCouponThemeId();
        int updateRows = couponThemeStatisticService.updateSendedCount(themeId, couponUserEntities.size());
        if (updateRows == 0) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_CAN_SEND_COUNT_ERROR);
        }

        //批量更新优惠券明细状态
        updateRows = batchUpdateUseStatus(couponEntities);
        if (updateRows < couponUserEntities.size()) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_CAN_SEND_COUNT_ERROR);
        }

        //保存用户优惠券信息
        couponUserService.batchSave(couponUserEntities);

        //更新用户领券明细
        couponUserStatisticService.batchSaveOrUpdate(couponUserEntities);
    }


    /*
     * @description 更新为领券状态
     * @author 唐陆军
     */
    @Override
    public Integer batchUpdateUseStatus(List<CouponEntity> couponEntities) {
        int total = 0;
        for (CouponEntity couponEntity : couponEntities) {
            CouponEntity updateEntity = new CouponEntity();
            updateEntity.setSourceId(couponEntity.getSourceId());
            updateEntity.setStartTime(couponEntity.getStartTime());
            updateEntity.setEndTime(couponEntity.getEndTime());
            updateEntity.setUserType(couponEntity.getUserType());
            updateEntity.setUserId(couponEntity.getUserId());
            updateEntity.setStatus(couponEntity.getStatus());

            LambdaUpdateWrapper where = Wrappers.lambdaUpdate(CouponEntity.class)
                    .eq(CouponEntity::getStatus, CouponStatusEnum.STATUS_ISSUE.getStatus())
                    .eq(CouponEntity::getId, couponEntity.getId());
            int row = this.baseMapper.update(updateEntity, where);
            total = total + row;
        }
        return total;
    }


    @Override
    public PageResponse<CouponViewResponse> queryCouponByPageRequest(CouponQueryBo inputDto) {
        PageResponse<CouponViewResponse> pageResult = new PageResponse<>();

        // es查询券
        Page<CouponEsDoc> esCouponPage = searchCoupons(inputDto);
        List<CouponEsDoc> couponEsDocList = esCouponPage.getContent();

        if (CollectionUtils.isEmpty(couponEsDocList)) {
            return pageResult;
        }
        pageResult.setTotal((int) esCouponPage.getTotalElements());

        // 获取券id集合
        List<Long> couponIdList = couponEsDocList.stream().map(CouponEsDoc::getId).collect(Collectors.toList());

        // 获取第三方券明细
        List<CouponThirdEntity> couponThirdList = couponThirdService.listByIds(couponIdList);
        Map<Long, CouponThirdEntity> thirdCouponMap = couponThirdList.stream().collect(Collectors.toMap(CouponThirdEntity::getCouponId, Function.identity()));

        // 查询分库券详情
        List<CouponEntity> couponEntityList = queryCouponsByIds(couponIdList);

        Map<Long, CouponUserEntity> couponUserEntityMap = getCouponUserEntityMap(couponIdList);

        List<CouponViewResponse> responseList = new ArrayList<>();
        couponEntityList.forEach(entity -> {
            CouponUserEntity couponUserEntity = couponUserEntityMap.get(entity.getId());
            CouponViewResponse target = new CouponViewResponse();
            BeanUtils.copyProperties(entity, target);
            target.setBindTel(couponUserEntity == null ? null : couponUserEntity.getBindTel());
            target.setCrowdScopeId(entity.getUserType());
            target.setBindTime(couponUserEntity == null ? null : couponUserEntity.getCreateTime());

            if (thirdCouponMap.containsKey(entity.getId())) {
                target.setThirdCouponCode(thirdCouponMap.get(entity.getId()).getThirdCouponCode());
            }

            responseList.add(target);
        });

        /**
         * 检查是否查询转赠或转让状态的券,对于转赠的优惠券，需要查询转赠出去的券的状态并转换
         */
        Map<Long, CouponEntity> receiveCouponMap = new HashMap<>();
        if ((Arrays.asList(CouponStatusEnum.STATUS_DONATE.getStatus(), CouponStatusEnum.STATUS_ASSIGN.getStatus()).contains(inputDto.getCouponStatus()))) {
            List<Long> receiveCouponIds = responseList.stream().filter(v -> null != v.getReceiveCouponId()).map(CouponViewResponse::getReceiveCouponId).collect(Collectors.toList());
            // 转赠出去的券集合
            List<CouponEntity> receiveCouponList = queryCouponsByIds(receiveCouponIds);
            receiveCouponMap = receiveCouponList.stream().collect(Collectors.toMap(CouponEntity::getId, Function.identity()));
            ;
        }
        Map<Long, CouponEntity> finalReceiveCouponMap = receiveCouponMap;

        /**
         * 获取优惠券活动所属组织信息
         */
        Set<Long> couponThemeIds = responseList.stream().map(o -> o.getCouponThemeId()).collect(Collectors.toSet());
        List<CouponThemeOrgEntity> couponThemeOrgList = queryCouponThemeOrgBycouponThemeIds(couponThemeIds);
        Set<Long> orgList = couponThemeOrgList.stream().map(o -> o.getOrgId()).collect(Collectors.toSet());
        // 获取组织详情信息
        Map<Long, OrgOutDto> orgInfoMap = queryOrgInfo(orgList);
        // 按券活动分组组织信息
        Map<Long, List<CouponThemeOrgEntity>> couponThemeMap = couponThemeOrgList.stream().collect(Collectors.groupingBy(CouponThemeOrgEntity::getCouponThemeId));

        responseList.forEach(dto -> {
            // 重新构建
            CouponViewResponse newDto = this.buildNewCouponViewResponse(dto, inputDto, couponThemeMap, finalReceiveCouponMap, orgInfoMap);
            BeanUtils.copyProperties(newDto, dto);
        });

        pageResult.setListObj(responseList);
        return pageResult;
    }

    private Map<Long, CouponUserEntity> getCouponUserEntityMap(List<Long> couponIdList) {
        LambdaQueryWrapper<CouponUserEntity> couponUserQueryWrapper = Wrappers.lambdaQuery(CouponUserEntity.class);
        couponUserQueryWrapper.in(CouponUserEntity::getCouponId, couponIdList);
        List<CouponUserEntity> couponUserEntities = couponUserService.getBaseMapper().selectList(couponUserQueryWrapper);
        return couponUserEntities.stream().collect(Collectors.toMap(CouponUserEntity::getCouponId, Function.identity()));
    }


    /**
     * 列表返回对象转换
     *
     * @param source                源对象
     * @param queryBo               请求参数
     * @param couponThemeMap        有查询权限的活动
     * @param finalReceiveCouponMap 已赠送或已转让的券信息
     * @param orgInfoMap            券活动相关组织详情
     * @return
     */
    private CouponViewResponse buildNewCouponViewResponse(CouponViewResponse source, CouponQueryBo queryBo, Map<Long, List<CouponThemeOrgEntity>> couponThemeMap, Map<Long, CouponEntity> finalReceiveCouponMap, Map<Long, OrgOutDto> orgInfoMap) {
        CouponViewResponse dto = new CouponViewResponse();
        BeanUtils.copyProperties(source, dto);

        // 检查是否查询转赠或转让状态的券
        if (CouponStatusEnum.STATUS_DONATE.getStatus().equals(queryBo.getCouponStatus()) || CouponStatusEnum.STATUS_ASSIGN.getStatus().equals(queryBo.getCouponStatus())) {
            if (finalReceiveCouponMap.get(dto.getReceiveCouponId()) != null) {
                dto.setStatus(finalReceiveCouponMap.get(dto.getReceiveCouponId()).getStatus());
            } else {
                dto.setStatus(null);
            }
        }

        if (MapUtils.isNotEmpty(couponThemeMap) && couponThemeMap.containsKey(dto.getCouponThemeId())) {
            List<CouponThemeOrgEntity> clist = couponThemeMap.get(dto.getCouponThemeId());
            Set<Long> orgIdList = clist.stream().map(o -> o.getOrgId()).collect(Collectors.toSet());
            List<String> orgNameList = orgIdList.stream().map(o -> {
                if (orgInfoMap == null) {
                    return org.apache.commons.lang3.StringUtils.EMPTY;
                }
                return orgInfoMap.get(o) == null ? org.apache.commons.lang3.StringUtils.EMPTY : orgInfoMap.get(o).getOrgName();
            }).collect(Collectors.toList());

            // 组织id、组织名称
            dto.setOrgIds(new ArrayList<>(orgIdList));
            dto.setOrgNames(orgNameList);
        }

        // 券来源
        dto.setSourceStr(CouponSourceTypeEnum.getStrBySource(dto.getSource()));
        dto.setCrowdScopeIdStr(UserTypeEnum.getStrByUserType(dto.getCrowdScopeId()));

        // 券号解密
        dto.setCouponCode(AESPromotionUtil.decrypt(dto.getCouponCode()));

        /**
         * 券码脱敏处理
         */
        if (Objects.nonNull(queryBo.getMask()) && queryBo.getMask()) {
            String functionCode = FunctionCodeConstant.PROM_PROMOTION_COUPONLIST_HOUSE_CODE_MASK;
            String functionCodeThird = FunctionCodeConstant.PROM_PROMOTION_COUPONLIST_HOUSE_THIRD_CODE_MASK;
            String functionCodePhone = FunctionCodeConstant.PROM_PROMOTION_COUPONLIST_HOUSE_PHONE_CODE_MASK;
            String functionCodeNumber = FunctionCodeConstant.PROM_PROMOTION_COUPONLIST_HOUSE_PHONE_CODE_MASK;
            if (Objects.equals(CouponStatusEnum.STATUS_DONATE.getStatus(), queryBo.getCouponStatus())) {
                functionCode = FunctionCodeConstant.PROM_PROMOTION_COUPON_DONATELIST_CODE_MASK;
                functionCodeThird = FunctionCodeConstant.PROM_PROMOTION_COUPON_DONATELIST_THIRD_CODE_MASK;
                functionCodePhone = FunctionCodeConstant.PROM_PROMOTION_COUPON_DONATELIST_PHONE_CODE_MASK;
                functionCodeNumber = FunctionCodeConstant.PROM_PROMOTION_COUPON_DONATELIST_NUMBER_CODE_MASK;
            } else if (Objects.equals(CouponStatusEnum.STATUS_ASSIGN.getStatus(), queryBo.getCouponStatus())) {
                functionCode = FunctionCodeConstant.PROM_PROMOTION_COUPON_ASSIGNLIST_CODE_MASK;
                functionCodeThird = FunctionCodeConstant.PROM_PROMOTION_COUPON_ASSIGNLIST_THIRD_CODE_MASK;
                functionCodePhone = FunctionCodeConstant.PROM_PROMOTION_COUPON_ASSIGNLIST_PHONE_CODE_MASK;
                functionCodeNumber = FunctionCodeConstant.PROM_PROMOTION_COUPON_ASSIGNLIST_NUMBER_CODE_MASK;
            }

            String functionCodes = AuthorityHolder.AuthorityThreadLocal.get().getFunctionInfo().getFunctionCodes();
            if (dto.getCouponType().equals(CouponTypeEnum.COUPON_TYPE_THIRD.getType()) && !functionCodes.contains(functionCodeThird)) {
                //券码脱敏查看
                dto.setCouponCode(DesensitizationUtil.maskCouponCode(dto.getCouponCode()));
            } else if (!dto.getCouponType().equals(CouponTypeEnum.COUPON_TYPE_THIRD.getType()) && !functionCodes.contains(functionCode)) {
                //券码脱敏查看
                dto.setCouponCode(DesensitizationUtil.maskCouponCode(dto.getCouponCode()));
            }

            if (!functionCodes.contains(functionCodePhone)) {
                dto.setReceiveUserMobile(DesensitizationUtil.maskCouponcell(dto.getReceiveUserMobile()));
            }

            if (!functionCodes.contains(functionCodeNumber)) {
                dto.setBindTel(DesensitizationUtil.maskCouponcell(dto.getBindTel()));
//                    couponTypeViewVO.setCellNo(DesensitizationUtil.maskCouponcell(couponTypeViewVO.getCellNo()));
            }
        }

        return dto;
    }

    /**
     * 优惠券明细es查询
     *
     * @param queryBo
     * @return
     */
    private Page<CouponEsDoc> searchCoupons(CouponQueryBo queryBo) {
        // 查询参数过滤
        CouponQueryBo inputDto = transQueryParam(queryBo);
        int current = inputDto.getCurrentPage();
        int pageSize = inputDto.getItemsPerPage();

        // 构建请求
        QueryBuildHelper queryBuilder = new QueryBuildHelper();
        QueryBuildHelper.ParamBuilder paramBuilder = couponQueryParamBuild(inputDto);
        queryBuilder.setParamBuilder(paramBuilder);

        // 设置排序
        if (CouponStatusEnum.STATUS_DONATE.getStatus().equals(inputDto.getCouponStatus()) || CouponStatusEnum.STATUS_ASSIGN.getStatus().equals(inputDto.getCouponStatus())) {
            queryBuilder.descSort(CouponEsDoc.UPDATE_TIME);
        } else if (null != inputDto.getType() && CouponConstant.COUPON_QUERY_TYPE == inputDto.getType().intValue()) {
            queryBuilder.descSort(CouponEsDoc.USED_TIME);
        } else if (null == inputDto.getType() || CouponConstant.COUPON_QUERY_TYPE != inputDto.getType().intValue()) {
            queryBuilder.descSort(CouponEsDoc.CREATE_TIME);
        }

        queryBuilder.setPage(current, pageSize);

        Page<CouponEsDoc> pageResponse = couponEsDocService.searchPage(queryBuilder.getQueryBuilder());
        return pageResponse;
    }

    /**
     * 构建券明细查询条件
     *
     * @param inputDto
     * @return
     */
    private QueryBuildHelper.ParamBuilder couponQueryParamBuild(CouponQueryRequest inputDto) {
        QueryBuildHelper queryBuilder = new QueryBuildHelper();
        QueryBuildHelper.ParamBuilder paramBuilder = queryBuilder.getParamBuilder();
        //券状态为4已失效
        if (CouponStatusEnum.STATUS_INVALID.getStatus().equals(inputDto.getCouponStatus())) {
            List<Integer> statusList = Arrays.asList(CouponStatusEnum.STATUS_USED.getStatus(), CouponStatusEnum.STATUS_CANCEL.getStatus(),
                    CouponStatusEnum.STATUS_DONATE.getStatus(), CouponStatusEnum.STATUS_ASSIGN.getStatus(), CouponStatusEnum.STATUS_FREEZE.getStatus());
            paramBuilder.andIn(CouponEsDoc.STATUS, statusList);

            paramBuilder.andLt(CouponEsDoc.END_TIME, new Date());
        }

        // 券状态不等于4(已失效)
        if (null != inputDto.getCouponStatus() && !CouponStatusEnum.STATUS_INVALID.getStatus().equals(inputDto.getCouponStatus())) {
            paramBuilder.andEq(CouponEsDoc.STATUS, inputDto.getCouponStatus());
        }

        if (CouponStatusEnum.STATUS_ISSUE.getStatus().equals(inputDto.getCouponStatus()) || CouponStatusEnum.STATUS_USE.getStatus().equals(inputDto.getCouponStatus())) {
            // 券状态为待发行、可使用： and (c.end_time is null or c.end_time &gt;= now())
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateUtils.FORMAT_YYYY_MM_DD_HH_MM_SS));
            paramBuilder.andQueryBuilder(QueryBuilders.boolQuery().should(new BoolQueryBuilder().mustNot(new ExistsQueryBuilder(CouponEsDoc.END_TIME)))
                    .should(new RangeQueryBuilder(CouponEsDoc.END_TIME).gte(now)));
        }

        // and c.end_time  &gt;= #{couponEffectiveStartTime}
        paramBuilder.andGte(CouponEsDoc.END_TIME, inputDto.getCouponEffectiveStartTime());
        // and c.start_time  &lt;= #{couponEffectiveEndTime}
        paramBuilder.andLte(CouponEsDoc.START_TIME, inputDto.getCouponEffectiveEndTime());
        // and c.create_time &gt;= #{couponCreateStartTime}
        paramBuilder.andGte(CouponEsDoc.CREATE_TIME, inputDto.getCouponCreateStartTime());
        // and c.create_time &lt;= #{couponCreateEndTime}
        paramBuilder.andLte(CouponEsDoc.CREATE_TIME, inputDto.getCouponCreateEndTime());
        // and c.user_type =#{crowdScopeId}
        paramBuilder.andEq(CouponEsDoc.USER_TYPE, inputDto.getCrowdScopeId());
        // and c.receive_user_type =#{receiveUserType}
        paramBuilder.andEq(CouponEsDoc.RECEIVE_USER_TYPE, inputDto.getReceiveUserType());
        // and c.receive_user_mobile LIKE CONCAT(#{receiveUserMobile}, '%')
        paramBuilder.andPrefixLike(CouponEsDoc.RECEIVE_USER_MOBILE, inputDto.getReceiveUserMobile());

        if (CouponSourceTypeEnum.COUPON_SOURCE_PRESENTED.getSource().equals(inputDto.getSource()) || CouponSourceTypeEnum.COUPON_SOURCE_ASSIGN.getSource().equals(inputDto.getSource())) {
            // "source == 5 || source == 23"
            paramBuilder.andIn(CouponEsDoc.SOURCE, Arrays.asList(CouponSourceTypeEnum.COUPON_SOURCE_PRESENTED.getSource(), CouponSourceTypeEnum.COUPON_SOURCE_ASSIGN.getSource()));
        } else if (null != inputDto.getSource() && !CouponSourceTypeEnum.COUPON_SOURCE_PRESENTED.getSource().equals(inputDto.getSource()) && !CouponSourceTypeEnum.COUPON_SOURCE_ASSIGN.getSource().equals(inputDto.getSource())) {
            // source != null and source != 5 and source != 23
            paramBuilder.andEq(CouponEsDoc.SOURCE, inputDto.getSource());
        }

        if (org.apache.commons.lang3.StringUtils.isNotBlank(inputDto.getSourceId())) {
            // "source != null and (source == 5 || source == 23)"
            if (CouponSourceTypeEnum.COUPON_SOURCE_PRESENTED.getSource().equals(inputDto.getSource()) || CouponSourceTypeEnum.COUPON_SOURCE_ASSIGN.getSource().equals(inputDto.getSource())) {
                // and (c.source_id = #{sourceId} or give_user_mobile = #{sourceId})
                paramBuilder.andQueryBuilder(QueryBuilders.boolQuery().should(QueryBuilders.termQuery(CouponEsDoc.SOURCE_ID, inputDto.getSourceId()))
                        .should(QueryBuilders.termQuery(CouponEsDoc.GIVE_USER_MOBILE, inputDto.getSourceId())));
            } else {
                paramBuilder.andEq(CouponEsDoc.SOURCE_ID, inputDto.getSourceId());
            }
        }

        // and c.update_time &gt;= #{giveTimeFrom}
        paramBuilder.andGte(CouponEsDoc.UPDATE_TIME, inputDto.getGiveTimeFrom());
        // and  c.update_time &lt;= #{giveTimeTo}
        paramBuilder.andGte(CouponEsDoc.UPDATE_TIME, inputDto.getGiveTimeTo());
        // and c.coupon_code = #{couponCode}
        paramBuilder.andEq(CouponEsDoc.COUPON_CODE, inputDto.getCouponCode());
        // and  c.theme_title like #{couponActivityName} %
        paramBuilder.andPrefixLike(CouponEsDoc.THEME_TITLE_KEYWORD, inputDto.getCouponActivityName());
        // and c.bind_tel LIKE CONCAT(#{cellNo}, '%')
        paramBuilder.andPrefixLike(CouponEsDoc.BIND_TEL, inputDto.getCellNo());
        // and  c.coupon_theme_id = #{couponActivityId}
        paramBuilder.andEq(CouponEsDoc.COUPON_THEME_ID, inputDto.getCouponActivityId());
        // and c.coupon_type = #{couponType}
        paramBuilder.andEq(CouponEsDoc.COUPON_TYPE, inputDto.getCouponType());

        // "type == null or type != 1"
        if (null == inputDto.getType() || CouponConstant.COUPON_QUERY_TYPE != inputDto.getType().intValue()) {
            List<Long> themeIds = org.apache.commons.collections4.CollectionUtils.isEmpty(inputDto.getThemeIds()) ? Arrays.asList(-1L) : inputDto.getThemeIds();
            paramBuilder.andIn(CouponEsDoc.COUPON_THEME_ID, themeIds);
        }

        return paramBuilder;
    }

    @Override
    public Long exportCouponListAsync(CouponExportBo bo) {
        if (bo.getCouponStatus() != null && bo.getCouponStatus() == -1) {
            bo.setCouponStatus(null);
        }

        CouponQueryBo inputDto = new CouponQueryBo();
        BeanUtils.copyProperties(bo, inputDto);
        inputDto.setCurrentPage(1);
        inputDto.setItemsPerPage(1);

        Long generateBatchId = this.exportAsync(inputDto, CouponBatchTypeEnum.LOG_TYPE_EXPORT_COUPON, CouponExportResponse.class);
        return generateBatchId;
    }

    @Override
    public Long exportDonateCouponListAsync(DonateCouponsExportBo bo) {
        if (bo.getCouponStatus() != null && bo.getCouponStatus() == -1) {
            bo.setCouponStatus(null);
        }

        CouponQueryBo inputDto = new CouponQueryBo();
        BeanUtils.copyProperties(bo, inputDto);
        inputDto.setCurrentPage(1);
        inputDto.setItemsPerPage(1);

        Long generateBatchId;
        if (Objects.equals(CouponStatusEnum.STATUS_DONATE.getStatus(), inputDto.getCouponStatus())) {
            generateBatchId = this.exportAsync(inputDto, CouponBatchTypeEnum.LOG_TYPE_EXPORT_DONATE_COUPON, CouponDonateExportResponse.class);
        } else {
            generateBatchId = this.exportAsync(inputDto, CouponBatchTypeEnum.LOG_TYPE_EXPORT_ASSIGN_COUPON, CouponAssignExportResponse.class);
        }

        return generateBatchId;
    }

    @Override
    public List<CouponEntity> dynamicSelect(String tableName, CouponQueryWrapperBo param) {
        Assert.notNull(tableName);

        return this.baseMapper.selectByCouponQueryBo(tableName, param);
    }

    /**
     * 券明细导出文件异步处理
     *
     * @param inputDto
     * @param couponBatchTypeEnum
     * @param responseClass
     * @return
     */
    private Long exportAsync(CouponQueryBo inputDto, CouponBatchTypeEnum couponBatchTypeEnum, Class responseClass) {
        // 查询数据总数
        Page<CouponEsDoc> totalObj = searchCoupons(inputDto);
        int total = Long.valueOf(totalObj.getTotalElements()).intValue();

        // 生成导出记录
        Long generateBatchId = saveCouponGenerateBatch(Long.valueOf(total).intValue(), couponBatchTypeEnum);

        int totalPage = 1;
        if (total > EXPORT_DATA_PAGE_SIZE) {
            totalPage = total / EXPORT_DATA_PAGE_SIZE + (total % EXPORT_DATA_PAGE_SIZE > 0 ? 1 : 0);
        }

        int finalTotalPage = totalPage;
        couponBatchExecutor.execute(() -> {
            // 获取导出的券数据
            List<Object> rowDatas = batchCouponExportList(inputDto, finalTotalPage, responseClass);

            // 生成excel文件并上传
            FileUploadResultDto asyncResult = exportCouponAndUpload(rowDatas, responseClass);

            // 更新到导出记录
            updateCouponGenerateBatch(generateBatchId, asyncResult);
        });

        return generateBatchId;
    }

    /**
     * 批量获取需导出的券明细
     *
     * @param inputDto
     * @param finalTotalPage
     * @return
     */
    private List<Object> batchCouponExportList(CouponQueryBo inputDto, int finalTotalPage, Class typeClass) {
        List<Object> rowDatas = new ArrayList<>();

        // 分批获取
        for (int i = 0; i < finalTotalPage; i++) {
            inputDto.setCurrentPage(i);
            inputDto.setItemsPerPage(EXPORT_DATA_PAGE_SIZE);

            // 查询券明细
            PageResponse<CouponViewResponse> pageResponse = this.queryCouponByPageRequest(inputDto);
            List<CouponViewResponse> list = pageResponse.getListObj();

            if (typeClass == CouponExportResponse.class) {
                // 优惠券明细列表-》excel导出明细集合
                List<CouponExportResponse> rowList = list.stream().map(vo -> convertCouponExportResponse(vo)).collect(Collectors.toList());
                rowDatas.addAll(rowList);
            } else if (typeClass == CouponDonateExportResponse.class) {
                // 转赠优惠券列表-》excel导出明细集合
                List<CouponDonateExportResponse> rowList = list.stream().map(vo -> convertCouponDonateExportResponse(vo)).collect(Collectors.toList());
                rowDatas.addAll(rowList);
            } else if (typeClass == CouponAssignExportResponse.class) {
                // 转让优惠券列表-》excel导出明细集合
                List<CouponAssignExportResponse> rowList = list.stream().map(vo -> convertCouponAssignExportResponse(vo)).collect(Collectors.toList());
                rowDatas.addAll(rowList);
            }
        }

        return rowDatas;
    }

    /**
     * 导出优惠券明细并上传
     *
     * @param rowDatas
     * @return
     */
    private FileUploadResultDto exportCouponAndUpload(List<Object> rowDatas, Class classNme) {
        ByteArrayOutputStream outputStream = null;
        try {
            Class classType = CouponExportResponse.class;
            if (classNme == CouponDonateExportResponse.class) {
                classType = CouponDonateExportResponse.class;
            } else if (classNme == CouponAssignExportResponse.class) {
                classType = CouponAssignExportResponse.class;
            }

            //导出到文件
            outputStream = new ByteArrayOutputStream();
            excelExporter.export(outputStream, classType, rowDatas);
            //上传文件
            InputStream fsInputStream = new ByteArrayInputStream(outputStream.toByteArray());
            String fileName = String.format("coupon-data-%s%s", DateUtil.format(new Date(), DatePattern.PURE_DATETIME_PATTERN), ImportConstant.CSV_SUFFIX);
            MultipartFile multipartFile = new CommonMultipartFile("file", fileName, ImportConstant.EXCEL_2003_CONTENT_TYPE, fsInputStream);

            ResponseDto<String> responseDto = commonFileClient.uploadFileByFixedFileName(multipartFile, "/excel/export/couponData");
            String url = responseDto.getData();

            return FileUploadResultDto.builder().statusEnum(AsyncStatusEnum.FINISHED).uploadFile(url).build();
        } catch (ImportException ex) {
            log.error("导出优惠券明细生成excel文件异常", ex);
            return FileUploadResultDto.builder().statusEnum(AsyncStatusEnum.FAIL).errorReason(ex.getMessage()).build();
        } catch (Exception ex) {
            log.error("导出优惠券明细异常", ex);
            return FileUploadResultDto.builder().statusEnum(AsyncStatusEnum.FAIL).errorReason("导出异常").build();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ex) {
                    log.error("导出优惠券明细IO关闭异常", ex);
                }
            }
        }
    }

    /**
     * 转让优惠券明细导出对象转换
     *
     * @param vo
     * @return
     */
    private CouponAssignExportResponse convertCouponAssignExportResponse(CouponViewResponse vo) {
        CouponDonateExportResponse convertBean = this.convertCouponDonateExportResponse(vo);

        CouponAssignExportResponse data = new CouponAssignExportResponse();
        BeanUtils.copyProperties(convertBean, data);
        return data;
    }

    /**
     * 转赠优惠券明细导出对象转换
     *
     * @param vo
     * @return
     */
    private CouponDonateExportResponse convertCouponDonateExportResponse(CouponViewResponse vo) {
        CouponDonateExportResponse exportResponse = new CouponDonateExportResponse();
        exportResponse.setCouponThemeId(vo.getCouponThemeId() + "");
        exportResponse.setThemeTitle(vo.getThemeTitle());
        exportResponse.setCouponCode(vo.getCouponCode());
        exportResponse.setThirdCouponCode(vo.getThirdCouponCode());
        exportResponse.setCreateTime(DateUtils.parseDateToString(vo.getCreateTime(), DateUtils.FORMAT_YYYY_MM_DD_HH_MM_SS));
        exportResponse.setOrgNames(String.join(",", vo.getOrgNames()));
        exportResponse.setValidTimeStr(null == vo.getStartTime() ? "" : (DateUtils.parseDateToString(vo.getStartTime(), DateUtils.FORMAT_YYYY_MM_DD_HH_MM_SS) + " - " + DateUtils.parseDateToString(vo.getEndTime(), DateUtils.FORMAT_YYYY_MM_DD_HH_MM_SS)));
        exportResponse.setCouponUserType(UserTypeEnum.getStrByUserType(vo.getCrowdScopeId()));
        exportResponse.setCellNo(vo.getBindTel());

        String status = null;
        if (vo.getStatus() != null) {
            if (CouponStatusEnum.STATUS_ISSUE.getStatus().equals(vo.getStatus()) || CouponStatusEnum.STATUS_USE.getStatus().equals(vo.getStatus())) {
                if (vo.getEndTime() != null) {
                    if (vo.getEndTime().before(new Date())) {
                        status = "已过期";
                    }
                }
            }
        }
        exportResponse.setStatus(status);
        exportResponse.setBindTime(DateUtils.parseDateToString(vo.getBindTime(), DateUtils.FORMAT_YYYY_MM_DD_HH_MM_SS));
        exportResponse.setDonateAccountType(UserTypeEnum.getStrByUserType(vo.getReceiveUserType()));
        exportResponse.setDonateTimeName(DateUtils.parseDateToString(vo.getGiveTime(), DateUtils.FORMAT_YYYY_MM_DD_HH_MM_SS));
        exportResponse.setDonateMobile(vo.getReceiveUserMobile());
        return exportResponse;
    }

    /**
     * 转换CouponExportResponse导出对象
     *
     * @param vo
     * @return
     */
    private CouponExportResponse convertCouponExportResponse(CouponViewResponse vo) {
        CouponExportResponse exportResponse = new CouponExportResponse();
        exportResponse.setCouponThemeId(vo.getCouponThemeId() + "");
        exportResponse.setThemeTitle(vo.getThemeTitle());
        exportResponse.setCouponCode(vo.getCouponCode());
        exportResponse.setThirdCouponCode(vo.getThirdCouponCode());
        exportResponse.setSourceStr(vo.getSourceStr());
        exportResponse.setSourceId(vo.getSourceId());
        exportResponse.setCreateTime(DateUtils.parseDateToString(vo.getCreateTime(), DateUtils.FORMAT_YYYY_MM_DD_HH_MM_SS));
        exportResponse.setOrgNames(String.join(",", vo.getOrgNames()));
        exportResponse.setValidTimeStr(null == vo.getStartTime() ? "" : (DateUtils.parseDateToString(vo.getStartTime(), DateUtils.FORMAT_YYYY_MM_DD_HH_MM_SS) + " - " + DateUtils.parseDateToString(vo.getEndTime(), DateUtils.FORMAT_YYYY_MM_DD_HH_MM_SS)));
        exportResponse.setCouponUserType(UserTypeEnum.getStrByUserType(vo.getCrowdScopeId()));
        exportResponse.setCellNo(vo.getBindTel());

        String status = org.apache.commons.lang3.StringUtils.EMPTY;
        if (CouponStatusEnum.STATUS_ISSUE.getStatus().equals(vo.getStatus()) || CouponStatusEnum.STATUS_USE.getStatus().equals(vo.getStatus())) {
            if (vo.getEndTime() != null && vo.getEndTime().before(new Date())) {
                status = "已过期";
            } else {
                status = CouponStatusEnum.getStrByStatus(vo.getStatus());
            }
        } else {
            status = CouponStatusEnum.getStrByStatus(vo.getStatus());
        }
        exportResponse.setStatus(status);
        exportResponse.setBindTime(DateUtils.parseDateToString(vo.getBindTime(), DateUtils.FORMAT_YYYY_MM_DD_HH_MM_SS));

        return exportResponse;
    }

    /**
     * 生成excel导出记录
     *
     * @param total
     * @return
     */
    private Long saveCouponGenerateBatch(Integer total, CouponBatchTypeEnum couponBatchTypeEnum) {
        CouponGenerateBatchEntity couponGenerateBatchEntity = new CouponGenerateBatchEntity();
        couponGenerateBatchEntity.setType(couponBatchTypeEnum.getType());
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

    /**
     * 更新导出记录
     *
     * @param generateBatchId
     * @param exportResult
     * @return
     */
    private void updateCouponGenerateBatch(Long generateBatchId, FileUploadResultDto exportResult) {
        CouponGenerateBatchEntity generateBatchEntity = new CouponGenerateBatchEntity();
        generateBatchEntity.setId(generateBatchId);
        generateBatchEntity.setSendCouponStatus(exportResult.getStatusEnum().getStatus());
        generateBatchEntity.setUploadFile(exportResult.getUploadFile());
        generateBatchEntity.setFailReason(exportResult.getErrorReason());
        generateBatchEntity.setFinishTime(new Date());
        couponGenerateBatchService.updateById(generateBatchEntity);
    }

    /**
     * 查询参数过滤
     *
     * @param sourceInputDto
     */
    private CouponQueryBo transQueryParam(final CouponQueryBo sourceInputDto) {
        CouponQueryBo inputDto = new CouponQueryBo();
        BeanUtils.copyProperties(sourceInputDto, inputDto);

        // 登录用户id
        Long userId = inputDto.getUserId();
        String userIdStr = userId.toString();

        if (inputDto.getCouponStatus() != null && inputDto.getCouponStatus() == -1) {
            inputDto.setCouponStatus(null);
        }

        if (CouponStatusEnum.STATUS_ALL.getStatus().equals(inputDto.getSource())) {
            inputDto.setSource(null);
        }

        if (StringUtils.isNotBlank(inputDto.getCouponCode())) {
            // 加密传到数据库，查询
            inputDto.setCouponCode(AESPromotionUtil.encrypt(inputDto.getCouponCode()));
        }

        List<Long> authStoreIdList = new ArrayList<>();
        StoreInfo storeInfo = midPlatformLoginHelper.getStoreInfoByUserId(userIdStr);
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(storeInfo.getAuthStoreList())) {
            authStoreIdList = storeInfo.getAuthStoreList().stream().map(AuthStoreDTO::getStoreId).collect(Collectors.toList());
        }

        List<AuthMerchantDTO> authMerchantDTOList = midPlatformLoginHelper.getMerchantInfoByUserId(userIdStr).getAuthMerchantList();

        // 店铺（楼盘id）不为空需要过滤掉没有权限的数据
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(inputDto.getStoreIds())) {
            List<Long> finalAuthStoreIdList = authStoreIdList;
            List<Long> storeIdList = inputDto.getStoreIds().stream().filter(item -> finalAuthStoreIdList.contains(item)).collect(Collectors.toList());
            inputDto.setStoreIds(storeIdList);
        }

        // 店铺（楼盘id）为空就看商家（分公司）权限
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(inputDto.getStoreIds()) && org.apache.commons.collections4.CollectionUtils.isNotEmpty(authMerchantDTOList)) {
            List<Long> authMerchantIdList = authMerchantDTOList.stream().map(AuthMerchantDTO::getMerchantId).collect(Collectors.toList());
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(inputDto.getMerchantIds())) {
                List<Long> merchantIdList = inputDto.getMerchantIds().stream().filter(item -> authMerchantIdList.contains(item)).collect(Collectors.toList());
                inputDto.setMerchantIds(merchantIdList);
            } else {
                inputDto.setMerchantIds(authMerchantIdList);
            }
        }

        // 兼容房商城集团（总部）店铺限制
        List<Long> authOrgIds = authMerchantDTOList.stream()
                .filter(authMerchantDTO -> Objects.equals(authMerchantDTO.getOrgLevelCode(), OrgLevelEnum.GROUP.getOrgLevelCode()))
                .map(AuthMerchantDTO::getMerchantId).collect(Collectors.toList());
        inputDto.setGroupIds(authOrgIds);
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(inputDto.getStoreIds())) {
            inputDto.setStoreIds(authStoreIdList);
        }

        //优惠券-数据权限
        Set<Long> authOrgList = authMerchantDTOList.stream().map(o -> o.getMerchantId()).collect(Collectors.toSet());
        authOrgList.addAll(authStoreIdList);
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(authOrgList)) {
            List<Long> orgIds = inputDto.getOrgIds();
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(orgIds)) {
                List<Long> collect = orgIds.stream().filter(o -> authOrgList.contains(o)).collect(Collectors.toList());
                inputDto.setOrgIds(collect);
            } else {
                inputDto.setOrgIds(new ArrayList<>(authOrgList));
            }
        }

        List<Long> themeIds = couponThemeService.queryAuthThemeId(inputDto.getOrgIds());
        inputDto.setThemeIds(themeIds);

        return inputDto;
    }


    /**
     * 查询优惠券活动所属组织信息
     *
     * @param couponThemeIds
     * @return
     */
    private List<CouponThemeOrgEntity> queryCouponThemeOrgBycouponThemeIds(Set<Long> couponThemeIds) {
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(couponThemeIds)) {
            return Collections.EMPTY_LIST;
        }
        QueryWrapper<CouponThemeOrgEntity> couponThemeOrgQuery = new QueryWrapper<>();
        couponThemeOrgQuery.in("coupon_theme_id", couponThemeIds);
        List<CouponThemeOrgEntity> couponThemeOrgList = couponThemeOrgService.getBaseMapper().selectList(couponThemeOrgQuery);
        return couponThemeOrgList;
    }

    /**
     * 查询券集合
     *
     * @param couponIdList
     * @return
     */
    private List<CouponEntity> queryCouponsByIds(List<Long> couponIdList) {
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(couponIdList)) {
            return Collections.EMPTY_LIST;
        }
        QueryWrapper<CouponEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", couponIdList);
        List<CouponEntity> couponEntityList = baseMapper.selectList(queryWrapper);
        return couponEntityList;
    }


    /**
     * 获取组织信息
     *
     * @param orgList
     * @return
     */
    private Map<Long, OrgOutDto> queryOrgInfo(Set<Long> orgList) {
        if (orgList == null || orgList.size() == 0) {
            return new HashMap<>();
        }

        OrgIdsInput orgIdsInput = new OrgIdsInput();
        orgIdsInput.setIds(new ArrayList<>(orgList));
        InputDto<OrgIdsInput> inputDto = new InputDto<>();
        inputDto.setData(orgIdsInput);
        OutputDto<List<OrgOutDto>> orgOutInfo = ouserWebFeignClient.findByOrgId(inputDto);
        return orgOutInfo.getData().stream().collect(Collectors.toMap(OrgOutDto::getOrgId, Function.identity()));
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invalidCouponWithTx(CouponInvalidBo bo) {
        List<Long> couponIds = bo.getIdList();
        List<CouponEntity> couponEntities = this.baseMapper.selectBatchIds(couponIds);

        if (org.apache.commons.collections4.CollectionUtils.isEmpty(couponEntities)) {
            return;
        }

        try {
            couponEntities.forEach(coupon -> {
                if (Objects.equals(coupon.getStatus(), CouponStatusEnum.STATUS_USED.getStatus())) {
                    // 已使用状态的优惠券不能作废
                    throw new BusinessException(CouponErrorCode.UPDATE_COUPON_INVALID_USED_NOT_ALLOWED.getCode(),
                            CouponErrorCode.UPDATE_COUPON_INVALID_USED_NOT_ALLOWED.getMessage());
                }

//                // 解密后写入操作日志
//                coupon.setCouponCode(AESPromotionUtil.decrypt(coupon.getCouponCode()));

                // 记录优惠券操作日志 作废
                OprLogDo oprLogDo = new OprLogDo();
                oprLogDo.setRefId(coupon.getId());
                oprLogDo.setOprType(LogOprType.INVALID);
                oprLogDo.setOprThemeType(LogOprThemeType.COUPON);
                couponOprLogService.saveOprLog(oprLogDo);
            });

            // 更新coupon、coupon_user表的状态
            this.updateCouponStatus(CouponStatusEnum.STATUS_CANCEL.getStatus(), couponIds);

            // 同步es
            couponEsManageService.refreshEsByCouponIds(couponIds);

            // 触发保底券事件
            sendBaoDiMsg(couponIds);
        } catch (BusinessException e) {
            log.error(e.getCode(), e);
            throw e;
        } catch (Exception e) {
            log.error(CouponErrorCode.UPDATE_COUPON_INVALID_EXCEPTION.getCode(), e);
            throw new BusinessException(CouponErrorCode.UPDATE_COUPON_INVALID_EXCEPTION.getCode(), CouponErrorCode.UPDATE_COUPON_INVALID_EXCEPTION.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void freezeCouponWithTx(FreezeCouponBo bo) {
        List<Long> couponIds = bo.getIdList();
        List<CouponEntity> couponEntities = this.baseMapper.selectBatchIds(couponIds);
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(couponEntities)) {
            return;
        }

        try {
            CouponEntity couponPo = new CouponEntity();
            Integer logOprType = 0;
            if (bo.getFreeze()) {
                couponEntities.forEach(coupon -> {
                    // 冻结
                    if (!Arrays.asList(CouponStatusEnum.STATUS_ISSUE.getStatus(), CouponStatusEnum.STATUS_USE.getStatus()).contains(coupon.getStatus())) {
                        // 已使用状态的优惠券不能冻结
                        throw new BusinessException(CouponErrorCode.UPDATE_COUPON_FREEZE_STATUS_NOT_ALLOWED);
                    }
                });

                couponPo.setStatus(CouponStatusEnum.STATUS_FREEZE.getStatus());
                logOprType = LogOprType.FREEZE.getType();
            } else {
                couponEntities.forEach(coupon -> {
                    // 解冻
                    CouponThemeEntity couponThemePO = couponThemeService.getById(coupon.getCouponThemeId());
                    CouponUserEntity couponUser = couponUserService.getById(coupon.getId());

                    if (Objects.isNull(coupon.getEndTime()) || coupon.getStartTime().getTime() > System.currentTimeMillis()) {
                        couponPo.setStatus(CouponStatusEnum.STATUS_ISSUE.getStatus());
                    } else if (coupon.getEndTime().getTime() < System.currentTimeMillis()) {
                        couponPo.setStatus(CouponStatusEnum.STATUS_INVALID.getStatus());
                    } else if ((couponUser.getBindTel() == null) && (couponThemePO.getCouponGiveRule().equals(CouponGiveRuleEnum.COUPON_GIVE_RULE_OFFLINE_PREFABRICATED.getType()))) {
                        couponPo.setStatus(CouponStatusEnum.STATUS_ISSUE.getStatus());
                    } else {
                        couponPo.setStatus(CouponStatusEnum.STATUS_USE.getStatus());
                    }
                });

                logOprType = LogOprType.UNFREEZE.getType();
            }

            // 更新coupon、coupon_user表的状态
            this.updateCouponStatus(couponPo.getStatus(), couponIds);

            Integer finalLogOprType = logOprType;
            couponEntities.forEach(coupon -> {
                OprLogDo oprLogDo = new OprLogDo();
                oprLogDo.setOprUserId(bo.getUserId());
                oprLogDo.setOprUserName(bo.getUsername());
                oprLogDo.setRefId(coupon.getId());
                oprLogDo.setOprType(LogOprType.of(finalLogOprType));
                oprLogDo.setOprThemeType(LogOprThemeType.COUPON);
                couponOprLogService.saveOprLog(oprLogDo);

                // 同步es
                couponEsManageService.refreshEsByCouponIds(Arrays.asList(coupon.getId()));
            });

            if (bo.getFreeze()) {
                sendBaoDiMsg(couponIds);
            }
        } catch (BusinessException e) {
            log.error(e.getCode(), e);
            throw e;
        } catch (Exception e) {
            log.error(CouponErrorCode.UPDATE_COUPON_FREEZE_EXCEPTION.getCode(), e);
            throw new BusinessException(CouponErrorCode.UPDATE_COUPON_FREEZE_EXCEPTION);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void postponeCouponWithTx(PostponeCouponBo bo) {
        List<Long> idList = bo.getIdList();
        List<CouponEntity> couponEntities = this.baseMapper.selectBatchIds(idList);
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(couponEntities)) {
            return;
        }

        List<Long> couponIds = couponEntities.stream().map(CouponEntity::getId).collect(Collectors.toList());

        try {
            couponEntities.forEach(coupon -> {
                if (Objects.equals(coupon.getStatus(), CouponStatusEnum.STATUS_USED.getStatus())) {
                    throw new BusinessException(CouponErrorCode.UPDATE_COUPON_POSTPONE_USED_NOT_ALLOWED.getCode(),
                            CouponErrorCode.UPDATE_COUPON_POSTPONE_USED_NOT_ALLOWED.getMessage());
                }
            });

            CouponEntity couponPo = new CouponEntity();
            couponPo.setEndTime(bo.getPostponeDate());
            // 更新状态
            UpdateWrapper updateWrapper = new UpdateWrapper();
            updateWrapper.in("id", couponIds);
            this.baseMapper.update(couponPo, updateWrapper);

            couponEntities.forEach(coupon -> {
                // 记录优惠券操作日志：延期
                OprLogDo oprLogDo = new OprLogDo();
                oprLogDo.setOprUserId(bo.getUserId());
                oprLogDo.setOprUserName(bo.getUsername());
                oprLogDo.setRefId(coupon.getId());
                oprLogDo.setOprType(LogOprType.EXTENSION);
                oprLogDo.setOprThemeType(LogOprThemeType.COUPON);

                String postponeDate = DateUtils.parseDateToString(bo.getPostponeDate(), DateUtils.FORMAT_YYYY_MM_DD_HH_MM_SS);
                String operContent = "延期至" + postponeDate;
                oprLogDo.setOprContent(operContent);
                couponOprLogService.saveOprLog(oprLogDo);

                // 同步es
                couponEsManageService.refreshEsByCouponIds(Arrays.asList(coupon.getId()));
            });
        } catch (BusinessException e) {
            log.error(e.getCode(), e);
            throw e;
        } catch (Exception e) {
            log.error(CouponErrorCode.UPDATE_COUPON_POSTPONE_EXCEPTION.getCode(), e);
            throw new BusinessException(CouponErrorCode.UPDATE_COUPON_POSTPONE_EXCEPTION);
        }
    }


    /**
     * 发送保底券
     *
     * @param couponIds 券id
     */
    private void sendBaoDiMsg(List<Long> couponIds) {
        QueryWrapper<CouponUserEntity> couponUserQuery = new QueryWrapper<>();
        couponUserQuery.in("coupon_id", couponIds);
        couponUserQuery.select("bind_tel");
        List<CouponUserEntity> couponUserList = couponUserService.getBaseMapper().selectList(couponUserQuery);

        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(couponUserList)) {
            /**
             * 判断用户是否有进行中的保底券，没有则调用发券
             */
            List<String> mobiles = couponUserList.stream().map(CouponUserEntity::getBindTel).distinct().collect(Collectors.toList());
            publisher.publishEvent(new MinCouponEvent(mobiles));
        }
    }

    /**
     * 更新券状态
     *
     * @param status
     * @param couponIds
     */
    private void updateCouponStatus(Integer status, List<Long> couponIds) {
        // 更新coupon、coupon_user表的状态
        CouponEntity couponPo = new CouponEntity();
        couponPo.setStatus(status);
        UpdateWrapper updateWrapper = new UpdateWrapper();
        updateWrapper.in("id", couponIds);
        this.baseMapper.update(couponPo, updateWrapper);

        CouponUserEntity couponUser = new CouponUserEntity();
        couponUser.setStatus(status);
        UpdateWrapper updateUserWrapper = new UpdateWrapper();
        updateUserWrapper.in("coupon_id", couponIds);
        couponUserService.getBaseMapper().update(couponUser, updateUserWrapper);
    }




    @Override
    @Transactional
    public void generateCouponsWithTx(List<CouponEntity> couponEntityList) {
        CouponEntity first = couponEntityList.stream().findFirst().get();
        Long couponThemeId = first.getCouponThemeId();
        int generateCount = couponEntityList.size();
        // 更新coupon_Theme_statistic表
        couponThemeStatisticService.incrCreateCountById(couponThemeId, generateCount);
        // 更新couponThemeCache
        couponThemeCacheService.incrCreatedCountById(couponThemeId, generateCount);

        try {
            // 操作coupon表可能数据量比较大，尽量避免此表回滚 放最后
            saveBatch(couponEntityList);
        } catch (Exception e) {
            log.error("generateCouponsWithTx生券失败: message={}", e.getMessage(), e);
            // 手动回滚couponThemeCache
            couponThemeCacheService.incrCreatedCountById(couponThemeId, -generateCount);
            throw e;
        }
    }


    @Override
    @Transactional
    public void generateThirdCouponsWithTx(List<CouponEntity> couponEntityList, List<CouponThirdEntity> couponThirdEntities) {
        //保存第三方券码信息
        couponThirdService.saveBatch(couponThirdEntities);
        //保存优惠券信息
        generateCouponsWithTx(couponEntityList);
    }

    @Override
    public void syncCouponEs(List<CouponEntity> couponEntityList) {
        List<CouponEsDoc> esDocList = new ArrayList<>();
        couponEntityList.forEach(bean -> {
            CouponEsDoc doc = new CouponEsDoc();
            BeanUtil.copyProperties(bean, doc);
            esDocList.add(doc);
        });
        // 同步es
        couponEsDocService.saveBatch(esDocList);
    }


}
