package com.fcb.coupon.app.model.bo;

import com.fcb.coupon.app.model.dto.OperateCouponDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 名    称：CheckCouponUsefulBo
 * 功    能：
 * 创 建 人：mashiqiong
 * 创建时间：2021/08/25 17:53
 * 修 改 人：
 * 修改时间：
 * 说    明：
 * 版 本 号：
 */
@Data
public class CheckCouponUsefulBo implements Serializable {
    private static final long serialVersionUID = 7688729911603662818L;

    @ApiModelProperty(value="明源项目id")
    private String itemId;

    @ApiModelProperty(value="物业类型 0住宅 1公寓 2商铺 3写字楼 4车位 5储藏室")
    private String propertyType;

    @ApiModelProperty(value="房源id")
    private String roomGuid;

    @ApiModelProperty(value="交易id", required = true)
    private String transactionId;

    @ApiModelProperty(value="手机号", required = true)
    private String phone;

    @ApiModelProperty(value="券id列表", required = true)
    private List<OperateCouponDto> checkCoupons;
}
