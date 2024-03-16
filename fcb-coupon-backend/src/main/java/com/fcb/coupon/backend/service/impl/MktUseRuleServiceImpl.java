package com.fcb.coupon.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fcb.coupon.backend.constant.OrgImportTemplateConstant;
import com.fcb.coupon.backend.exception.CouponThemeErrorCode;
import com.fcb.coupon.backend.exception.MktUseRuleErrorCode;
import com.fcb.coupon.backend.mapper.MktUseRuleMapper;
import com.fcb.coupon.backend.model.ao.AddOrgAo;
import com.fcb.coupon.backend.model.ao.DeleteOrgAo;
import com.fcb.coupon.backend.model.bo.*;
import com.fcb.coupon.backend.model.dto.*;
import com.fcb.coupon.backend.model.entity.CouponThemeEntity;
import com.fcb.coupon.backend.model.entity.MktUseRuleEntity;
import com.fcb.coupon.backend.model.param.response.MktUseRuleByIdsResponse;
import com.fcb.coupon.backend.model.param.response.MktUseRuleOrgListResponse;
import com.fcb.coupon.backend.model.param.response.MktUseRuleSelectionResponse;
import com.fcb.coupon.backend.model.param.response.PageResponse;
import com.fcb.coupon.backend.remote.client.OuserWebFeignClient;
import com.fcb.coupon.backend.remote.dto.input.InputDto;
import com.fcb.coupon.backend.remote.dto.out.OutputDto;
import com.fcb.coupon.backend.service.CouponOprLogService;
import com.fcb.coupon.backend.service.CouponThemeService;
import com.fcb.coupon.backend.service.MktUseRuleService;
import com.fcb.coupon.common.constant.CouponConstant;
import com.fcb.coupon.common.constant.RedisLockKeyConstant;
import com.fcb.coupon.common.constant.RestConstant;
import com.fcb.coupon.common.dto.AuthMerchantDTO;
import com.fcb.coupon.common.dto.AuthStoreDTO;
import com.fcb.coupon.common.dto.MerchantInfo;
import com.fcb.coupon.common.dto.StoreInfo;
import com.fcb.coupon.common.enums.*;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.midplatform.MidPlatformLoginHelper;
import com.fcb.coupon.common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author HanBin_Yang
 * @since 2021/6/21 10:39
 */
@Service
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
@Slf4j
@RefreshScope
public class MktUseRuleServiceImpl extends ServiceImpl<MktUseRuleMapper, MktUseRuleEntity> implements MktUseRuleService {
    private final CouponThemeService couponThemeService;
    private final MidPlatformLoginHelper midPlatformLoginHelper;
    private final CouponOprLogService couponOprLogService;
    private final OuserWebFeignClient ouserWebFeignClient;
    private final RestTemplate restTemplate;

    @Autowired
    private MktUseRuleService mktUseRuleService;

    @Value("${remote.url.middleend.adminportal}")
    private String middleendUrl;

    /**
     * 切分list查询组织size
     */
    public final int ORG_QUERY_PAGE_SIZE = 1000;

    @Override
    public Map<String, Integer> addOrgBatch(CouponThemeAddOrgBo bo) {
        CouponThemeEntity couponThemeDb = getCouponThemeDb(bo.getCouponThemeId());

        // 获取coupon_theme表配置的适用人群
        JSONArray themeDbPubPorts = getCouponThemePubPorts(couponThemeDb.getApplicableUserTypes());

        List<AddOrgAo> orgAddList = bo.getOrgAddList();
        switch (MktUseRuleTypeEnum.of(bo.getRuleType())) {
            case STORE:
                // 刷新组织缓存，防止操作了楼盘上下架不能实时同步
                refreshAuthorOrgCache(bo.getUt());
                // 获取权限店铺
                StoreInfo storeInfo = midPlatformLoginHelper.getStoreInfoByUserId(String.valueOf(bo.getUserId()));
                // storeInfoMap
                Map<Long, AuthStoreDTO> storeInfoMap = storeInfo.getAuthStoreList().stream().collect(Collectors.toMap(AuthStoreDTO::getStoreId, Function.identity(), (f, l) -> f));
                // 校验楼盘上下架 并且填充需要的数据
                for (int i = 0; i < orgAddList.size(); i++) {
                    AddOrgAo item = orgAddList.get(i);
                    if (storeInfoMap.containsKey(item.getOrgId())) {
                        AuthStoreDTO authStoreDto = storeInfoMap.get(item.getOrgId());
                        // 校验上下架
                        validateStoreOnline(themeDbPubPorts, authStoreDto);
                        // 填充店铺名称
                        item.setOrgName(authStoreDto.getStoreName());
                        // 填充楼盘编码
                        item.setOrgCode(authStoreDto.getBuildCode());

                    } else {
                        throw new BusinessException(MktUseRuleErrorCode.STORE_NOT_EXIST.getCode(), MktUseRuleErrorCode.STORE_NOT_EXIST.getMessage() + "，所在索引：" + i);
                    }
                }
                break;
            case MERCHANT:
                MerchantInfo merchantInfo = midPlatformLoginHelper.getMerchantInfoByUserId(String.valueOf(bo.getUserId()));

                Map<Long, AuthMerchantDTO> authMerchantMap = merchantInfo.getAuthMerchantList().stream().filter(bean -> StringUtils.equals(MktUseRuleTypeEnum.MERCHANT.getOrgLevelCode(), bean.getOrgLevelCode())).collect(Collectors.toMap(AuthMerchantDTO::getMerchantId, Function.identity(), (f, l) -> f));
                // 填充更多需要的数据
                fillMoreMessageToAddOrgList(orgAddList, authMerchantMap, MktUseRuleTypeEnum.getOrgLevelNameByType(bo.getRuleType()));
                break;
            case GROUP:
                MerchantInfo groupInfo = midPlatformLoginHelper.getMerchantInfoByUserId(String.valueOf(bo.getUserId()));

                Map<Long, AuthMerchantDTO> authGroupMap = groupInfo.getAuthMerchantList().stream().filter(bean -> StringUtils.equals(MktUseRuleTypeEnum.GROUP.getOrgLevelCode(), bean.getOrgLevelCode())).collect(Collectors.toMap(AuthMerchantDTO::getMerchantId, Function.identity(), (f, l) -> f));
                // 填充更多需要的数据
                fillMoreMessageToAddOrgList(orgAddList, authGroupMap, MktUseRuleTypeEnum.getOrgLevelNameByType(bo.getRuleType()));
                break;
            default:
                throw new BusinessException(MktUseRuleErrorCode.ORG_TYPE_NOT_SUPPORT);
        }

        String keyName = RedisLockKeyConstant.OPERATE_MKT_USE_RULE + bo.getCouponThemeId();
        return RedisUtil.executeLock(keyName, 60, TimeUnit.SECONDS, () -> doAddOrg(bo, couponThemeDb));
    }

    @Override
    @Transactional
    public void addOrgRelatedDataWithTx(List<MktUseRuleEntity> insertBeans) {
        baseMapper.insertBatch(insertBeans);
        // 删除不是这个组织范围的 例如：保存的是集团，则删除所有的分公司和楼盘，保存分公司和楼盘以此类推
        MktUseRuleEntity first = insertBeans.get(0);
        LambdaQueryWrapper<MktUseRuleEntity> queryWrapper = Wrappers.lambdaQuery(MktUseRuleEntity.class);
        queryWrapper
                .eq(MktUseRuleEntity::getThemeRef, first.getThemeRef())
                .ne(MktUseRuleEntity::getRuleType, first.getRuleType());
        baseMapper.delete(queryWrapper);
    }

    @Override
    public boolean removeOrgBatch(MktUseRuleDeleteOrgBo bo) {
        CouponThemeEntity couponThemeDb = getCouponThemeDb(bo.getCouponThemeId());

        List<DeleteOrgAo> deleteOrgList = bo.getDeleteOrgList();
        List<Long> ids = deleteOrgList.stream().map(DeleteOrgAo::getId).distinct().collect(Collectors.toList());

        mktUseRuleService.removeOrgRelatedDataWithTx(bo.getCouponThemeId(), bo.getRuleType(), ids);

        if (!CouponThemeStatus.CREATE.getStatus().equals(couponThemeDb.getStatus())) {
            StringBuilder oprContent = new StringBuilder();
            oprContent.append("删除").append(deleteOrgList.size()).append("个")
                    .append(MktUseRuleTypeEnum.getOrgLevelNameByType(bo.getRuleType())).append(": [");
            deleteOrgList.forEach(bean -> {
                oprContent.append(bean.getOrgName()).append("、");
            });
            oprContent.deleteCharAt(oprContent.length() - 1);
            oprContent.append("]");

            // 异步日志
            OprLogDo oprLogDo = OprLogDo.builder()
                    .oprUserId(bo.getUserId())
                    .oprUserName(bo.getUsername())
                    .oprContent(oprContent.toString())
                    .refId(bo.getCouponThemeId())
                    .oprThemeType(LogOprThemeType.COUPON_THEME)
                    .oprType(LogOprType.DELETE)
                    .build();
            couponOprLogService.saveOprLogAsync(oprLogDo);
        }

        return true;
    }

    @Override
    @Transactional
    public void removeOrgRelatedDataWithTx(Long couponThemeId, Integer ruleType, List<Long> ids) {
        LambdaQueryWrapper<MktUseRuleEntity> delQueryWrapper = Wrappers.lambdaQuery(MktUseRuleEntity.class);
        delQueryWrapper
                .in(MktUseRuleEntity::getId, ids)
                .eq(MktUseRuleEntity::getThemeRef, couponThemeId)
                .eq(MktUseRuleEntity::getRuleType, ruleType);
        baseMapper.delete(delQueryWrapper);

        // 删除不是这个组织范围的 例如：保存的是集团，则删除所有的分公司和楼盘，保存分公司和楼盘以此类推
        LambdaQueryWrapper<MktUseRuleEntity> queryWrapper = Wrappers.lambdaQuery(MktUseRuleEntity.class);
        queryWrapper
                .eq(MktUseRuleEntity::getThemeRef, couponThemeId)
                .ne(MktUseRuleEntity::getRuleType, ruleType);
        baseMapper.delete(queryWrapper);
    }

    @Override
    public Map<String, String> importOrgBatch(MktUseRuleImportAddOrgBo bo) {
        if (CollectionUtil.isEmpty(bo.getImportDataList())) {
            throw new BusinessException(MktUseRuleErrorCode.IMPORT_DATA_EMPTY);
        }

        Map<String, String> returnMap = new HashMap<>();

        CouponThemeEntity couponThemeDb = getCouponThemeDb(bo.getCouponThemeId());
        // 解析导入的数据
        Tuple2<Map<String, List<Integer>>, List<AddOrgAo>> resolveResult = resolveImportOrgData(bo, couponThemeDb);
        // 解析出来的有错误的信息
        Map<String, List<Integer>> validateErrorMap = resolveResult.getT1();
        // validateErrorMap有数据，说明有异常情况
        if (MapUtils.isNotEmpty(validateErrorMap)) {
            validateErrorMap.forEach((k, v) -> {
                StringBuilder sb = new StringBuilder();
                for (Integer line : v) {
                    sb.append(line).append("、");
                }
                sb.deleteCharAt(sb.length() - 1);
                returnMap.put(k, sb.toString());
            });
        } else {
            CouponThemeAddOrgBo couponThemeAddOrgBo = new CouponThemeAddOrgBo();
            couponThemeAddOrgBo.setUserId(bo.getUserId());
            couponThemeAddOrgBo.setUsername(bo.getUsername());
            couponThemeAddOrgBo.setUt(bo.getUt());
            couponThemeAddOrgBo.setCouponThemeId(bo.getCouponThemeId());
            couponThemeAddOrgBo.setRuleType(bo.getRuleType());
            couponThemeAddOrgBo.setOrgAddList(resolveResult.getT2());

            // 正式批量添加组织
            String keyName = RedisLockKeyConstant.OPERATE_MKT_USE_RULE + bo.getCouponThemeId();
            Map<String, Integer> addMap = RedisUtil.executeLock(keyName, 60, TimeUnit.SECONDS, () -> doAddOrg(couponThemeAddOrgBo, couponThemeDb));
            // map转换一下
            addMap.forEach((k, v) -> returnMap.put(k, String.valueOf(v)));
        }

        return returnMap;
    }

    private Tuple2<Map<String, List<Integer>>, List<AddOrgAo>> resolveImportOrgData(MktUseRuleImportAddOrgBo bo, CouponThemeEntity couponThemeDb) {
        JSONArray couponThemePubPorts = getCouponThemePubPorts(couponThemeDb.getApplicableUserTypes());
        // excel表第一列的数据
        List<AddOrgImportBo> importDataList = bo.getImportDataList();
        Set<String> fistRowDataSet = bo.getImportDataList().stream().map(AddOrgImportBo::getOrgCode).collect(Collectors.toSet());
        // 权限商家信息
        Map<String, AuthMerchantDTO> authMerchantCodeMap = new HashMap<>();
        // 权限店铺信息
        Map<String, AuthStoreDTO> authStoreMap = new HashMap<>();
        // 无权限但是数据库真实存在的
        Set<String> noAuthButExistOrgCodeSet = new HashSet<>();
        Set<String> noAuthButExistOrgNameSet = new HashSet<>();
        // Excel数据拆分处理，拆分成了2部分，1部分是权限的，另一部分是不在权限或者不存在的
        switch (MktUseRuleTypeEnum.of(bo.getRuleType())) {
            case MERCHANT:
                List<AuthMerchantDTO> authMerchantList = midPlatformLoginHelper.getMerchantInfoByUserId(String.valueOf(bo.getUserId())).getAuthMerchantList();
                if (CollectionUtil.isEmpty(authMerchantList)) {
                    throw new BusinessException(MktUseRuleErrorCode.NO_GROUP_AND_MERCHANT_PERMISSIONS);
                }
                // 权限商家，并且是excel表格内的
                authMerchantCodeMap = authMerchantList.stream().filter(item -> {
                    if (fistRowDataSet.contains(item.getMerchantCode().trim()) && MktUseRuleTypeEnum.MERCHANT.getOrgLevelCode().equals(item.getOrgLevelCode())) {
                        // 踢掉在权限范围内的数据，从这里之后，fistRowDataSet里的数据就只剩下不在权限的和不存在的
                        fistRowDataSet.remove(item.getMerchantCode().trim());
                        return true;
                    } else {
                        return false;
                    }
                }).collect(Collectors.toMap(item -> item.getMerchantCode().trim(), Function.identity()));

                if (CollectionUtils.isNotEmpty(fistRowDataSet)) {
                    fillNoAuthOrgData(fistRowDataSet, noAuthButExistOrgCodeSet, noAuthButExistOrgNameSet, MktUseRuleTypeEnum.MERCHANT.getOrgLevelCode());
                }
                break;
            case STORE:
                // 刷新组织缓存 防止楼盘上下架同步不实时
                refreshAuthorOrgCache(bo.getUt());
                List<AuthStoreDTO> authStoreList = midPlatformLoginHelper.getStoreInfoByUserId(String.valueOf(bo.getUserId())).getAuthStoreList();
                if (CollectionUtil.isEmpty(authStoreList)) {
                    throw new BusinessException(MktUseRuleErrorCode.NO_STORE_PERMISSIONS);
                }
                // 权限楼盘，并且是excel表格内的
                authStoreMap = authStoreList.stream().filter(item -> {
                    if (item.getBuildCode() != null && fistRowDataSet.contains(item.getBuildCode().trim())) {
                        // 踢掉在权限范围内的数据，从这里之后，fistRowDataSet里的数据就只剩下不在权限的和不存在的
                        fistRowDataSet.remove(item.getBuildCode().trim());
                        return true;
                    } else {
                        return false;
                    }
                }).collect(Collectors.toMap(item -> item.getBuildCode().trim(), Function.identity(), (f, l) -> f));

                if (CollectionUtils.isNotEmpty(fistRowDataSet)) {
                    fillNoAuthOrgData(fistRowDataSet, noAuthButExistOrgCodeSet, noAuthButExistOrgNameSet, MktUseRuleTypeEnum.STORE.getOrgLevelCode());
                }
                break;
            default:
                throw new BusinessException(MktUseRuleErrorCode.ORG_TYPE_NOT_SUPPORT);
        }

        String orgLevelName = MktUseRuleTypeEnum.getOrgLevelNameByType(bo.getRuleType());
        // 需要后续添加到数据库的数据
        List<AddOrgAo> orgAddList = new ArrayList<>();
        // 校验重复行用的
        MultiValueMap<String, Integer> codeLineMap = new LinkedMultiValueMap<>();
        // 错误的信息和所在行map
        MultiValueMap<String, Integer> multiValErrorMap = new LinkedMultiValueMap<>();
        // 开始循环校验
        for (int i = 0; i < importDataList.size(); i++) {
            int line = i + 2;
            AddOrgImportBo rowData = importDataList.get(i);
            String codeStr = rowData.getOrgCode().trim();
            String orgNameStr = rowData.getOrgName().trim();
            // 校验 编码空
            if (StringUtils.isBlank(codeStr)) {
                multiValErrorMap.add(String.format(OrgImportTemplateConstant.CODE_EMPTY, orgLevelName), line);
                continue;
            }
            // 校验 名称空
            if (StringUtils.isBlank(orgNameStr)) {
                multiValErrorMap.add(String.format(OrgImportTemplateConstant.NAME_EMPTY, orgLevelName), line);
                continue;
            }
            // 记录重复行数据， 如果codeLineList.size > 1则有重复行
            codeLineMap.add(codeStr, line);
            switch (MktUseRuleTypeEnum.of(bo.getRuleType())) {
                case MERCHANT:
                    MultiValueMap<String, Integer> merchantErrorMap = validateMerchantNameAndCodeInDb(line, codeStr, orgNameStr, authMerchantCodeMap, noAuthButExistOrgCodeSet, noAuthButExistOrgNameSet);
                    if (MapUtils.isEmpty(merchantErrorMap)) {
                        AuthMerchantDTO authMerchantDTO = authMerchantCodeMap.get(codeStr);
                        AddOrgAo addOrgAo = prepareAddOrgAoBean(authMerchantDTO.getMerchantId(), authMerchantDTO.getMerchantCode(), authMerchantDTO.getMerchantName());
                        orgAddList.add(addOrgAo);
                    } else {
                        multiValErrorMap.addAll(merchantErrorMap);
                    }
                    break;
                case STORE:
                    MultiValueMap<String, Integer> storeErrorMap = validateStoreNameAndCodeInDb(line, codeStr, orgNameStr, authStoreMap, noAuthButExistOrgCodeSet, noAuthButExistOrgNameSet, couponThemePubPorts);
                    if (MapUtils.isEmpty(storeErrorMap)) {
                        AuthStoreDTO authStoreDTO = authStoreMap.get(codeStr);
                        AddOrgAo addOrgAo = prepareAddOrgAoBean(authStoreDTO.getStoreId(), authStoreDTO.getStoreCode(), authStoreDTO.getStoreName());
                        orgAddList.add(addOrgAo);
                    } else {
                        multiValErrorMap.addAll(storeErrorMap);
                    }
                    break;
                default:
                    throw new BusinessException(MktUseRuleErrorCode.ORG_TYPE_NOT_SUPPORT);
            }
        }
        // 校验 重复行
        codeLineMap.forEach((orgCode, lineList) -> {
            if (lineList.size() > 1) {
                multiValErrorMap.addAll(String.format(OrgImportTemplateConstant.CODE_REPEAT, orgLevelName, orgCode), lineList);
            }
        });
        return Tuples.of(multiValErrorMap, orgAddList);
    }

    private MultiValueMap<String, Integer> validateStoreNameAndCodeInDb(int line, String codeStr, String nameStr, Map<String, AuthStoreDTO> authStoreMap, Set<String> noAuthButExistOrgCodes, Set<String> noAuthButExistOrgNameSet, JSONArray couponThemePubPorts) {
        String orgLevelName = MktUseRuleTypeEnum.STORE.getOrgLevelName();

        // 店铺编码不在权限内
        if (!authStoreMap.containsKey(codeStr)) {
            // 判断店铺编码 无权限还是不存在
            return orgCodeNotExistOrNotAuth(orgLevelName, line, codeStr, noAuthButExistOrgCodes);
        }
        // 判断是否在对应端下线了
        else {
            AuthStoreDTO authStoreDTO = authStoreMap.get(codeStr);
            validateStoreOnline(couponThemePubPorts, authStoreDTO);
        }

        Set<String> authStoreNameSet = authStoreMap.values().stream().map(AuthStoreDTO::getStoreName).collect(Collectors.toSet());
        if (!authStoreNameSet.contains(nameStr)) {
            // 判断店铺名称 无权限还是不存在
            return orgNameNotExistOrNotAuth(orgLevelName, line, nameStr, noAuthButExistOrgNameSet);
        }

        if (!StringUtils.equals(authStoreMap.get(codeStr).getStoreName().trim(), nameStr)) {
            MultiValueMap<String, Integer> multiValueMap = new LinkedMultiValueMap<>();
            multiValueMap.add(String.format(OrgImportTemplateConstant.CODE_NAME_NOT_MATCH, orgLevelName), line);
            return multiValueMap;
        }
        return null;
    }

    private MultiValueMap<String, Integer> validateMerchantNameAndCodeInDb(int line, String codeStr, String nameStr, Map<String, AuthMerchantDTO> authOrgCodeMap, Set<String> noAuthButExistOrgCodes, Set<String> noAuthButExistOrgNameSet) {
        String orgLevelName = MktUseRuleTypeEnum.MERCHANT.getOrgLevelName();
        if (!authOrgCodeMap.containsKey(codeStr)) {
            // 判断商家编码 无权限还是不存在
            return orgCodeNotExistOrNotAuth(orgLevelName, line, codeStr, noAuthButExistOrgCodes);
        }

        Set<String> authMerchantNameSet = authOrgCodeMap.values().stream().map(AuthMerchantDTO::getMerchantName).collect(Collectors.toSet());
        if (!authMerchantNameSet.contains(nameStr)) {
            // 判断商家名称 无权限还是不存在
            return orgNameNotExistOrNotAuth(orgLevelName, line, nameStr, noAuthButExistOrgNameSet);
        }

        if (!StringUtils.equals(authOrgCodeMap.get(codeStr).getMerchantName().trim(), nameStr)) {
            MultiValueMap<String, Integer> multiValueMap = new LinkedMultiValueMap<>();
            multiValueMap.add(String.format(OrgImportTemplateConstant.CODE_NAME_NOT_MATCH, orgLevelName), line);

            return multiValueMap;
        }
        return null;
    }

    private AddOrgAo prepareAddOrgAoBean(Long orgId, String orgCode, String orgName) {
        AddOrgAo addOrgAo = new AddOrgAo();
        addOrgAo.setOrgId(orgId);
        addOrgAo.setOrgCode(orgCode);
        addOrgAo.setOrgName(orgName);
        return addOrgAo;
    }

    private void fillNoAuthOrgData(Set<String> fistRowDataSet, Set<String> noAuthButExistOrgCodeSet, Set<String> noAuthButExistOrgNameSet, String orgLevelCode) {
        List<String> queryList = new ArrayList<>(fistRowDataSet);
        int totalPage = (queryList.size() + ORG_QUERY_PAGE_SIZE - 1) / ORG_QUERY_PAGE_SIZE;
        for (int i = 0; i < totalPage; i++) {
            List<String> subList = queryList.subList(i * ORG_QUERY_PAGE_SIZE, Math.min((i + 1) * ORG_QUERY_PAGE_SIZE, queryList.size()));
            OrgInfoByPluralismInDto orgReq = new OrgInfoByPluralismInDto();
            orgReq.setOrgLevelCode(orgLevelCode);
            orgReq.setBuildCodes(subList);
            List<OrgInfoByPluralismOutDto> orgList = getExistOrgList(orgReq);
            // 装载没有权限但是真实存在的，noAuthButExistOrgNameSet
            if (CollectionUtils.isNotEmpty(orgList)) {
                for (OrgInfoByPluralismOutDto item : orgList) {
                    // 装载没有权限但是真实存在的
                    noAuthButExistOrgNameSet.add(item.getOrgName());
                    if (MktUseRuleTypeEnum.STORE.getOrgLevelCode().equals(orgLevelCode)) {
                        noAuthButExistOrgCodeSet.add(item.getBuildCode());
                    } else {
                        noAuthButExistOrgCodeSet.add(item.getOrgCode());
                    }
                }
            }
        }
    }

    private MultiValueMap<String, Integer> orgNameNotExistOrNotAuth(String orgLevelName, int line, String nameStr, Set<String> noAuthButExistOrgNameSet) {
        MultiValueMap<String, Integer> multiValueMap = new LinkedMultiValueMap<>();
        if (noAuthButExistOrgNameSet.contains(nameStr)) {
            multiValueMap.add(String.format(OrgImportTemplateConstant.NAME_NO_AUTH, orgLevelName), line);
        } else {
            multiValueMap.add(String.format(OrgImportTemplateConstant.NAME_NOT_EXIST, orgLevelName), line);
        }
        return multiValueMap;
    }

    private MultiValueMap<String, Integer> orgCodeNotExistOrNotAuth(String orgLevelName, int line, String codeStr, Set<String> noAuthButExistOrgCodes) {
        MultiValueMap<String, Integer> multiValueMap = new LinkedMultiValueMap<>();
        if (noAuthButExistOrgCodes.contains(codeStr)) {
            multiValueMap.add(String.format(OrgImportTemplateConstant.CODE_NO_AUTH, orgLevelName), line);
        } else {
            multiValueMap.add(String.format(OrgImportTemplateConstant.CODE_NOT_EXIST, orgLevelName), line);
        }

        return multiValueMap;
    }

    private List<OrgInfoByPluralismOutDto> getExistOrgList(OrgInfoByPluralismInDto query) {
        InputDto<OrgInfoByPluralismInDto> inputDto = new InputDto<>();
        inputDto.setData(query);

        OutputDto<List<OrgInfoByPluralismOutDto>> out = ouserWebFeignClient.getOrgInfoListByPluralism(inputDto);

        if (Objects.isNull(out)) {
            return null;
        } else {
            return out.getData();
        }
    }

    private void fillMoreMessageToAddOrgList(List<AddOrgAo> orgAddList, Map<Long, AuthMerchantDTO> authMerchantMap, String orgName) {
        orgAddList.forEach(item -> {
            if (authMerchantMap.containsKey(item.getOrgId())) {
                // 填充商家或集团名称
                item.setOrgName(authMerchantMap.get(item.getOrgId()).getMerchantName());
                // 填充商家或集团代码
                item.setOrgCode(authMerchantMap.get(item.getOrgId()).getMerchantCode());
            } else {
                throw new BusinessException(MktUseRuleErrorCode.ORG_NOT_EXIST.getCode(), String.format(MktUseRuleErrorCode.ORG_NOT_EXIST.getMessage(), orgName));
            }

        });
    }

    private void refreshAuthorOrgCache(String oprUserToken) {
        String url = middleendUrl + RestConstant.REFRESH_AUTHORITY + "?ut=" + oprUserToken;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> map = new HashMap<>();
        HttpEntity<Map<String, String>> request = new HttpEntity<>(map, headers);
        log.info("刷新用户组织缓存，start. url={}", url);
        long time = System.currentTimeMillis();
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, request, String.class);
        String body = responseEntity.getBody();
        time = System.currentTimeMillis() - time;
        log.info("刷新用户组织缓存，end: 耗时{}秒 body={}", time / 1000, body);
    }

    private void validateStoreOnline(JSONArray themeDbPubPorts, AuthStoreDTO authStoreDto) {
        // 券活动配置了机构适用人群，不在机构端上线，抛出
        if (themeDbPubPorts.contains(UserTypeEnum.SAAS.getUserType()) && !Objects.equals(authStoreDto.getOrgPointBuildOnlineStatus(), 1)) {
            throw new BusinessException(MktUseRuleErrorCode.STORE_OFFLINE);
        }

        // 券活动配置了会员适用人群，不在会员端上线，抛出
        if (themeDbPubPorts.contains(UserTypeEnum.B.getUserType()) && !Objects.equals(authStoreDto.getBuildOnlineStatus(), 1)) {
            throw new BusinessException(MktUseRuleErrorCode.STORE_OFFLINE);
        }

        // 券活动配置了C端适用人群，不在C端上线，抛出
        if (themeDbPubPorts.contains(UserTypeEnum.C.getUserType()) && !Objects.equals(authStoreDto.getCpointBuildOnlineStatus(), 1)) {
            throw new BusinessException(MktUseRuleErrorCode.STORE_OFFLINE);
        }
    }

    private JSONArray getCouponThemePubPorts(String crowdScopeIds) {
        JSONObject jsonObject = JSON.parseObject(crowdScopeIds);
        return jsonObject.getJSONArray("ids");
    }

    private Map<String, Integer> doAddOrg(CouponThemeAddOrgBo bo, CouponThemeEntity couponThemeDb) {

        Map<String, Integer> returnMap = new HashMap<>();
        returnMap.put(CouponConstant.SUCCESS_MESSAGE, 0);

        List<AddOrgAo> orgAddList = bo.getOrgAddList();
        // 反转
        Collections.reverse(orgAddList);
        // 获取已经存在的组织
        Set<Long> existedOrgSet = getExistOrgSet(bo.getCouponThemeId(), bo.getRuleType());

        List<MktUseRuleEntity> insertBeans = new ArrayList<>();
        orgAddList.forEach(bean -> {
            if (OrgIsAdded(existedOrgSet, bean.getOrgId())) {
                Integer cnt = returnMap.getOrDefault(CouponConstant.FAIL_MESSAGE, 0);
                cnt++;
                returnMap.put(CouponConstant.FAIL_MESSAGE, cnt);
                return;
            }

            MktUseRuleEntity insertBean = prepareMktUseRuleInsertBean(bo, bean);
            insertBeans.add(insertBean);

            Integer cnt = returnMap.get(CouponConstant.SUCCESS_MESSAGE);
            cnt++;
            returnMap.put(CouponConstant.SUCCESS_MESSAGE, cnt);
        });

        if (CollectionUtil.isEmpty(insertBeans)) {
            return returnMap;
        }

        // 操作mkt_use_rule表相关数据
        mktUseRuleService.addOrgRelatedDataWithTx(insertBeans);

        // insertBeans is not empty && 不是待提交 就记日志
        if (CollectionUtil.isNotEmpty(insertBeans) && !CouponThemeStatus.CREATE.getStatus().equals(couponThemeDb.getStatus())) {
            StringBuilder oprContent = new StringBuilder();
            oprContent.append("新增").append(insertBeans.size()).append("个")
                    .append(MktUseRuleTypeEnum.getOrgLevelNameByType(bo.getRuleType())).append(": [");
            insertBeans.forEach(bean -> {
                oprContent.append(bean.getRefDescription()).append("、");
            });
            oprContent.deleteCharAt(oprContent.length() - 1);
            oprContent.append("]");

            // 异步日志
            OprLogDo oprLogDo = OprLogDo.builder()
                    .oprUserId(bo.getUserId())
                    .oprUserName(bo.getUsername())
                    .oprContent(oprContent.toString())
                    .refId(bo.getCouponThemeId())
                    .oprThemeType(LogOprThemeType.COUPON_THEME)
                    .oprType(LogOprType.UPDATE_THEME_AFTER_CHECK)
                    .build();
            couponOprLogService.saveOprLogAsync(oprLogDo);
        }
        return returnMap;
    }

    private MktUseRuleEntity prepareMktUseRuleInsertBean(CouponThemeAddOrgBo bo, AddOrgAo bean) {
        MktUseRuleEntity entity = new MktUseRuleEntity();
        entity.setThemeRef(bo.getCouponThemeId());
        entity.setRuleType(bo.getRuleType());

        entity.setLimitRef(bean.getOrgId());
        entity.setRefDescription(bean.getOrgName());
        entity.setExtendRef(bean.getOrgCode());

        entity.setCreateUserid(bo.getUserId());
        entity.setCreateUsername(bo.getUsername());
        entity.setIsDeleted(CouponConstant.NO);

        return entity;
    }

    private boolean OrgIsAdded(Set<Long> existedOrgSet, Long inputOrgId) {
        return existedOrgSet != null && existedOrgSet.contains(inputOrgId);
    }

    private Set<Long> getExistOrgSet(Long couponThemeId, Integer ruleType) {
        LambdaQueryWrapper<MktUseRuleEntity> queryWrapper = Wrappers.lambdaQuery(MktUseRuleEntity.class);
        queryWrapper.select(MktUseRuleEntity::getLimitRef)
                .eq(MktUseRuleEntity::getThemeRef, couponThemeId)
                .eq(MktUseRuleEntity::getRuleType, ruleType);

        List<MktUseRuleEntity> mktUseRuleEntities = baseMapper.selectList(queryWrapper);
        if (CollectionUtil.isNotEmpty(mktUseRuleEntities)) {
            return mktUseRuleEntities.stream().map(MktUseRuleEntity::getLimitRef).collect(Collectors.toSet());
        }
        return null;
    }

    private CouponThemeEntity getCouponThemeDb(Long couponThemeId) {
        CouponThemeEntity entity = couponThemeService.getById(couponThemeId);
        if (Objects.isNull(entity)) {
            throw new BusinessException(CouponThemeErrorCode.COUPON_THEME_NOT_EXIST);
        }

        return entity;
    }

    @Override
    public Map<Integer, List<Long>> getMktUseRuleMap(MktUseRuleCouponThemeDetailBo bo) {
        LambdaQueryWrapper<MktUseRuleEntity> queryWrapper = Wrappers.lambdaQuery(MktUseRuleEntity.class);
        queryWrapper.eq(MktUseRuleEntity::getThemeRef, bo.getThemeRef());

        List<MktUseRuleEntity> mktUseRuleEntityList = baseMapper.selectList(queryWrapper);
        //封装查询结果
        HashMap<Integer, List<Long>> ruleTypeMap = new HashMap<>();
        if (CollectionUtil.isEmpty(mktUseRuleEntityList)) {
            return ruleTypeMap;
        }

        for (MktUseRuleEntity mktUseRuleEntity : mktUseRuleEntityList) {
            if (ruleTypeMap.containsKey(mktUseRuleEntity.getRuleType())) {
                ruleTypeMap.get(mktUseRuleEntity.getRuleType()).add(mktUseRuleEntity.getLimitRef());
            } else {
                List<Long> list = new ArrayList();
                list.add(mktUseRuleEntity.getLimitRef());
                ruleTypeMap.put(mktUseRuleEntity.getRuleType(), list);
            }
        }

        return ruleTypeMap;
    }

    @Override
    public PageResponse<MktUseRuleOrgListResponse> listMktUseRule(MktUseRuleOrgListBo bo) {
        //拿当前用户的组织权限
        MerchantInfo merchantInfo = midPlatformLoginHelper.getMerchantInfoByUserId(bo.getUserId().toString());
        StoreInfo storeInfo = midPlatformLoginHelper.getStoreInfoByUserId(bo.getUserId().toString());
        //查的是店铺信息
        Map<Long, Set<Long>> authStoreMap = storeInfo.getAuthStoreList().stream().filter(item -> item.getMerchantId() != null).collect(Collectors.groupingBy(AuthStoreDTO::getMerchantId, Collectors.mapping(AuthStoreDTO::getStoreId, Collectors.toSet())));
        //查的商家信息
        Map<Long, Set<Long>> authMerchantMap = merchantInfo.getAuthMerchantList().stream().filter(item -> {
            // 只拿出商家，过滤掉是集团的 并且过滤掉没有挂载集团的商家
            return (StringUtils.equals(item.getOrgLevelCode(), "FGS") || Objects.equals(item.getLevel(), 2)) && Objects.nonNull(item.getParentId());
        }).collect(Collectors.groupingBy(AuthMerchantDTO::getParentId, Collectors.mapping(AuthMerchantDTO::getMerchantId, Collectors.toSet())));


        MktUseRuleOrgListDto dto = initMktUseRuleOrgListDto(bo, merchantInfo, storeInfo, authMerchantMap, authStoreMap);
        int total = baseMapper.listMktUseRuleCount(dto);
        PageResponse pageResponse = new PageResponse();
        List<MktUseRuleOrgListResponse> listObjs = new ArrayList<>();
        pageResponse.setListObj(listObjs);
        pageResponse.setTotal(total);

        if (total == 0) {
            return pageResponse;
        }

        List<MktUseRuleEntity> mktUseRuleEntityList = baseMapper.listMktUseRule(dto);
        if (CollectionUtils.isEmpty(mktUseRuleEntityList)) {
            return pageResponse;
        }

        Integer ruleType = mktUseRuleEntityList.get(0).getRuleType();
        Map<Long, AuthStoreDTO> authStoreInfoMap = new HashMap<>();
        Map<Long, AuthMerchantDTO> merchantInfoMap = new HashMap<>();
        if (Objects.equals(MktUseRuleTypeEnum.STORE.getType(), ruleType)) {
            authStoreInfoMap = storeInfo.getAuthStoreList().stream().collect(Collectors.toMap(AuthStoreDTO::getStoreId, Function.identity(), (f, l) -> f));
        }
        if (Objects.equals(MktUseRuleTypeEnum.STORE.getType(), ruleType) || Objects.equals(MktUseRuleTypeEnum.MERCHANT.getType(), ruleType)) {
            merchantInfoMap = merchantInfo.getAuthMerchantList().stream().filter(item -> StringUtils.equals(item.getOrgLevelCode(), "FGS") || Objects.equals(item.getLevel(), 2)).collect(Collectors.toMap(AuthMerchantDTO::getMerchantId, Function.identity(), (f, l) -> f));
        }

        for (MktUseRuleEntity entity : mktUseRuleEntityList) {
            MktUseRuleOrgListResponse mktUseRuleOrgListResponse = new MktUseRuleOrgListResponse();
            mktUseRuleOrgListResponse.setId(entity.getId());
            mktUseRuleOrgListResponse.setKeyId(entity.getId());
            mktUseRuleOrgListResponse.setName(entity.getRefDescription());
            mktUseRuleOrgListResponse.setRegionName(entity.getExtendRef());
            mktUseRuleOrgListResponse.setBuildCode(entity.getExtendRef());
            mktUseRuleOrgListResponse.setMerchantType(entity.getRuleType());
            if (Objects.equals(MktUseRuleTypeEnum.STORE.getType(), ruleType)) {
                AuthStoreDTO authStoreDTO = authStoreInfoMap.get(entity.getLimitRef());
                AuthMerchantDTO authMerchantInfo = merchantInfoMap.get(authStoreDTO.getMerchantId());
                if (Objects.nonNull(authMerchantInfo)) {
                    mktUseRuleOrgListResponse.setOwnedMerchantName(authStoreDTO.getMerchantName());
                    mktUseRuleOrgListResponse.setOwnedGroupName(authMerchantInfo.getParentName());
                }
            }

            if (Objects.equals(MktUseRuleTypeEnum.MERCHANT.getType(), ruleType)) {
                AuthMerchantDTO authMerchantInfo = merchantInfoMap.get(entity.getLimitRef());
                if (null != authMerchantInfo && null != authMerchantInfo.getParentName()) {
                    mktUseRuleOrgListResponse.setOwnedGroupName(authMerchantInfo.getParentName());
                }
            }

            listObjs.add(mktUseRuleOrgListResponse);
        }

        return pageResponse;
    }

    private MktUseRuleOrgListDto initMktUseRuleOrgListDto(MktUseRuleOrgListBo bo, MerchantInfo merchantInfo, StoreInfo storeInfo, Map<Long, Set<Long>> authMerchantMap, Map<Long, Set<Long>> authStoreMap) {
        MktUseRuleOrgListDto dto = new MktUseRuleOrgListDto();

        dto.setRefType(bo.getRefType());
        dto.setGroupId(bo.getGroupId());
        dto.setMerchantId(bo.getMerchantId());
        dto.setBuildCode(bo.getBuildCode());
        dto.setMerchantCode(bo.getMerchantCode());
        dto.setMerchantName(bo.getMerchantName());
        dto.setStartItem(bo.getStartItem());
        dto.setItemsPerPage(bo.getItemsPerPage());
        dto.setRuleTypeList(convertRuleTypeList(bo));
        dto.setThemeRef(bo.getThemeRef());

        initMerchantIdsByGroupIdIsNotNull(bo, dto, merchantInfo, storeInfo);
        initMerchantIdsByGroupIdIsNull(bo, dto, merchantInfo, storeInfo);
        initStoreIdsByBelongsToMerchantIdsIsNotNull(bo, dto, authStoreMap);
        initStoreIdsByBelongsToGroupIdsIsNotNull(bo, dto, authMerchantMap, authStoreMap);

        return dto;
    }

    private List<Integer> convertRuleTypeList(MktUseRuleOrgListBo bo) {
        switch (MktUseRuleInputType.of(bo.getMerchantType())) {
            case GROUP: {
                return Collections.singletonList(MktUseRuleTypeEnum.GROUP.getType());
            }
            case MERCHANT: {
                return Collections.singletonList(MktUseRuleTypeEnum.MERCHANT.getType());
            }
            case STORE: {
                return Collections.singletonList(MktUseRuleTypeEnum.STORE.getType());
            }
            default: {
                return Arrays.asList(MktUseRuleTypeEnum.GROUP.getType(), MktUseRuleTypeEnum.MERCHANT.getType(), MktUseRuleTypeEnum.STORE.getType());
            }
        }

    }

    private void initStoreIdsByBelongsToGroupIdsIsNotNull(MktUseRuleOrgListBo bo, MktUseRuleOrgListDto dto, Map<Long, Set<Long>> authMerchantMap, Map<Long, Set<Long>> authStoreMap) {
        if (CollectionUtils.isEmpty(bo.getBelongsToGroupIds())) {
            return;
        }

        Set<Long> selectedStoreIds = new HashSet<>();
        Set<Long> selectedMerchantIds = new HashSet<>();
        //遍历所属集团
        for (Long groupId : bo.getBelongsToGroupIds()) {
            if (authMerchantMap.containsKey(groupId)) {
                //装载所属集团下的商家ids
                selectedMerchantIds.addAll(authMerchantMap.get(groupId));
            }
        }

        if (CollectionUtils.isEmpty(selectedMerchantIds)) {
            selectedMerchantIds.add(-1L);
        }

        // 查的是商家信息
        if (Objects.equals(bo.getMerchantType(), MktUseRuleInputType.MERCHANT.getType())) {
            selectedMerchantIds.add(-1L);
            selectedMerchantIds.add(bo.getMerchantId());
            dto.setMerchantIds(new ArrayList<>(selectedMerchantIds));
            return;
        }

        // 查的是店铺信息
        if (Objects.equals(bo.getMerchantType(), MktUseRuleInputType.STORE.getType())) {
            Set<Long> merchantToStoreIds = new HashSet<>();
            for (Long merchantId : selectedMerchantIds) {
                if (authStoreMap.containsKey(merchantId)) {
                    merchantToStoreIds.addAll(authStoreMap.get(merchantId));
                }
            }

            if (selectedMerchantIds.contains(-1L) && merchantToStoreIds.isEmpty()) {
                merchantToStoreIds.add(-1L);
            }

            if (CollectionUtils.isNotEmpty(merchantToStoreIds)) {
                if (CollectionUtils.isNotEmpty(selectedStoreIds)) {
                    merchantToStoreIds.retainAll(selectedStoreIds);
                }

                if (CollectionUtils.isEmpty(merchantToStoreIds)) {
                    merchantToStoreIds.add(-1L);
                }
                dto.setStoreIds(new ArrayList<>(merchantToStoreIds));
                // setStoreIds已在权限范围内，大的权限就不需要了
                return;
            }

            selectedStoreIds.add(-1L);
            dto.setStoreIds(new ArrayList<>(selectedStoreIds));
            // setStoreIds已在权限范围内，大的权限就不需要了
            dto.setMerchantIds(null);
        }
    }

    private void initStoreIdsByBelongsToMerchantIdsIsNotNull(MktUseRuleOrgListBo bo, MktUseRuleOrgListDto dto, Map<Long, Set<Long>> authStoreMap) {
        if (CollectionUtils.isEmpty(bo.getBelongsToMerchantIds()) || !Objects.equals(bo.getMerchantType(), MktUseRuleInputType.STORE.getType())) {
            return;
        }

        Set<Long> selectedStoreIds = new HashSet<>();

        // 查的是店铺信息
        for (Long id : bo.getBelongsToMerchantIds()) {
            if (authStoreMap.containsKey(id)) {
                selectedStoreIds.addAll(authStoreMap.get(id));
            }
        }
        selectedStoreIds.add(-1L);
        selectedStoreIds.add(bo.getMerchantId());
        dto.setStoreIds(new ArrayList<>(selectedStoreIds));
        // setStoreIds已在权限范围内，大的权限就不需要了
        dto.setMerchantIds(null);
    }

    private void initMerchantIdsByGroupIdIsNull(MktUseRuleOrgListBo bo, MktUseRuleOrgListDto dto, MerchantInfo merchantInfo, StoreInfo storeInfo) {
        if (Objects.nonNull(bo.getGroupId())) {
            return;
        }

        if (Objects.equals(bo.getMerchantType(), MktUseRuleInputType.MERCHANT.getType())) {
            List<Long> merchantIds = merchantInfo.getAuthMerchantList().stream().filter(item -> StringUtils.equals(item.getOrgLevelCode().trim(), "FGS") || Objects.equals(item.getLevel(), 2)).map(AuthMerchantDTO::getMerchantId).distinct().collect(Collectors.toList());
            if (CollectionUtils.isEmpty(merchantIds)) {
                dto.setMerchantIds(Collections.singletonList(-1L));
            } else {
                dto.setMerchantIds(merchantIds);
            }
        }

        if (Objects.equals(bo.getMerchantType(), MktUseRuleInputType.GROUP.getType())) {
            List<Long> groupIds = merchantInfo.getAuthMerchantList().stream().filter(item -> StringUtils.equals(item.getOrgLevelCode().trim(), "ZB") || Objects.equals(item.getLevel(), 1)).map(AuthMerchantDTO::getMerchantId).distinct().collect(Collectors.toList());
            if (CollectionUtils.isEmpty(groupIds)) {
                dto.setMerchantIds(Collections.singletonList(-1L));
            } else {
                dto.setMerchantIds(groupIds);
            }
        }

        if (Objects.equals(bo.getMerchantType(), MktUseRuleInputType.STORE.getType())) {
            // 取出当前登录用户权限店铺列表
            List<Long> authStoreIds = storeInfo.getAuthStoreList().stream().map(AuthStoreDTO::getStoreId).distinct().collect(Collectors.toList());
            if (CollectionUtils.isEmpty(authStoreIds)) {
                dto.setMerchantIds(Collections.singletonList(-1L));
            } else {
                dto.setMerchantIds(authStoreIds);
            }
        }
    }

    private void initMerchantIdsByGroupIdIsNotNull(MktUseRuleOrgListBo bo, MktUseRuleOrgListDto dto, MerchantInfo merchantInfo, StoreInfo storeInfo) {
        if (Objects.isNull(bo.getGroupId())) {
            return;
        }

        if (Objects.equals(bo.getMerchantType(), MktUseRuleInputType.MERCHANT.getType())) {
            //房商城-查询商家时增加集团筛选条件
            //商家id
            List<Long> merchantIds = bo.getMerchantIds();
            List<Long> newMerchantIds = new ArrayList<>();
            List<Long> authChildMerchantIds = merchantInfo.getAuthMerchantList().stream()
                    .filter(authMerchantDTO -> Objects.equals(authMerchantDTO.getParentId(), bo.getGroupId()))
                    .map(AuthMerchantDTO::getMerchantId)
                    .collect(Collectors.toList());

            for (Long merchantId : merchantIds) {
                if (authChildMerchantIds.contains(merchantId)) {
                    newMerchantIds.add(merchantId);
                }
            }
            newMerchantIds.add(-1L);
            dto.setMerchantIds(newMerchantIds);
        }

        if (Objects.equals(bo.getMerchantType(), MktUseRuleInputType.STORE.getType())) {
            // 房商城-查询店铺时增加集团筛选条件
            // 如果商家id为空，则查询条件为该集团底下的所有商家\
            //店铺id
            List<Long> storeIds = bo.getMerchantIds();
            List<Long> newStoreIds = new ArrayList<>();
            List<Long> authChildMerchantIds = merchantInfo.getAuthMerchantList().stream()
                    .filter(authMerchantDTO -> Objects.equals(authMerchantDTO.getParentId(), bo.getGroupId()))
                    .map(AuthMerchantDTO::getMerchantId)
                    .collect(Collectors.toList());

            authChildMerchantIds.add(bo.getMerchantId());
            dto.setMerchantId(null);

            if (CollectionUtils.isNotEmpty(authChildMerchantIds)) {
                List<Long> authChildStoreIds = storeInfo.getAuthStoreList().stream()
                        .filter(authStoreDTO -> authChildMerchantIds.contains(authStoreDTO.getMerchantId()))
                        .map(AuthStoreDTO::getStoreId)
                        .collect(Collectors.toList());
                for (Long storeId : storeIds) {
                    if (authChildStoreIds.contains(storeId)) {
                        newStoreIds.add(storeId);
                    }
                }
                newStoreIds.add(-1L);
                dto.setMerchantIds(newStoreIds);
            } else {
                dto.setMerchantIds(Arrays.asList(-1L));
            }
        }
    }

    @Override
    public PageResponse<MktUseRuleByIdsResponse> getMktUseRuleByIds(MktUseRuleByIdsBo bo) {
        //拿当前用户的组织权限
        MerchantInfo merchantInfo = midPlatformLoginHelper.getMerchantInfoByUserId(bo.getUserId().toString());
        StoreInfo storeInfo = midPlatformLoginHelper.getStoreInfoByUserId(bo.getUserId().toString());
        //查的是店铺信息
        Map<Long, Set<Long>> authStoreMap = storeInfo.getAuthStoreList().stream().filter(item -> item.getMerchantId() != null).collect(Collectors.groupingBy(AuthStoreDTO::getMerchantId, Collectors.mapping(AuthStoreDTO::getStoreId, Collectors.toSet())));
        //查的商家信息
        Map<Long, Set<Long>> authMerchantMap = merchantInfo.getAuthMerchantList().stream().filter(item -> {
            // 只拿出商家，过滤掉是集团的 并且过滤掉没有挂载集团的商家
            return (StringUtils.equals(item.getOrgLevelCode(), "FGS") || Objects.equals(item.getLevel(), 2)) && Objects.nonNull(item.getParentId());
        }).collect(Collectors.groupingBy(AuthMerchantDTO::getParentId, Collectors.mapping(AuthMerchantDTO::getMerchantId, Collectors.toSet())));

        MktUseRuleByIdsDto dto = new MktUseRuleByIdsDto();
        initMktUseRuleByIdsDto(bo, dto, merchantInfo, storeInfo, authMerchantMap, authStoreMap);

        int total = baseMapper.getMktUseRuleByIdsCount(dto);
        PageResponse pageResponse = new PageResponse();
        List<MktUseRuleByIdsResponse> listObjs = new ArrayList<>();
        pageResponse.setListObj(listObjs);
        pageResponse.setTotal(total);

        if (total == 0) {
            return pageResponse;
        }

        List<MktUseRuleEntity> mktUseRuleEntityList = baseMapper.getMktUseRuleByIds(dto);
        if (CollectionUtils.isNotEmpty(mktUseRuleEntityList)) {
            return pageResponse;
        }

        Integer ruleType = mktUseRuleEntityList.get(0).getRuleType();
        Map<Long, AuthStoreDTO> authStoreInfoMap = new HashMap<>();
        Map<Long, AuthMerchantDTO> merchantInfoMap = new HashMap<>();
        if (Objects.equals(MktUseRuleTypeEnum.STORE.getType(), ruleType)) {
            authStoreInfoMap = storeInfo.getAuthStoreList().stream().collect(Collectors.toMap(AuthStoreDTO::getStoreId, Function.identity(), (f, l) -> f));
        }
        if (Objects.equals(MktUseRuleTypeEnum.STORE.getType(), ruleType) || Objects.equals(MktUseRuleTypeEnum.MERCHANT.getType(), ruleType)) {
            merchantInfoMap = merchantInfo.getAuthMerchantList().stream().filter(item -> StringUtils.equals(item.getOrgLevelCode(), "FGS") || Objects.equals(item.getLevel(), 2)).collect(Collectors.toMap(AuthMerchantDTO::getMerchantId, Function.identity(), (f, l) -> f));
        }

        for (MktUseRuleEntity entity : mktUseRuleEntityList) {
            MktUseRuleByIdsResponse mktUseRuleByIdsResponse = new MktUseRuleByIdsResponse();
            mktUseRuleByIdsResponse.setId(entity.getId());
            mktUseRuleByIdsResponse.setKeyId(entity.getId());
            mktUseRuleByIdsResponse.setName(entity.getRefDescription());
            mktUseRuleByIdsResponse.setRegionName(entity.getExtendRef());
            mktUseRuleByIdsResponse.setBuildCode(entity.getExtendRef());
            mktUseRuleByIdsResponse.setMerchantType(entity.getRuleType());
            if (Objects.equals(MktUseRuleTypeEnum.STORE.getType(), ruleType)) {
                AuthStoreDTO authStoreDTO = authStoreInfoMap.get(entity.getLimitRef());
                AuthMerchantDTO authMerchantInfo = merchantInfoMap.get(authStoreDTO.getMerchantId());
                if (Objects.nonNull(authMerchantInfo)) {
                    mktUseRuleByIdsResponse.setOwnedMerchantName(authStoreDTO.getMerchantName());
                    mktUseRuleByIdsResponse.setOwnedGroupName(authMerchantInfo.getParentName());
                }
            }

            if (Objects.equals(MktUseRuleTypeEnum.MERCHANT.getType(), ruleType)) {
                AuthMerchantDTO authMerchantInfo = merchantInfoMap.get(entity.getLimitRef());
                if (null != authMerchantInfo && null != authMerchantInfo.getParentName()) {
                    mktUseRuleByIdsResponse.setOwnedGroupName(authMerchantInfo.getParentName());
                }
            }

            listObjs.add(mktUseRuleByIdsResponse);
        }

        return pageResponse;
    }

    private void initMktUseRuleByIdsDto(MktUseRuleByIdsBo bo, MktUseRuleByIdsDto dto, MerchantInfo merchantInfo, StoreInfo storeInfo, Map<Long, Set<Long>> authMerchantMap, Map<Long, Set<Long>> authStoreMap) {
        dto.setRefType(bo.getRefType());
        dto.setGroupId(bo.getGroupId());
        dto.setMerchantId(bo.getMerchantId());
        dto.setBuildCode(bo.getBuildCode());
        dto.setMerchantCode(bo.getMerchantCode());
        dto.setMerchantName(bo.getMerchantName());
        dto.setStartItem(bo.getStartItem());
        dto.setItemsPerPage(bo.getItemsPerPage());
        dto.setRuleTypeList(convertRuleTypeByIds(bo));

        initMerchantIdsByGroupIdIsNotNullByIds(bo, dto, merchantInfo, storeInfo);
        initMerchantIdsByGroupIdIsNullByIds(bo, dto, merchantInfo, storeInfo);
        initStoreIdsByBelongsToMerchantIdsIsNotNullByIds(bo, dto, authStoreMap);
        initStoreIdsByBelongsToGroupIdsIsNotNullByIds(bo, dto, authMerchantMap, authStoreMap);

    }

    private List<Integer> convertRuleTypeByIds(MktUseRuleByIdsBo bo) {
        switch (MktUseRuleInputType.of(bo.getMerchantType())) {
            case GROUP: {
                return Collections.singletonList(MktUseRuleTypeEnum.GROUP.getType());
            }
            case MERCHANT: {
                return Collections.singletonList(MktUseRuleTypeEnum.MERCHANT.getType());
            }
            case STORE: {
                return Collections.singletonList(MktUseRuleTypeEnum.STORE.getType());
            }
            default: {
                return Arrays.asList(MktUseRuleTypeEnum.GROUP.getType(), MktUseRuleTypeEnum.MERCHANT.getType(), MktUseRuleTypeEnum.STORE.getType());
            }
        }

    }

    private void initStoreIdsByBelongsToGroupIdsIsNotNullByIds(MktUseRuleByIdsBo bo, MktUseRuleByIdsDto dto, Map<Long, Set<Long>> authMerchantMap, Map<Long, Set<Long>> authStoreMap) {
        if (CollectionUtils.isEmpty(bo.getBelongsToGroupIds())) {
            return;
        }

        Set<Long> selectedStoreIds = new HashSet<>();
        Set<Long> selectedMerchantIds = new HashSet<>();
        //遍历所属集团
        for (Long groupId : bo.getBelongsToGroupIds()) {
            if (authMerchantMap.containsKey(groupId)) {
                //装载所属集团下的商家ids
                selectedMerchantIds.addAll(authMerchantMap.get(groupId));
            }
        }

        if (CollectionUtils.isEmpty(selectedMerchantIds)) {
            selectedMerchantIds.add(-1L);
        }

        // 查的是商家信息
        if (Objects.equals(bo.getMerchantType(), MktUseRuleInputType.MERCHANT.getType())) {
            selectedMerchantIds.add(-1L);
            selectedMerchantIds.add(bo.getMerchantId());
            dto.setMerchantIds(new ArrayList<>(selectedMerchantIds));
            return;
        }

        // 查的是店铺信息
        if (Objects.equals(bo.getMerchantType(), MktUseRuleInputType.STORE.getType())) {
            Set<Long> merchantToStoreIds = new HashSet<>();
            for (Long merchantId : selectedMerchantIds) {
                if (authStoreMap.containsKey(merchantId)) {
                    merchantToStoreIds.addAll(authStoreMap.get(merchantId));
                }
            }

            if (selectedMerchantIds.contains(-1L) && merchantToStoreIds.isEmpty()) {
                merchantToStoreIds.add(-1L);
            }

            if (CollectionUtils.isNotEmpty(merchantToStoreIds)) {
                if (CollectionUtils.isNotEmpty(selectedStoreIds)) {
                    merchantToStoreIds.retainAll(selectedStoreIds);
                }

                if (CollectionUtils.isEmpty(merchantToStoreIds)) {
                    merchantToStoreIds.add(-1L);
                }
                dto.setStoreIds(new ArrayList<>(merchantToStoreIds));
                // setStoreIds已在权限范围内，大的权限就不需要了
                return;
            }

            selectedStoreIds.add(-1L);
            dto.setStoreIds(new ArrayList<>(selectedStoreIds));
            // setStoreIds已在权限范围内，大的权限就不需要了
            dto.setMerchantIds(null);
        }
    }

    private void initStoreIdsByBelongsToMerchantIdsIsNotNullByIds(MktUseRuleByIdsBo bo, MktUseRuleByIdsDto dto, Map<Long, Set<Long>> authStoreMap) {
        if (CollectionUtils.isEmpty(bo.getBelongsToMerchantIds()) || !Objects.equals(bo.getMerchantType(), MktUseRuleInputType.STORE.getType())) {
            return;
        }

        Set<Long> selectedStoreIds = new HashSet<>();

        // 查的是店铺信息
        for (Long id : bo.getBelongsToMerchantIds()) {
            if (authStoreMap.containsKey(id)) {
                selectedStoreIds.addAll(authStoreMap.get(id));
            }
        }
        selectedStoreIds.add(-1L);
        selectedStoreIds.add(bo.getMerchantId());
        dto.setStoreIds(new ArrayList<>(selectedStoreIds));
        // setStoreIds已在权限范围内，大的权限就不需要了
        dto.setMerchantIds(null);
    }

    private void initMerchantIdsByGroupIdIsNullByIds(MktUseRuleByIdsBo bo, MktUseRuleByIdsDto dto, MerchantInfo merchantInfo, StoreInfo storeInfo) {
        if (Objects.nonNull(bo.getGroupId())) {
            return;
        }

        if (Objects.equals(bo.getMerchantType(), MktUseRuleInputType.MERCHANT.getType())) {
            List<Long> merchantIds = merchantInfo.getAuthMerchantList().stream().filter(item -> StringUtils.equals(item.getOrgLevelCode().trim(), "FGS") || Objects.equals(item.getLevel(), 2)).map(AuthMerchantDTO::getMerchantId).distinct().collect(Collectors.toList());
            if (CollectionUtils.isEmpty(merchantIds)) {
                dto.setMerchantIds(Collections.singletonList(-1L));
            } else {
                dto.setMerchantIds(merchantIds);
            }
        }

        if (Objects.equals(bo.getMerchantType(), MktUseRuleInputType.GROUP.getType())) {
            List<Long> groupIds = merchantInfo.getAuthMerchantList().stream().filter(item -> StringUtils.equals(item.getOrgLevelCode().trim(), "ZB") || Objects.equals(item.getLevel(), 1)).map(AuthMerchantDTO::getMerchantId).distinct().collect(Collectors.toList());
            if (CollectionUtils.isEmpty(groupIds)) {
                dto.setMerchantIds(Collections.singletonList(-1L));
            } else {
                dto.setMerchantIds(groupIds);
            }
        }

        if (Objects.equals(bo.getMerchantType(), MktUseRuleInputType.STORE.getType())) {
            // 取出当前登录用户权限店铺列表
            List<Long> authStoreIds = storeInfo.getAuthStoreList().stream().map(AuthStoreDTO::getStoreId).distinct().collect(Collectors.toList());
            if (CollectionUtils.isEmpty(authStoreIds)) {
                dto.setMerchantIds(Collections.singletonList(-1L));
            } else {
                dto.setMerchantIds(authStoreIds);
            }
        }
    }

    private void initMerchantIdsByGroupIdIsNotNullByIds(MktUseRuleByIdsBo bo, MktUseRuleByIdsDto dto, MerchantInfo merchantInfo, StoreInfo storeInfo) {
        if (Objects.isNull(bo.getGroupId())) {
            return;
        }

        if (Objects.equals(bo.getMerchantType(), MktUseRuleInputType.MERCHANT.getType())) {
            //房商城-查询商家时增加集团筛选条件
            //商家id
            List<Long> merchantIds = bo.getMerchantIds();
            List<Long> newMerchantIds = new ArrayList<>();
            List<Long> authChildMerchantIds = merchantInfo.getAuthMerchantList().stream()
                    .filter(authMerchantDTO -> Objects.equals(authMerchantDTO.getParentId(), bo.getGroupId()))
                    .map(AuthMerchantDTO::getMerchantId)
                    .collect(Collectors.toList());

            for (Long merchantId : merchantIds) {
                if (authChildMerchantIds.contains(merchantId)) {
                    newMerchantIds.add(merchantId);
                }
            }
            newMerchantIds.add(-1L);
            dto.setMerchantIds(newMerchantIds);
        }

        if (Objects.equals(bo.getMerchantType(), MktUseRuleInputType.STORE.getType())) {
            // 房商城-查询店铺时增加集团筛选条件
            // 如果商家id为空，则查询条件为该集团底下的所有商家\
            //店铺id
            List<Long> storeIds = bo.getMerchantIds();
            List<Long> newStoreIds = new ArrayList<>();
            List<Long> authChildMerchantIds = merchantInfo.getAuthMerchantList().stream()
                    .filter(authMerchantDTO -> Objects.equals(authMerchantDTO.getParentId(), bo.getGroupId()))
                    .map(AuthMerchantDTO::getMerchantId)
                    .collect(Collectors.toList());

            authChildMerchantIds.add(bo.getMerchantId());
            dto.setMerchantId(null);

            if (CollectionUtils.isNotEmpty(authChildMerchantIds)) {
                List<Long> authChildStoreIds = storeInfo.getAuthStoreList().stream()
                        .filter(authStoreDTO -> authChildMerchantIds.contains(authStoreDTO.getMerchantId()))
                        .map(AuthStoreDTO::getStoreId)
                        .collect(Collectors.toList());
                for (Long storeId : storeIds) {
                    if (authChildStoreIds.contains(storeId)) {
                        newStoreIds.add(storeId);
                    }
                }
                newStoreIds.add(-1L);
                dto.setMerchantIds(newStoreIds);
            } else {
                dto.setMerchantIds(Arrays.asList(-1L));
            }
        }
    }

    @Override
    public PageResponse<MktUseRuleSelectionResponse> getSelectedSelectionList(MktUseRuleSelectionBo bo) {
        MktUseRuleSelectionDto dto = new MktUseRuleSelectionDto();

        dto.setExtendRef(bo.getExtendRef());
        dto.setLimitRef(bo.getLimitRef());
        dto.setThemeRef(bo.getThemeRef());
        dto.setRefDescription(bo.getRefDescription());

        dto.setRefType(bo.getRefType());
        dto.setRuleType(bo.getRuleType());
        dto.setExtendRef(bo.getExtendRef());
        dto.setStartItem(bo.getStartItem());
        dto.setItemsPerPage(bo.getItemsPerPage());

        int total = baseMapper.getSelectedSelectionListCount(dto);
        PageResponse pageResponse = new PageResponse();
        List<MktUseRuleSelectionResponse> listObjs = new ArrayList<>();
        pageResponse.setListObj(listObjs);
        pageResponse.setTotal(total);

        if (total == 0) {
            return pageResponse;
        }

        List<MktUseRuleEntity> mktUseRuleEntityList = baseMapper.getSelectedSelectionList(dto);
        if (CollectionUtils.isNotEmpty(mktUseRuleEntityList)) {
            return pageResponse;
        }

        mktUseRuleEntityList.stream().forEach(entity -> {
            MktUseRuleSelectionResponse mktUseRuleSelectionResponse = new MktUseRuleSelectionResponse();
            BeanUtil.copyProperties(entity, mktUseRuleSelectionResponse);
            mktUseRuleSelectionResponse.setMerchantName(entity.getRefDescription());
            listObjs.add(mktUseRuleSelectionResponse);
        });

        return pageResponse;
    }
}
