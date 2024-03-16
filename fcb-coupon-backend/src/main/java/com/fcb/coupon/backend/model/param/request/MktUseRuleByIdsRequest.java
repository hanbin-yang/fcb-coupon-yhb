package com.fcb.coupon.backend.model.param.request;

import cn.hutool.core.bean.BeanUtil;
import com.fcb.coupon.backend.model.bo.MktUseRuleByIdsBo;
import com.fcb.coupon.common.dto.AbstractBaseConvertor;
import com.fcb.coupon.common.dto.AuthorityHolder;
import com.fcb.coupon.common.dto.UserInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @author mashiqiong
 * @date 2021-08-02 16:30
 */
@ApiModel(description = "管理后台->优惠券活动列表->添加后条件查询适用组织 入参")
@Data
public class MktUseRuleByIdsRequest extends AbstractBaseConvertor<MktUseRuleByIdsBo> implements Serializable {

    @ApiModelProperty(value = "商家ID", dataType = "Long")
    private Long merchantId;
    @ApiModelProperty(value = "集团ID", dataType = "Long")
    private Long groupId;
    @ApiModelProperty(value = "商家名称", dataType = "Long")
    private String merchantName;
    @ApiModelProperty(value = "券活动ID", dataType = "Long")
    private Long themeRef;
    @ApiModelProperty(value = "省代码", dataType = "Long")
    private Long provinceCode;
    @ApiModelProperty(value = "市代码", dataType = "Long")
    private Long cityCode;
    @ApiModelProperty(value = "类型", dataType = "Long")
    private Integer refType;
    @ApiModelProperty(value = " 适用范围ID", dataType = "Long")
    private Integer merchantType;
    @ApiModelProperty(value = "所属店铺ids（针对店铺）", dataType = "List<Long>")
    private List<Long> storeIds;
    @ApiModelProperty(value = "需要查询的范围限制ids", dataType = "List<Long>")
    private List<Long> merchantIds;
    @ApiModelProperty(value = "所属集团的ids（针对商家，店铺）", dataType = "List<Long>")
    private List<Long> belongsToGroupIds;
    @ApiModelProperty(value = "所属商家ids（针对店铺）", dataType = "List<Long>")
    private List<Long> belongsToMerchantIds;
    @ApiModelProperty(value = "店铺编码", dataType = "String")
    private String buildCode;
    @ApiModelProperty(value = "商家代码", dataType = "String")
    private String merchantCode;
    @ApiModelProperty(value = "当前页码")
    private Integer currentPage;
    @ApiModelProperty(value = "页大小")
    private Integer itemsPerPage;
    @Override
    public MktUseRuleByIdsBo convert() {
        MktUseRuleByIdsBo bo = new MktUseRuleByIdsBo();
        BeanUtil.copyProperties(this, bo);

        //设置登录用户信息
        UserInfo userInfo = AuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        if (Objects.nonNull(userInfo)) {
            bo.loadUserInfo(userInfo);
        }
        return bo;
    }
}
