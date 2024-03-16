package com.fcb.coupon.backend.model.param.request;

import cn.hutool.core.bean.BeanUtil;
import com.fcb.coupon.backend.model.bo.CouponVerificationListBo;
import com.fcb.coupon.common.dto.AbstractBaseConvertor;
import com.fcb.coupon.common.dto.AuthorityHolder;
import com.fcb.coupon.common.dto.UserInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 后台管理->营销中心->优惠券管理->券核销->券核销列表
 * @author mashiqiong
 * @date 2021-6-23 21:23
 */
@Data
public class CouponVerificationListRequest extends AbstractBaseConvertor<CouponVerificationListBo> implements Serializable {
    private static final long serialVersionUID = 2088486608316275708L;
    @ApiModelProperty(value = "优惠券ID")
    private Long couponActivityId;
    @ApiModelProperty(value = "券活动名称")
    private String couponActivityName;
    @ApiModelProperty(value = "券号")
    private String couponCode;
    @ApiModelProperty(value = "优惠券状态")
    private Integer couponStatus;
    @ApiModelProperty(value = "券生效开始时间")
    private Date couponEffectiveStartTime;
    @ApiModelProperty(value = "券生效结束时间")
    private Date couponEffectiveEndTime;
    @ApiModelProperty(value = "创建开始时间")
    private Date couponCreateStartTime;
    @ApiModelProperty(value = "创建结束时间")
    private Date couponCreateEndTime;
    @ApiModelProperty(value = "核销开始时间")
    private Date usedStartTime;
    @ApiModelProperty(value = "核销结束时间")
    private Date usedEndTime;
    @ApiModelProperty(value = "手机号码")
    private String cellNo;
    @ApiModelProperty(value = "优惠券绑定开始时间")
    private Date couponBindStartTime;
    @ApiModelProperty(value = "优惠券绑定截至时间")
    private Date couponBindEndTime;
    @ApiModelProperty(value = "核销人")
    private String updateUsername;
    @ApiModelProperty(value = "使用人群编号，0是会员,1是机构经纪人,2是C端用户")
    private Integer crowdScopeId;
    @ApiModelProperty(value = "核销店铺Id")
    private Long usedStoreId;
    @ApiModelProperty(value = "核销店铺名称")
    private String usedStoreName;
    @ApiModelProperty(value = "楼盘编码")
    private String usedStoreCode;
    @ApiModelProperty(value = "订单code")
    private String orderCode;

    @Override
    public CouponVerificationListBo convert() {
        CouponVerificationListBo bo = new CouponVerificationListBo();
        BeanUtil.copyProperties(this, bo);

        //设置登录用户信息
        UserInfo userInfo = AuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        if (Objects.nonNull(userInfo)) {
            bo.loadUserInfo(userInfo);
        }
        return bo;
    }
}
