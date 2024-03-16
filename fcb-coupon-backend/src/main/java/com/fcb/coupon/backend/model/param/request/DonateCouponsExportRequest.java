package com.fcb.coupon.backend.model.param.request;

import cn.hutool.core.bean.BeanUtil;
import com.fcb.coupon.backend.model.bo.DonateCouponsExportBo;
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
 * 赠送优惠券明细导出请求
 *
 * @Author WeiHaiQi
 * @Date 2021-06-25 17:16
 **/
@Data
public class DonateCouponsExportRequest extends AbstractBaseConvertor<DonateCouponsExportBo> implements Serializable {

    private static final long serialVersionUID = 5319473138926321624L;

    @ApiModelProperty(value = "电话号码")
    private String cellNo;
    @ApiModelProperty(value = "优惠券活动名称")
    private String couponActivityName;
    @ApiModelProperty(value = "券码")
    private String couponCode;
    @ApiModelProperty(value = "优惠券状态")
    private Integer couponStatus;
    @ApiModelProperty(value = "券有效期-开始时间")
    private Date couponEffectiveStartTime;
    @ApiModelProperty(value = "券有效期-结束时间")
    private Date couponEffectiveEndTime;
    @ApiModelProperty(value = "生券-开始时间")
    private Date couponCreateStartTime;
    @ApiModelProperty(value = "生券-结束时间")
    private Date couponCreateEndTime;
    @ApiModelProperty(value = "券活id")
    private Long couponActivityId;
    @ApiModelProperty(value = "赠送时间-开始")
    private Date giveTimeFrom;
    @ApiModelProperty(value = "赠送时间-结束")
    private Date giveTimeTo;
    @ApiModelProperty(value = "赠送券的接收人手机号")
    private String receiveUserMobile;
    @ApiModelProperty(value = "组织ID集合")
    private List<Long> orgIds;
    @ApiModelProperty(value = "账号类型，0是会员,1是机构经纪人,2是C端用户")
    private Integer crowdScopeId;
    @ApiModelProperty(notes = "账号类型，0是会员,1是机构经纪人,2是C端用户", example = "0")
    private Integer receiveUserType;

    @Override
    public DonateCouponsExportBo convert() {
        DonateCouponsExportBo bo = new DonateCouponsExportBo();
        BeanUtil.copyProperties(this, bo);

        // 设置登录用户信息
        UserInfo userInfo = AuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        if (Objects.nonNull(userInfo)) {
            bo.loadUserInfo(userInfo);
        }
        return bo;
    }
}
