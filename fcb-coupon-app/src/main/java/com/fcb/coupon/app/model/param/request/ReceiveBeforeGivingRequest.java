package com.fcb.coupon.app.model.param.request;

import com.fcb.coupon.app.model.bo.ReceiveBeforeGivingBo;
import com.fcb.coupon.common.dto.AbstractBaseConvertor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author YangHanBin
 * @date 2021-08-13 8:56
 */
@Data
@ApiModel(description = "转增-领取福利 入参")
public class ReceiveBeforeGivingRequest extends AbstractBaseConvertor<ReceiveBeforeGivingBo> implements Serializable {
    private static final long serialVersionUID = -6947620135227022099L;

    @ApiModelProperty(value = "转赠记录编码", required = true)
    @NotBlank
    private String beforeGiveCode;

    @ApiModelProperty(value = "领券人用户id")
    private Long receiveUserId;

    public ReceiveBeforeGivingBo convert() {
        ReceiveBeforeGivingBo bo = new ReceiveBeforeGivingBo();
        BeanUtils.copyProperties(this, bo);
        return bo;
    }
}
