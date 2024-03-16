package com.fcb.coupon.app.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author YangHanBin
 * @date 2021-08-24 10:08
 */
@Data
@ApiModel(description = "订单-操作优惠券 入参")
public class OperateCoupons4OrderInputDto implements Serializable {
    private static final long serialVersionUID = 1061597148017090793L;
    /**
     * 项目id：1
     * 房间id：123
     * 原交易id：S01
     * 券操作：解锁
     * 【券码：A】
     * 项目id：1
     * 房间id：123
     * 新交易id：S02
     * 券操作：锁定
     * 【券码：C】
     * 券操作：替换
     * 【券码：B】
     */
    @ApiModelProperty(value="明源项目id", required = true)
    private String itemId;

    @ApiModelProperty(value="物业类型 0住宅 1公寓 2商铺 3写字楼 4车位 5储藏室")
    private String propertyType;

    @ApiModelProperty(value="房间id", required = true)
    private String roomGuid;

    @ApiModelProperty(value="房间名称", required = true)
    private String roomName;

    @ApiModelProperty(value="交易id", required = true)
    private String transactionId;

    @ApiModelProperty(value="原交易id")
    private String oldTransactionId;

    @ApiModelProperty(value="手机号", required = true)
    private String phone;

    @ApiModelProperty(value="需要解锁的券")
    private List<OperateCouponDto> unlockCoupons;

    @ApiModelProperty(value="需要换绑交易id的券")
    private List<OperateCouponDto> rebindCoupons;

    @ApiModelProperty(value="需要上锁的券")
    private List<OperateCouponDto> lockCoupons;

    @ApiModelProperty(value = "需要核销的优惠券")
    private List<OperateCouponDto> verifyCoupons;

    @ApiModelProperty(value="明源操作类型 1认购态换房 2认购态作废 3认购态退房/挞定 4签约态作废 5签约态退房/挞定 6重置认购态换房 7重置认购态作废 8重置认购态退房/挞定 9认购转签约 10重置认购态转签约态 11保存为认购态", required = true)
    private Integer oprType;

    @ApiModelProperty(value = "核销来源 0手动核销 1明源核销", required = true)
    private Integer usedChannel;
}

