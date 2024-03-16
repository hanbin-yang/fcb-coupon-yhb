package com.fcb.coupon.backend.model.param.request;

import cn.hutool.core.bean.BeanUtil;
import com.fcb.coupon.backend.model.bo.AsyncTaskListBo;
import com.fcb.coupon.common.dto.AbstractBaseConvertor;
import com.fcb.coupon.common.dto.AuthorityHolder;
import com.fcb.coupon.common.dto.UserInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * 后台管理->营销中心->优惠券管理->券核销->券核销列表
 * @author mashiqiong
 * @date 2021-6-28 21:23
 */
@Data
public class AsyncTaskListRequest extends AbstractBaseConvertor<AsyncTaskListBo> implements Serializable {
    private static final long serialVersionUID = 2088486608316275708L;
    @Override
    public AsyncTaskListBo convert() {
        AsyncTaskListBo bo = new AsyncTaskListBo();
        BeanUtil.copyProperties(this, bo);

        //设置登录用户信息
        UserInfo userInfo = AuthorityHolder.AuthorityThreadLocal.get().getUserInfo();
        if (Objects.nonNull(userInfo)) {
            bo.loadUserInfo(userInfo);
        }
        return bo;
    }
}
