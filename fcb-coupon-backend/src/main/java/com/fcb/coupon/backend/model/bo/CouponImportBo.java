package com.fcb.coupon.backend.model.bo;

import cn.hutool.core.bean.BeanUtil;
import com.fcb.coupon.backend.model.param.request.CouponThirdImportRequest;
import com.fcb.coupon.common.dto.AbstractBaseConvertor;
import com.fcb.coupon.common.dto.AuthorityHolder;
import com.fcb.coupon.common.dto.UserInfo;
import com.fcb.coupon.common.excel.bean.SheetParseResult;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月18日 16:29:00
 */
@Data
public class CouponImportBo {
    private Long themeId;

    private List<CouponThirdImportRequest> thirdImportRequestList;

    /**
     * 登录用户信息
     */
    private Long userId;
    private String username;

}
