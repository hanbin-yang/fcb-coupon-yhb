package com.fcb.coupon.backend.model.param.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fcb.coupon.backend.serializer.DateToLongSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * TODO
 *
 * @Author Weihq
 * @Date 2021-06-15 18:58
 **/
@ApiModel(value="优惠券明细",description="优惠券明细")
@Data
public class CouponViewResponse implements Serializable {
    private static final long serialVersionUID = -1698868954971347629L;

    @ApiModelProperty(value="券id")
    private Long id;
    @ApiModelProperty(value="券码")
    private String couponCode;
    @ApiModelProperty(value="券活动ID")
    private Long couponThemeId;
    @ApiModelProperty(value="券活动名称")
    private String themeTitle;
    @ApiModelProperty(value="券状态")
    private Integer status;
    @ApiModelProperty(value="券生效结束时间")
    @JsonSerialize(using = DateToLongSerializer.class)
    private Date endTime;
    @ApiModelProperty(value="使用时间(核销时间)")
    private Date usedTime;
    @ApiModelProperty(value="券生效开始时间")
    @JsonSerialize(using = DateToLongSerializer.class)
    private Date startTime;
    @ApiModelProperty(value="创建时间")
    @JsonSerialize(using = DateToLongSerializer.class)
    private Date createTime;
    @ApiModelProperty(value="订单code")
    private String orderCode;
    @ApiModelProperty(value="绑券时间")
    @JsonSerialize(using = DateToLongSerializer.class)
    private Date bindTime;
    @ApiModelProperty(value="绑定人手机号")
    @JsonProperty(value = "cellNo")
    private String bindTel;
    @ApiModelProperty(value="用户id")
    private Long userId;
    @ApiModelProperty(value="用户名称")
    private String userName;
    @ApiModelProperty(value="劵类型")
    private Integer couponType;
    @ApiModelProperty(value="核销店铺id")
    private Long usedStoreId;
    @ApiModelProperty(value="核销店铺")
    private String usedStoreName;
    @ApiModelProperty(value="楼盘编码")
    private String usedStoreCode;
    @ApiModelProperty(value="转赠类型")
    private Integer giveType;
    @ApiModelProperty(value="转赠人id")
    private Long giveUserId;
    @ApiModelProperty(value="转赠人名称")
    private String giveUserName;
    @ApiModelProperty(value="转赠人手机号")
    private String giveUserMobile;
    @ApiModelProperty(value="转赠时间")
    @JsonSerialize(using = DateToLongSerializer.class)
    private Date giveTime;
    @ApiModelProperty(value="接受人id")
    private Long receiveUserId;
    @ApiModelProperty(value="接受人手机号")
    private String receiveUserMobile;
    @ApiModelProperty(value="接受人账号类型")
    private Integer receiveUserType;
    @ApiModelProperty(value="接收人获得的新的券id")
    private Long receiveCouponId;
    @ApiModelProperty(value="接收人获得的券的状态")
    private Integer receiveCouponStatus;
    @ApiModelProperty(value="来源")
    private Integer source;
    @ApiModelProperty(value="来源id")
    private String sourceId;
    @ApiModelProperty(value="来源描述")
    private String sourceStr;
    @ApiModelProperty(value="修改人")
    private String updateUsername;

    @ApiModelProperty(value="组织id集合")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private List<Long> orgIds;
    @ApiModelProperty(value="组织名称集合")
    private List<String> orgNames;
    @ApiModelProperty(value="使用人群 0是会员,1是机构经纪人,2是C端用户")
    private Integer crowdScopeId;
    @ApiModelProperty(value="使用人群")
    private String crowdScopeIdStr;
    @ApiModelProperty(value="第三方券码")
    private String thirdCouponCode;
}
