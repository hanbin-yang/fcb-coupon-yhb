package com.fcb.coupon.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fcb.coupon.backend.model.bo.*;
import com.fcb.coupon.backend.model.entity.MktUseRuleEntity;
import com.fcb.coupon.backend.model.param.response.MktUseRuleByIdsResponse;
import com.fcb.coupon.backend.model.param.response.MktUseRuleOrgListResponse;
import com.fcb.coupon.backend.model.param.response.MktUseRuleSelectionResponse;
import com.fcb.coupon.backend.model.param.response.PageResponse;

import java.util.List;
import java.util.Map;

/**
 * @author HanBin_Yang
 * @since 2021/6/21 10:39
 */
public interface MktUseRuleService extends IService<MktUseRuleEntity> {
    Map<String, Integer> addOrgBatch(CouponThemeAddOrgBo bo);

    void addOrgRelatedDataWithTx(List<MktUseRuleEntity> insertBeans);

    boolean removeOrgBatch(MktUseRuleDeleteOrgBo bo);

    void removeOrgRelatedDataWithTx(Long couponThemeId, Integer ruleType, List<Long> delOrgIds);

    Map<String, String> importOrgBatch(MktUseRuleImportAddOrgBo bo);

    Map<Integer, List<Long>> getMktUseRuleMap(MktUseRuleCouponThemeDetailBo bo);

    PageResponse<MktUseRuleOrgListResponse> listMktUseRule(MktUseRuleOrgListBo bo);

    PageResponse<MktUseRuleByIdsResponse> getMktUseRuleByIds(MktUseRuleByIdsBo bo);

    PageResponse<MktUseRuleSelectionResponse> getSelectedSelectionList(MktUseRuleSelectionBo bo);
}
