package com.fcb.coupon.backend.model.mongo;

import lombok.Data;
import org.apache.commons.codec.language.bm.RuleType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author 唐陆军
 * @Description mongo的mkt_task_run_node_user表
 * @createTime 2021年08月23日 11:36:00
 */
@Data
@Document(collection = "mkt_task_run_node_user")
public class MktTaskRunNodeUserEntity {

    @Id
    private String mongoId;

    @Field("couponThemeId")
    private Long couponThemeId;

    @Field("taskId")
    private Long taskId;

    @Field("runId")
    private Long runId;

    @Field("nodeId")
    private Long nodeId;

    @Field("userId")
    private Long userId;

    @Field("unionId")
    private String unionId;

    @Field("phone")
    private String phone;

    @Field("retryCount")
    private Integer retryCount;

    @Field("compensationTime")
    private String compensationTime;

    @Field("createTime")
    private String createTime;

    @Field("status")
    private Integer status;

    @Field("message")
    private String message;

    @Field("companyId")
    private Long companyId;

    @Field("source")
    private Integer source;

    @Field("couponJson")
    private String couponJson;

    @Field("sendCouponUserType")
    private Integer sendCouponUserType;

}
