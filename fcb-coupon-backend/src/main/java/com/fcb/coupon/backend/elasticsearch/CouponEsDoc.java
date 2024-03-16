package com.fcb.coupon.backend.elasticsearch;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.io.Serializable;
import java.util.Date;


/**
 * Coupon的ES对象
 */
@Data
@Document(indexName = "fcb-coupon-data-back", type = "coupon")
@NoArgsConstructor
@AllArgsConstructor
public class CouponEsDoc implements Serializable {
    @Id
    private Long id;

    @Field(type = FieldType.Integer)
    private Integer versionNo;

    @Field(type = FieldType.Long)
    private Long couponThemeId;

    @Field(type = FieldType.Keyword)
    private String couponCode;

    @Field(type = FieldType.Integer)
    private Integer couponType;

    @MultiField(mainField = @Field(type = FieldType.Text), otherFields = {
            @InnerField(suffix = "keyword", type = FieldType.Keyword),
            @InnerField(suffix = "ik_max_word", type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_max_word")
    })
    private String themeTitle;

    @Field(type = FieldType.Date, pattern = "yyyy-MM-dd HH:mm:ss", format = DateFormat.custom)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @Field(type = FieldType.Date, pattern = "yyyy-MM-dd HH:mm:ss", format = DateFormat.custom)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    @Field(type = FieldType.Date, pattern = "yyyy-MM-dd HH:mm:ss", format = DateFormat.custom)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date bindTime;

    @Field(type = FieldType.Keyword)
    private String bindTel;

    @Field(type = FieldType.Integer)
    private Integer userType;

    @Field(type = FieldType.Keyword)
    private String userId;

    @Field(type = FieldType.Integer)
    private Integer status;

    @Field(type = FieldType.Integer)
    private Integer source;

    @Field(type = FieldType.Keyword)
    private String sourceId;


    @Field(type = FieldType.Date, pattern = "yyyy-MM-dd HH:mm:ss", format = DateFormat.custom)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Field(type = FieldType.Date, pattern = "yyyy-MM-dd HH:mm:ss", format = DateFormat.custom)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    public static final String ID = "id";
    public static final String COUPON_THEME_ID = "couponThemeId";
    public static final String COUPON_CODE = "couponCode";
    public static final String STATUS = "status";
    public static final String USER_ID = "userId";
    public static final String BIND_TEL = "bindTel";
    public static final String USER_TYPE = "userType";
    public static final String COUPON_DISCOUNT_TYPE = "couponDiscountType";
    public static final String END_TIME = "endTime";
    public static final String UPDATE_TIME = "updateTime";
    public static final String BIND_TIME = "bindTime";
    public static final String CREATE_TIME = "createTime";
    public static final String START_TIME = "startTime";
    public static final String RECEIVE_USER_TYPE = "receiveUserType";
    public static final String SOURCE = "source";
    public static final String THEME_TITLE = "themeTitle";
    public static final String SOURCE_ID = "sourceId";
    public static final String GIVE_USER_MOBILE = "giveUserMobile";
    public static final String COUPON_TYPE = "couponType";
    public static final String THEME_TYPE = "themeType";
    public static final String USED_TIME = "usedTime";
    public static final String RECEIVE_USER_MOBILE = "receiveUserMobile";

    public static final String THEME_TITLE_KEYWORD = "themeTitle.keyword";
}
