package com.fcb.coupon.app.remote.dto.output;

import lombok.Data;

import java.io.Serializable;

@Data
public class StoreOrgInfoOutDto implements Serializable {
    /**库存组织id */
    private Long storeId;
    /**库存组织名称 */
    private String storeName;
    /**库存组织类型 */
    private String storeType;
    /**库存组织名字 */
    private String storeTypeName;
    /**库存组织编码 */
    private String storeCode;
    /**是否用户管理的    1：是 ， 2：否 */
    private String isDefault;
    /**所属商家Id */
    private Long merchantId;
    /**所属商家名称 */
    private String merchantName;
    /**所属商家code */
    private String merchantCode;
    /**店铺详细地址 */
    private String detailAddress;

    /** 楼盘名称首字母*/
    private String nameInitial;
    /**楼盘名称拼音*/
    private String namePinYin;
    /** 楼盘编码 */
    private String buildCode;

    /** B端上线状态，0未上线 1已上线 **/
    private Integer buildOnlineStatus;
    /** C端上线状态，0未上线 1已上线 **/
    private Integer cpointBuildOnlineStatus;
    /** 机构端上线状态，0未上线 1已上线 */
    private Integer orgPointBuildOnlineStatus;
}
