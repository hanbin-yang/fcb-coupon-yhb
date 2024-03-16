package com.fcb.coupon.backend.model.param.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 营销中心->优惠券管理->优惠券活动列表 出参
 * @author mashiqiong
 * @date 2021-06-28 20:59
 */
@ApiModel(value="优惠券列表",description="优惠券列表")
@Data
public class AsyncTaskListResponse implements Serializable {
    private static final long serialVersionUID = -1698868954971347739L;

    @ApiModelProperty(value = "主键ID")
    private Long id;

    /**
     * 异步导出业务类型
     */
    @ApiModelProperty(value = "异步导出业务类型")
    private String taskType;

    /**
     * 下载路径
     */
    @ApiModelProperty(value = "下载路径")
    private String downPath;

    /**
     * 异步状态 0：异步任务待执行 1：异步执行成功 2： 异步执行失败
     */
    @ApiModelProperty(value = "异步状态 0：异步任务待执行 1：异步执行成功 2： 异步执行失败")
    private Integer asyncStatus;

    /**
     * 生成文件的记录数
     */
    @ApiModelProperty(value = "生成文件的记录数")
    private Integer records;

    /**
     * 异步任务文件的生成时间
     */
    @ApiModelProperty(value = "异步任务文件的生成时间")
    private Date createFileTime;

    /**
     * 成功数
     */
    @ApiModelProperty(value = "成功数")
    private Integer successRecord;

    /**
     * 失败数
     */
    @ApiModelProperty(value = "主键ID")
    private Integer failRecord;

    @ApiModelProperty(value = "创建用户ID")
    private Long createUserid;

    @ApiModelProperty(value = "创建用户名")
    private String createUsername;

    @ApiModelProperty(value = "创建用户ID")
    private String createUserip;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新用户ID")
    private Long updateUserid;

    @ApiModelProperty(value = "更新用户名")
    private String updateUsername;

    @ApiModelProperty(value = "更新IP")
    private String updateUserip;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "版本号")
    private Integer versionNo;

    /**
     * 逻辑删除字段 0 正常 1 已删除
     */
    @ApiModelProperty(value = "逻辑删除字段 0 正常 1 已删除")
    private Byte isDeleted;
}
