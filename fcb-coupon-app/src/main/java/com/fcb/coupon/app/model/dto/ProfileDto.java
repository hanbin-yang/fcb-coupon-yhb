package com.fcb.coupon.app.model.dto;

import lombok.Data;

@Data
public class ProfileDto {
	//经纪人ID
	private String broker_id;
	//用户唯一id
	private String union_id;
	//手机号
	private String phone;
	//真实中文姓名
	private String name;
	//昵称
	private String  nickname;
	//性别
	private Integer  gender;
	//头像"http//:ip:port/pic/xxx.jpg"
	private String  avatar;
}
