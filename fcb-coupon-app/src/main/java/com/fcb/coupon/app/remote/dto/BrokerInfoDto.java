package com.fcb.coupon.app.remote.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BrokerInfoDto {

    @ApiModelProperty("会员ID")
    private Long userId;

    @ApiModelProperty("会员等级")
    private Integer userLevel;

    @ApiModelProperty("用户类型" +
            " 1： 恒大老业主(非员工)\n" +
            "2： 恒大员工(老业主)\n" +
            "3： 恒大员工(非老业主)\n" +
            "4： 房产经纪中介人\n" +
            "5： 拓客员\n" +
            "6： 注册未认证用户\n" +
            "7： 苏宁员工\n" +
            "0：其他\n")
    private Integer userType;

    @ApiModelProperty("认证时间")
    private String applyTime;

    @ApiModelProperty("性别")
    private Integer gender;

    @ApiModelProperty("经纪人姓名")
    private String name;

    @ApiModelProperty("年龄")
    private Integer age;

    @ApiModelProperty("生日")
    private String birthday;

    @ApiModelProperty("注册时间")
    private String createTime;

    @ApiModelProperty("用户常驻地")
    private String gsCity;

    @ApiModelProperty("LBS城市")
    private String lbsCityCode;

    @ApiModelProperty("城市站点")
    private String siteCityCode;

    @ApiModelProperty("手机")
    private String mphone;

    @ApiModelProperty("机构账号")
    private String orgAccount;

    @ApiModelProperty("经纪人类型")
    private String brokerType;

    @ApiModelProperty("经纪人id")
    private String brokerId;

    @ApiModelProperty("是否禁用：0否；1是")
    private Integer isDisabled;

}
