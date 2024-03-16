package com.fcb.coupon.app.model.bo;

import com.fcb.coupon.app.model.dto.CouponDo;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author YangHanBin
 * @date 2021-08-24 15:04
 */
@Data
public class OperateCouponDbBo {
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
    private String orderCode;
    /**
     * 绑定手机号
     */
    private String bindTel;
    /**
     * 楼盘名称
     */
    private String storeName;
    /**
     * 楼盘编码
     */
    private String buildCode;
    /**
     * 楼盘id
     */
    private Long storeId;
    /**
     * 待解锁券
     */
    private List<CouponDo> unlockCoupons;
    /**
     * 待换绑券
     */
    private List<CouponDo> rebindCoupons;
    /**
     * 上锁券
     */
    private List<CouponDo> lockCoupons;
    /**
     * 核销券
     */
    private List<CouponDo> verifyCoupons;
    /**
     * 操作时间
     */
    private Date updateTime;
    /**
     * 渠道
     */
    private Integer usedChannel;
}

