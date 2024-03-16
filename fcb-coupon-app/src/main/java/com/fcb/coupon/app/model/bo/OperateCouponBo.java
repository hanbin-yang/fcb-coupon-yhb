package com.fcb.coupon.app.model.bo;

import com.fcb.coupon.app.model.dto.OperateCouponDto;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author YangHanBin
 * @date 2021-08-24 9:57
 */
@Data
public class OperateCouponBo implements Serializable {
    /**
     * 明源项目id
     */
    private String itemId;
    /**
     * 物业类型 0住宅 1公寓 2商铺 3写字楼 4车位 5储藏室
     */
    private String propertyType;
    /**
     * 房间id
     */
    private String roomGuid;
    /**
     * 房间名称
     */
    private String roomName;
    /**
     * 交易id
     */
    private String transactionId;
    /**
     * 原交易id
     */
    private String oldTransactionId;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 用户id
     */
    private String userId;

    private Integer userType;

    /**
     * 明源操作类型
     * 明源操作类型 1认购态换房 2认购态作废 3认购态退房/挞定 4签约态作废 5签约态退房/挞定 6重置认购态换房 7重置认购态作废 8重置认购态退房/挞定 9认购转签约 10重置认购态转签约态 11保存为认购态
     */
    private Integer oprType;

    /**
     * 需要解锁的券码
     */
    private List<OperateCouponDto> unlockCoupons;

    /**
     * 需要替换交易id的券码
     */
    private List<OperateCouponDto> rebindCoupons;

    /**
     * 需要上锁的券码
     */
    private List<OperateCouponDto> lockCoupons;

    /**
     * 需要核销的券
     */
    private List<OperateCouponDto> verifyCoupons;
    /**
     * 核销渠道 （如果是核销）
     */
    private Integer usedChannel;
}

