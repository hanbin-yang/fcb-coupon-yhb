package com.fcb.coupon.app.model.param.request;

import cn.hutool.core.bean.BeanUtil;
import com.fcb.coupon.app.model.bo.CouponBo;
import com.fcb.coupon.app.model.bo.CouponUserGetBo;
import com.fcb.coupon.common.dto.AbstractBaseConvertor;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 券详情查询请求
 *
 * @Author WeiHaiQi
 * @Date 2021-08-17 18:04
 **/
@Data
public class CouponUserGetRequest implements Serializable {

    private static final long serialVersionUID = -543139939538801277L;

    @NotNull(message = "券ID不能为空")
    @ApiModelProperty(value = "券id", required = true)
    private Long couponId;
    @NotBlank(message = "userId不能为空")
    @ApiModelProperty(value = "unionId 或 （saas为userid）", required = true)
    private String userId;


}
