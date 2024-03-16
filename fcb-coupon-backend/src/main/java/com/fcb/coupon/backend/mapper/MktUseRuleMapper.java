package com.fcb.coupon.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fcb.coupon.backend.model.dto.MktUseRuleByIdsDto;
import com.fcb.coupon.backend.model.dto.MktUseRuleOrgListDto;
import com.fcb.coupon.backend.model.dto.MktUseRuleSelectionDto;
import com.fcb.coupon.backend.model.entity.MktUseRuleEntity;

import java.util.List;


/**
 * <p>
 * 规则设置表 Mapper 接口
 * </p>
 *
 * @author 自动生成
 * @since 2021-06-11
 */
public interface MktUseRuleMapper extends BaseMapper<MktUseRuleEntity> {

    void insertBatch(List<MktUseRuleEntity> insertBeans);

    List<MktUseRuleEntity> listMktUseRule(MktUseRuleOrgListDto dto);

    Integer listMktUseRuleCount(MktUseRuleOrgListDto dto);

    int getMktUseRuleByIdsCount(MktUseRuleByIdsDto dto);

    List<MktUseRuleEntity> getMktUseRuleByIds(MktUseRuleByIdsDto dto);

    int getSelectedSelectionListCount(MktUseRuleSelectionDto dto);

    List<MktUseRuleEntity> getSelectedSelectionList(MktUseRuleSelectionDto dto);
}
