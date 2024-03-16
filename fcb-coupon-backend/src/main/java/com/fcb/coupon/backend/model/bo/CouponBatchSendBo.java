package com.fcb.coupon.backend.model.bo;

import com.fcb.coupon.backend.model.dto.CouponBatchSendImportBrokerDto;
import com.fcb.coupon.backend.model.dto.CouponBatchSendImportCustomerDto;
import com.fcb.coupon.backend.model.dto.CouponBatchSendImportMemberDto;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/*
优惠券发券参数
 */
@Data
public class CouponBatchSendBo {

    /**
     * 登录用户信息
     */
    private Long userId;
    private String username;


    /*
    优惠券活动
     */
    private Long themeId;

    /*
    发送的用户类型
     */
    private Integer sendUserType;

    /*
    来源
    */
    private Integer source;

    /*
    来源ID
    */
    private String sourceId;


    /*
    发送列表
     */
    private List<CouponSendUserBo> sendUserList;


    public void setPhones(String phoneStr) {
        if (StringUtils.isBlank(phoneStr)) {
            return;
        }
        this.sendUserList = new ArrayList<>();
        String[] phones = StringUtils.split(phoneStr, ',');
        for (String phone : phones) {
            if (StringUtils.isBlank(phone)) {
                continue;
            }
            CouponSendUserBo couponSendPhoneBo = new CouponSendUserBo();
            couponSendPhoneBo.setPhone(phone);
            couponSendPhoneBo.setCount(1);
            this.sendUserList.add(couponSendPhoneBo);
        }
    }

    public void setUserIds(String userIdStr) {
        if (StringUtils.isBlank(userIdStr)) {
            return;
        }
        this.sendUserList = new ArrayList<>();
        String[] userIds = StringUtils.split(userIdStr, ',');
        for (String userId : userIds) {
            if (StringUtils.isBlank(userId)) {
                continue;
            }
            CouponSendUserBo couponSendUserBo = new CouponSendUserBo();
            couponSendUserBo.setUserId(userId);
            couponSendUserBo.setCount(1);
            this.sendUserList.add(couponSendUserBo);
        }
    }

    public void setCustomers(List<CouponBatchSendImportCustomerDto> customerDtos) {
        this.sendUserList = new ArrayList<>();
        customerDtos.forEach(m -> {
            CouponSendUserBo couponSendPhoneBo = new CouponSendUserBo();
            couponSendPhoneBo.setPhone(m.getPhone());
            couponSendPhoneBo.setCount(m.getCount());
            this.sendUserList.add(couponSendPhoneBo);
        });
    }

    public void setMembers(List<CouponBatchSendImportMemberDto> memberDtos) {
        this.sendUserList = new ArrayList<>();
        memberDtos.forEach(m -> {
            CouponSendUserBo couponSendPhoneBo = new CouponSendUserBo();
            couponSendPhoneBo.setPhone(m.getPhone());
            couponSendPhoneBo.setCount(m.getCount());
            this.sendUserList.add(couponSendPhoneBo);
        });
    }

    public void setBrokers(List<CouponBatchSendImportBrokerDto> brokerDtos) {
        this.sendUserList = new ArrayList<>();
        brokerDtos.forEach(m -> {
            CouponSendUserBo couponSendPhoneBo = new CouponSendUserBo();
            couponSendPhoneBo.setPhone(m.getPhone());
            couponSendPhoneBo.setCount(m.getCount());
            this.sendUserList.add(couponSendPhoneBo);
        });
    }

}
