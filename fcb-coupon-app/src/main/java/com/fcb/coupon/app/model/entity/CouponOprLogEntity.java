package com.fcb.coupon.app.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * YangHanBin
 * @author 
 */
@Data
@TableName("coupon_opr_log")
public class CouponOprLogEntity implements Serializable {
    private static final long serialVersionUID = -3989082873006304509L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 操作类型 1 券 2 券活动
     */
    private Integer oprThemeType;

    /**
     * 操作类型 1.新建 2.提交审核 3.审核 4.驳回 5.生券 6.编辑 7.复制 8.关闭 9.发券 10.导入券码 11.删除 12.查看 101.作废 102.冻结 103.解冻 104.延期
     */
    private Integer oprType;

    /**
     * 操作概述：比如：修改模板
     */
    private String oprSummary;

    /**
     * 日志关联主体id
     */
    private Long oprRefId;

    /**
     * 其它扩展数据
     */
    private String extData;

    private String operContent;

    private Long createUserid;

    private String createUsername;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    private Long updateUserid;

    private String updateUsername;

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
}