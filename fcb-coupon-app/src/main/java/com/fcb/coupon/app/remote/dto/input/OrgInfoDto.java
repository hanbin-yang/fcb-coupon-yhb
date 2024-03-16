package com.fcb.coupon.app.remote.dto.input;


import com.fcb.coupon.app.remote.dto.Pagination;
import lombok.Data;

import java.util.List;

/**
 * 查询组织请求入参
 *
 * @Author Weihaiqi
 * @Date 2021-06-16 20:23
 **/
@Data
public class OrgInfoDto extends Pagination {

    private List<Long> ids;
    //公司ID
    private Long companyId;

    private String orgName;

    private Integer orgInfoType;

    private String orgCode;

    private List<Integer> orgInfoTypes;

    /**
     * 是否查询全部楼盘包括未上线的，默认只是查询上线的
     */
    private Boolean queryIsAll;

    /**
     * 楼盘上线类型  B端: BUILD_ONLINE_STATUS
     * C端: CPOINT_BUILD_ONLINE_STATUS
     * 机构端: ORG_POINT_BUILD_ONLINE_STATUS
     * 传入多个是并且关系只取并集查询上线的楼盘数据
     */
    private List<String> storePortTypes;

}
