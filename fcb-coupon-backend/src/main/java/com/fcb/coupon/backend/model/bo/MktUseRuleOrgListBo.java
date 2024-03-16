package com.fcb.coupon.backend.model.bo;

import com.fcb.coupon.common.dto.UserInfo;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * @author mashiqiong
 * @date 2021-6-27 16:30
 */
@Data
@ApiModel(description = "管理后台->优惠券活动列表->添加后条件查询适用组织 入参")
public class MktUseRuleOrgListBo {

    /**
     * 商家ID
     */
    private Long merchantId;
    /**
     * 集团ID
     */
    private Long groupId;
    /**
     * 商家名称
     */
    private String merchantName;
    /**
     * 券活动ID
     */
    private Long themeRef;
    /**
     * 省代码
     */
    private String provinceCode;
    /**
     * 市代码
     */
    private String cityCode;
    /**
     * 类型
     */
    private Integer refType;
    /**
     * 适用范围ID
     */
    private Integer merchantType;
    /**
     * 所属店铺ids（针对店铺）
     */
    private List<Long> storeIds;
    /**
     * 需要查询的范围限制ids
     */
    private List<Long> merchantIds;
    /**
     * 需要查询的范围限制ids
     */
    private List<Long> belongsToGroupIds;
    /**
     * 所属商家ids（针对店铺）
     */
    private List<Long> belongsToMerchantIds;
    /**
     * 店铺编码
     */
    private String buildCode;
    /**
     * 商家代码
     */
    private String merchantCode;

    /**
     * 当前登录用户id
     */
    private Long userId;

    /**
     * 当前登录用户组织级别
     */
    private String userOrgLevelCode;

    /**
     * 当前页码
     */
    private Integer currentPage;

    /**
     * 页面pageSize
     */
    private Integer itemsPerPage;

    public void loadUserInfo(UserInfo userInfo) {
        this.userId = userInfo.getId();
        this.userOrgLevelCode = userInfo.getOrgLevelCode();
    }

    /**
     *
     * @description <pre>
     * 根据页码和每页记录数获取页起始记录
     * </pre>
     * @return
     */
    public int getStartItem() {

        int start = (currentPage - 1) * itemsPerPage;
        if (start < 0) {
            start = 0;
        }
        return start;
    }
}
