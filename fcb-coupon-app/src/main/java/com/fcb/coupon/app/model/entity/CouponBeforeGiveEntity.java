package com.fcb.coupon.app.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * coupon_before_give
 * @author 
 */
@Data
@Accessors(chain = true)
@TableName("coupon_before_give")
public class CouponBeforeGiveEntity implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 券活动id
     */
    private Long couponThemeId;

    /**
     * 券ID
     */
    private Long couponId;

    /**
     * 失效时间
     */
    private Date expireTime;

    /**
     * 转赠类型 1短信赠送 2面对面赠送 3微信好友分享
     */
    private Integer giveType;

    /**
     * 赠送者userid
     */
    private String giveUserid;

    /**
     * 0 B端、1 saas端、2 C端、3旧机构
     */
    private Integer giveUserType;

    /**
     * 赠送者头像地址
     */
    private String giveAvatar;

    /**
     * 赠送者呢称
     */
    private String giveNickname;

    /**
     * 赠送者手机号
     */
    private String giveUserMobile;

    /**
     * 取值为android/ios/web/miniapp  app就是传的android、ios
     */
    private String terminalType;

    /**
     * 接受者手机号
     */
    private String receiveUserMobile;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新用户ID
     */
    private String updateUserid;

    /**
     * 更新用户名
     */
    private String updateUsername;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;

    @Version
    @TableField(fill = FieldFill.INSERT)
    private Integer versionNo;

    /**
     * 逻辑删除字段 0 正常 1 已删除
     */
    @TableLogic
    private Integer isDeleted;

    private static final long serialVersionUID = 1L;
}