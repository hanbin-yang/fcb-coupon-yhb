package com.fcb.coupon.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * async_task
 * @author 
 */
@Data
@Accessors(chain = true)
@TableName("async_task")
@ApiModel(value="AsyncTaskEntity对象", description="异步执行记录表")
public class AsyncTaskEntity implements Serializable {
    private static final long serialVersionUID = 1898712742709827037L;
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 异步导出业务类型
     */
    private String taskType;

    /**
     * 下载路径
     */
    private String downPath;

    /**
     * 异步状态 0：异步任务待执行 1：异步执行成功 2： 异步执行失败
     */
    private Integer asyncStatus;

    /**
     * 生成文件的记录数
     */
    private Integer records;

    /**
     * 异步任务文件的生成时间
     */
    private Date createFileTime;

    /**
     * 成功数
     */
    private Integer successRecord;

    /**
     * 失败数
     */
    private Integer failRecord;

    private Long createUserid;

    private String createUsername;

    @ApiModelProperty(value = "创建时间")
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
    private Byte isDeleted;
}