package com.fcb.coupon.backend.model.param.request;

import cn.hutool.core.bean.BeanUtil;
import com.fcb.coupon.backend.model.bo.MktUseRuleSelectionBo;
import com.fcb.coupon.common.dto.AbstractBaseConvertor;
import com.fcb.coupon.common.dto.AuthorityHolder;
import com.fcb.coupon.common.dto.UserInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author mashiqiong
 * @date 2021-08-02 09:54
 */
@ApiModel(description = "管理后台->优惠券活动列表->添加后条件查询适用组织 入参")
@Data
public class MktUseRuleSelectionRequest extends AbstractBaseConvertor<MktUseRuleSelectionBo> implements Serializable {

    @ApiModelProperty(value = "券活动ID", dataType = "Long")
    private Long themeRef;
    @ApiModelProperty(value = "规则类型：0：券规则；1：卡规则；2：促销规则", dataType = "Integer")
    private Integer refType;
    @ApiModelProperty(value = "规则类型：0:地区限制 1：商家限制 2: 商品限制 按类目 3：商品限制 按品牌 4 ：商品限制 按产品 5：商品限制：按商品", dataType = "Integer")
    private Integer RuleType;
    @ApiModelProperty(value = "地区主键 或者商家主键", dataType = "Long")
    private Long limitRef;
    @ApiModelProperty(value = "限制名称", dataType = "String")
    private String refDescription;
    @ApiModelProperty(value = "限制编码", dataType = "String")
    private String extendRef;
    private String merchantCode;
    @ApiModelProperty(value = "当前页码")
    private Integer currentPage;
    @ApiModelProperty(value = "页大小")
    private Integer itemsPerPage;
    @Override
    public MktUseRuleSelectionBo convert() {
        MktUseRuleSelectionBo bo = new MktUseRuleSelectionBo();
        BeanUtil.copyProperties(this, bo);

        //设置登录用户信息
        UserInfo userInfo = AuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        if (Objects.nonNull(userInfo)) {
            bo.loadUserInfo(userInfo);
        }
        return bo;
    }
}
