package com.fcb.coupon.backend.model.param.request;

import cn.hutool.core.bean.BeanUtil;
import com.fcb.coupon.backend.model.bo.CouponThemeListBo;
import com.fcb.coupon.common.dto.AbstractBaseConvertor;
import com.fcb.coupon.common.dto.AuthorityHolder;
import com.fcb.coupon.common.dto.UserInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 管理后台->营销中心->优惠券管理->优惠券活动列表->导出Excel 入参
 * @author mashiqiong
 * @date 2021-06-16 9:56
 */
@ApiModel(description = "查询券活动列表-入参")
@Data
public class CouponThemeActivityPageRequest extends AbstractBaseConvertor<CouponThemeListBo> implements Serializable {

    @ApiModelProperty(value = "券活动ID", dataType = "Long")
    private Long id;

    @ApiModelProperty(value = "优惠券名称", dataType = "String")
    private String themeTitle;

    @ApiModelProperty(value = "活动名称", dataType = "String")
    private String activityName;

    @ApiModelProperty(value = "券码生成方式 0电子券 1实体券/预制券 2红包券 3：第三方券码", dataType = "Integer")
    private Integer couponType;

    @ApiModelProperty(value = "发券类型(1:活动规则券,19:线下预制券,4:前台领券,17:主动营销券,18:权益优惠券,19:线下预制券,20:媒体广告券,21:直播券,22:营销活动页券)", dataType = "Integer")
    private Integer couponGiveRule;

    @ApiModelProperty(value = "状态，-1:全部；456:进行中、已过期、已关闭；0 未审核 1 待审核 3 审核不通过 4 进行中 5 已过期 6 已关闭", dataType = "Integer")
    private Integer status;

    @ApiModelProperty(value = "活动有效时间(起始)", dataType = "Date")
    private Date startTime;

    @ApiModelProperty(value = "活动有效时间(结束)", dataType = "Date")
    private Date endTime;

    @ApiModelProperty(value = "适用人群 0会员 1机构经纪人 2C端用户", dataType = "Integer")
    private Integer crowdScopeId;

    @ApiModelProperty(value = "所属商家", dataType = "List<Long>")
    private List<Long> orgIds;

    @ApiModelProperty(value = "优惠方式 0：金额 1：折扣 2：非固定金额 11：福利卡 12：红包券", dataType = "Integer")
    private Integer couponDiscountType;

    @Override
    public CouponThemeListBo convert() {
        CouponThemeListBo bo = new CouponThemeListBo();
        BeanUtil.copyProperties(this, bo);

        //设置登录用户信息
        UserInfo userInfo = AuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        if (Objects.nonNull(userInfo)) {
            bo.loadUserInfo(userInfo);
        }
        return bo;
    }
}
