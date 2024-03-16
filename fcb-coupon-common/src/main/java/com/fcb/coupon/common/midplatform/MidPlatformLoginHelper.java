package com.fcb.coupon.common.midplatform;

import com.fcb.coupon.common.exception.BusinessException;
import com.fcb.coupon.common.exception.CommonErrorCode;
import com.fcb.coupon.common.dto.FunctionInfo;
import com.fcb.coupon.common.dto.MerchantInfo;
import com.fcb.coupon.common.dto.StoreInfo;
import com.fcb.coupon.common.dto.UserInfo;
import com.fcb.coupon.common.util.HessianCodecUtil;
import com.fcb.coupon.common.util.OdyCacheKeyUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RBinaryStream;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author YangHanBin
 * @date 2021-06-11 10:40
 */
@Component
@Slf4j
@ConditionalOnClass(Redisson.class)
public class MidPlatformLoginHelper {
    @Autowired
    private RedissonClient redissonClient;

    public UserInfo getUserInfoByUt(String ut) {
        if (StringUtils.isBlank(ut)) {
            throw new BusinessException(CommonErrorCode.NO_LOGIN);
        }
        String userKey = OdyCacheKeyUtils.getUserKey(ut);
        RBinaryStream rBs = redissonClient.getBinaryStream(userKey);
        byte[] uBytes = rBs.get();
        UserInfo userInfo = HessianCodecUtil.decode(uBytes, UserInfo.class);
        if (Objects.isNull(userInfo)) {
            log.error("登录ut失效，ut={}", ut);
            throw new BusinessException(CommonErrorCode.UT_EXPIRED);
        }
        return  userInfo;
    }

    public MerchantInfo getMerchantInfoByUserId(String userId) {
        String merchantKey = OdyCacheKeyUtils.getUserMerchantCacheKey(userId);
        RBinaryStream rBs = redissonClient.getBinaryStream(merchantKey);
        byte[] uBytes = rBs.get();
        MerchantInfo merchantInfo = HessianCodecUtil.decode(uBytes, MerchantInfo.class);
        if (Objects.isNull(merchantInfo)) {
            log.error("根据userId获取MerchantInfo失败，userId={}", userId);
            throw new BusinessException(CommonErrorCode.GET_AUTHORITY_MERCHANT_FAIL);
        }
        return merchantInfo;
    }

    public StoreInfo getStoreInfoByUserId(String userId) {
        String storeKey = OdyCacheKeyUtils.getUserStoreCacheKey(userId);
        RBinaryStream rBs = redissonClient.getBinaryStream(storeKey);
        byte[] uBytes = rBs.get();
        StoreInfo storeInfo = HessianCodecUtil.decode(uBytes, StoreInfo.class);
        if (Objects.isNull(storeInfo)) {
            log.error("根据userId获取StoreInfo失败，userId={}", userId);
            throw new BusinessException(CommonErrorCode.GET_AUTHORITY_STORE_INFO_FAIL);
        }
        return storeInfo;
    }

    public FunctionInfo getFunctionInfoByUt(String ut) {
        if (StringUtils.isBlank(ut)) {
            throw new BusinessException(CommonErrorCode.NO_LOGIN);
        }
        String functionKey = OdyCacheKeyUtils.getFunctionKey(ut);
        RBinaryStream rBs = redissonClient.getBinaryStream(functionKey);
        byte[] uBytes = rBs.get();
        FunctionInfo functionInfo = HessianCodecUtil.decode(uBytes, FunctionInfo.class);
        if (Objects.isNull(functionInfo)) {
            log.error("根据ut获取FunctionInfo失败，ut={}", ut);
            throw new BusinessException(CommonErrorCode.NO_LOGIN);
        }

        return  functionInfo;
    }
}
