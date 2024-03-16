package com.fcb.coupon.app.model.param.request;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.fcb.coupon.app.model.bo.CouponBo;
import com.fcb.coupon.app.model.bo.CouponMingyuanBo;
import com.fcb.coupon.common.dto.AbstractBaseConvertor;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.List;

/**
 * 明源-查询对应优惠券信息入参
 *
 * @Author WeiHaiQi
 * @Date 2021-08-20 9:16
 **/
@Data
public class CouponMingyuanRequest extends AbstractBaseConvertor<CouponMingyuanBo> implements Serializable {

    /**
     * 券id json格式
     */
    private String list;

    @Override
    public CouponMingyuanBo convert() {
        CouponMingyuanBo bo = new CouponMingyuanBo();
        List<CouponMingyuanBo> list = JSON.parseArray(this.getList(), CouponMingyuanBo.class);
        if (CollectionUtils.isNotEmpty(list)){
            bo.setCouponIds(list.get(0).getCouponIds());
        }
        return bo;
    }
}
