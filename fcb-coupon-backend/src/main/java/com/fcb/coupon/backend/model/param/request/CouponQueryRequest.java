package com.fcb.coupon.backend.model.param.request;

import cn.hutool.core.bean.BeanUtil;
import com.fcb.coupon.backend.model.bo.CouponQueryBo;
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
 * 优惠券明细查询类
 *
 * @Author Weihq
 * @Date 2021-06-15 16:21
 **/
@ApiModel(description = "优惠券明细查询类")
@Data
public class CouponQueryRequest extends AbstractBaseConvertor<CouponQueryBo> implements Serializable {
    private static final long serialVersionUID = 2761418553892070453L;

    @ApiModelProperty(value = "优惠券活动ID")
    private Long couponActivityId;
    @ApiModelProperty(value = "优惠券活动名称")
    private String couponActivityName;
    @ApiModelProperty(value = "手机号")
    private String cellNo;
    @ApiModelProperty(value = "券码")
    private String couponCode;
    @ApiModelProperty(value = "优惠券创建时间-开始")
    private Date couponCreateStartTime;
    @ApiModelProperty(value = "优惠券创建时间-结束")
    private Date couponCreateEndTime;
    @ApiModelProperty(value = "优惠券有效开始时间")
    private Date couponEffectiveStartTime;
    @ApiModelProperty(value = "优惠券有效截止时间")
    private Date couponEffectiveEndTime;
    @ApiModelProperty(value = "来源")
    private Integer source;
    @ApiModelProperty(value = "来源id")
    private String sourceId;
    @ApiModelProperty(notes = "账号类型，0是会员,1是机构经纪人,2是C端用户")
    private Integer crowdScopeId;
    @ApiModelProperty(value = "优惠券状态")
    private Integer couponStatus;
    @ApiModelProperty(value = "券码是否脱敏")
    private Boolean mask;
    @ApiModelProperty(value = "组织ID集合")
    private List<Long> orgIds;
    @ApiModelProperty(value = "优惠券类型：3第三方券码")
    private Integer couponType;
    @ApiModelProperty(value = "接收人账号类型，0是会员,1是机构经纪人,2是C端用户", example = "0")
    private Integer receiveUserType;
    @ApiModelProperty(value = "赠送时间-开始")
    private Date giveTimeFrom;
    @ApiModelProperty(value = "赠送时间-结束")
    private Date giveTimeTo;
    @ApiModelProperty(value = "赠送券的接收人手机号")
    private String receiveUserMobile;
    /**
     * 查询券活动的时候会根据此类型判断是否将count和page分成两个接口，请勿轻易改动
     */
    @ApiModelProperty(value = "查询类型 1核销")
    private Integer type;
    @ApiModelProperty(value = "优惠券活动id集合")
    private List<Long> themeIds;

    @Override
    public CouponQueryBo convert() {
        CouponQueryBo bo = new CouponQueryBo();
        BeanUtil.copyProperties(this, bo);

        //设置登录用户信息
        UserInfo userInfo = AuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        if (Objects.nonNull(userInfo)) {
            bo.loadUserInfo(userInfo);
        }
        return bo;
    }
}
