package com.fcb.coupon.backend.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * YHB
 * @param <T>
 */
@ApiModel(value = "ouser通用传入dto")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OuserInputDto<T> implements Serializable {
    private static final long serialVersionUID = 1465394108883262896L;
    /**
     * 操作人ID
     **/
    @ApiModelProperty(value = "操作人ID")
    private Long userid;

    /**
     * 操作人姓名
     **/
    @ApiModelProperty(value = "操作人姓名")
    private String userName;

    /**
     * 操作人IP
     **/
    @ApiModelProperty(value = "操作人IP")
    private String userIp;

    /**
     * 请求链路Ticket
     **/
    @ApiModelProperty(value = "请求链路Ticket")
    private String traceTicket;

    /**
     * 操作人MAC地址
     **/
    @ApiModelProperty(value = "操作人MAC地址")
    private String mac;

    /**
     * 服务器ip
     **/
    @ApiModelProperty(value = "服务器ip")
    private String serverIp;

    /**
     * soa客户端ip
     */
    @ApiModelProperty(value = "soa客户端ip")
    private String clientIp;

    /**
     * 业务参数
     **/
    @ApiModelProperty(value = "核心调用数据")
    private T data;

    /**
     * 公司id
     **/
    @ApiModelProperty(value = "公司id")
    private Long companyId;

    /**
     * 商家id
     **/
    @ApiModelProperty(value = "商家id")
    private Long merchantId;


    /**
     * 商家权限id集合
     **/
    @ApiModelProperty(value = "商家权限id集合")
    private List<Long> merchantIds = new ArrayList<>();

    /**
     * 用户类型
     **/
    @ApiModelProperty(value = "用户类型")
    private Integer userType;

    /**
     * 个性化定位参数
     **/
    @ApiModelProperty(value = "个性化定位参数")
    private Map<String, String> sArgs = new HashMap<>();


}