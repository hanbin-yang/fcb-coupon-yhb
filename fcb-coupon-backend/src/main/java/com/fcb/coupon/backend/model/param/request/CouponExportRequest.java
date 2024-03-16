package com.fcb.coupon.backend.model.param.request;

import cn.hutool.core.bean.BeanUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fcb.coupon.backend.model.bo.CouponExportBo;
import com.fcb.coupon.common.dto.AbstractBaseConvertor;
import com.fcb.coupon.common.dto.AuthorityHolder;
import com.fcb.coupon.common.dto.UserInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 优惠券明细导出请求
 *
 * @Author WeiHaiQi
 * @Date 2021-06-24 14:35
 **/
@Data
public class CouponExportRequest extends AbstractBaseConvertor<CouponExportBo> implements Serializable {

    @ApiModelProperty(value = "电话号码")
    private String cellNo;
    @ApiModelProperty(value = "优惠券活动名称")
    private String couponActivityName;
    @ApiModelProperty(value = "券码")
    private String couponCode;
    @ApiModelProperty(value = "优惠券状态")
    private Integer couponStatus;
    @ApiModelProperty(value = "券有效期-开始时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    private Date couponEffectiveStartTime;
    @ApiModelProperty(value = "券有效期-结束时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    private Date couponEffectiveEndTime;
    @ApiModelProperty(value = "生券-开始时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    private Date couponCreateStartTime;
    @ApiModelProperty(value = "生券-结束时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    private Date couponCreateEndTime;
    @ApiModelProperty(value = "券活id")
    private Long couponActivityId;
    @ApiModelProperty(value = "组织ID集合")
    private List<Long> orgIds;
    @ApiModelProperty(value = "优惠券类型 3第三方券码")
    private Integer couponType;
    @ApiModelProperty(value = "来源")
    private Integer source;
    @ApiModelProperty(value = "账号类型，0是会员,1是机构经纪人,2是C端用户")
    private Integer crowdScopeId;

    @Override
    public CouponExportBo convert() {
        CouponExportBo bo = new CouponExportBo();
        BeanUtil.copyProperties(this, bo);

        // 设置登录用户信息
        UserInfo userInfo = AuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        if (Objects.nonNull(userInfo)) {
            bo.loadUserInfo(userInfo);
        }
        return bo;
    }
}
