package com.fcb.coupon.app.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * captchas
 * @author mashiqiong
 * @date 2021-8-19 10:32
 */
@Data
@Accessors(chain = true)
@TableName("captchas")
public class CaptchasEntity implements Serializable {
    private static final long serialVersionUID = -5700745275800759557L;
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 设备号
     */
    private String deviceId;

    /**
     * 验证码
     */
    private String captcha;

    /**
     * 是否发送成功
     */
    private Integer successIs;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 业务类型
     */
    private Integer businessType;

    /**
     * 手机号
     */
    private String mobile;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;

    @TableField(fill = FieldFill.INSERT)
    @Version
    private Integer versionNo;

    @ApiModelProperty(value = "逻辑删除字段 0 正常 1 已删除")
    @TableLogic
    private Integer isDeleted;
}
