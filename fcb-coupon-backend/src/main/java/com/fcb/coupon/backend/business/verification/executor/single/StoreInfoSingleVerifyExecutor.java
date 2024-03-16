package com.fcb.coupon.backend.business.verification.executor.single;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONArray;
import com.fcb.coupon.backend.business.verification.context.SingleVerifyContext;
import com.fcb.coupon.backend.business.verification.context.VerifyServiceContext;
import com.fcb.coupon.backend.exception.CouponVerificationErrorCode;
import com.fcb.coupon.backend.model.dto.StoreInfoInputDto;
import com.fcb.coupon.backend.model.dto.StoreInfoOutDto;
import com.fcb.coupon.backend.model.dto.ValidateStoreInfoDto;
import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.exception.ResponseErrorCode;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * 楼盘校验相关执行器
 * @author YangHanBin
 * @date 2021-09-07 17:08
 */
public class StoreInfoSingleVerifyExecutor extends AbstractSingleVerifyExecutor {


    public StoreInfoSingleVerifyExecutor(SingleVerifyContext verifyContext, VerifyServiceContext serviceContext) {
        super(verifyContext, serviceContext);
    }

    @Override
    protected void before() {
        // 获取需要核销的店铺详情 包含了楼盘上下架信息
        StoreInfoOutDto storeInfo = getSingleVerifyStoreInfoByStoreId(getVerifyContext().getUsedStoreId());
        setVerifyStoreInfo(storeInfo);
    }

    @Override
    protected void doExecute() {
        // 校验--楼盘上下架和适用店铺
        validateStore4Single(getCouponThemeCache().getThemeType(), getDbCoupon().getUserType(), getCouponThemeApplicableUserTypes(getCouponThemeCache().getApplicableUserTypes()), getVerifyStoreInfo());
    }

    @Override
    protected void after() {
        WriteSingleVerifyExecutor delegate = new WriteSingleVerifyExecutor(getVerifyContext(), getServiceContext());
        delegate.setCouponEsDoc(getCouponEsDoc());
        delegate.setDbCoupon(getDbCoupon());
        delegate.setCouponThemeCache(getCouponThemeCache());
        delegate.setOffLineFlag(getOffLineFlag());
        delegate.setVerifyUnionId(getVerifyUnionId());
        delegate.setVerifyStoreInfo(getVerifyStoreInfo());
        delegate.execute();
    }

    private StoreInfoOutDto getSingleVerifyStoreInfoByStoreId(Long verifyStoreId) {
        StoreInfoInputDto storeInfoInputDto = new StoreInfoInputDto();
        storeInfoInputDto.setStoreIds(Collections.singletonList(verifyStoreId));
        List<StoreInfoOutDto> storeInfoList = queryVerifyStoreInfoBatch(storeInfoInputDto);
        if (CollectionUtils.isEmpty(storeInfoList)) {
            log.error("根据storeIds或buildCodes查询单个楼盘详情返回 null! verifyStoreId={}", verifyStoreId);
            throw new BusinessException(CouponVerificationErrorCode.STORE_NOT_EXIST);
        }
        return storeInfoList.get(0);
    }

    private void validateStore4Single(Integer themeType, Integer userType, JSONArray themeDbPubPorts, StoreInfoOutDto needToVerifyStoreInfo) {
        ValidateStoreInfoDto dto = new ValidateStoreInfoDto();
        BeanUtil.copyProperties(needToVerifyStoreInfo, dto);
        dto.setUserType(userType);
        dto.setCouponThemePubPorts(themeDbPubPorts);
        dto.setCouponThemeId(getDbCoupon().getCouponThemeId());
        dto.setStoreId(getVerifyContext().getUsedStoreId());
        dto.setThemeType(themeType);

        ResponseErrorCode errorCode = validateStore4Single(dto);
        if (errorCode != null) {
            throw new BusinessException(errorCode);
        }
    }
}
