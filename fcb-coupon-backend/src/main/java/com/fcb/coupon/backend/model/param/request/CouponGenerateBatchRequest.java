package com.fcb.coupon.backend.model.param.request;

import cn.hutool.core.bean.BeanUtil;
import com.fcb.coupon.backend.model.bo.CouponGenerateBatchBo;
import com.fcb.coupon.common.dto.AbstractBaseConvertor;
import com.fcb.coupon.common.dto.AuthorityHolder;
import com.fcb.coupon.common.dto.UserInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Data
@ApiModel(description = "查询批量发送券活动任务列表参数")
public class CouponGenerateBatchRequest extends AbstractBaseConvertor<CouponGenerateBatchBo> implements Serializable {

    @ApiModelProperty(value = "批次id", required = false)
    private Long id;
    @ApiModelProperty(value = "任务类型", required = false)
    private Integer type;
    @ApiModelProperty(value = "任务名称", required = false)
    private String typeName;
    @ApiModelProperty(value = "多个任务类型", required = false)
    private List<Integer> types;

    @ApiModelProperty(value = "任务状态", required = false)
    private Integer sendCouponStatus;
    @ApiModelProperty(value = "创建时间", required = false)
    private Date createTime;
    @ApiModelProperty(value = "导入完成时间", required = false)
    private Date finishTime;
    @ApiModelProperty(value = "总记录数", required = false)
    private Integer totalRecord;
    @ApiModelProperty(value = "成功导入记录数", required = false)
    private Integer successRecord;
    @ApiModelProperty(value = "失败记录数", required = false)
    private Integer failRecord;
    @ApiModelProperty(value = "活动名称", required = false)
    private String themeTitle;
    @ApiModelProperty(value = "开始时间", required = false)
    private Date startTime;
    @ApiModelProperty(value = "导入完成时间", required = false)
    private Date endTime;
    @ApiModelProperty(value = "生成张数", required = false)
    private Integer generateNums;
    @ApiModelProperty(value = "操作人", required = false)
    private String createUsername;
    @ApiModelProperty(value = "上传文件", required = false)
    private String uploadFile;
    @ApiModelProperty(value = "1:优惠券活动列表（否则默认为这个模块）；2：明细列表；3：转赠列表；4：转让列表；5：核销列表；", required = false)
    private Integer moduleType;
    @ApiModelProperty(value = "模块名称", required = false)
    private String moduleName;
    @ApiModelProperty(value = "失败原因", required = false)
    private String failReason;
    @ApiModelProperty(value = "当前页码", required = false)
    private Integer currentPage;
    @ApiModelProperty(value = "页面pageSize", required = false)
    private Integer itemsPerPage;

    @Override
    public CouponGenerateBatchBo convert() {
        CouponGenerateBatchBo bo = new CouponGenerateBatchBo();
        BeanUtil.copyProperties(this, bo);

        //设置登录用户信息
        UserInfo userInfo = AuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        if (Objects.nonNull(userInfo)) {
            bo.loadUserInfo(userInfo);
        }
        return bo;
    }
}
