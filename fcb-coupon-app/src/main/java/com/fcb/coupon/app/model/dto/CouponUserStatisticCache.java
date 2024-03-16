package com.fcb.coupon.app.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.stream.Stream;

/**
 * @author YangHanBin
 * @date 2021-08-19 14:28
 */
@Data
public class CouponUserStatisticCache {
    @ApiModelProperty(value = "总领券数量")
    private Integer totalCount;

    @ApiModelProperty(value = "当月领券数量")
    private Integer monthCount;

    @ApiModelProperty(value = "当天领券数量")
    private Integer todayCount;

    @ApiModelProperty(value = "最后领券日期, 时间戳")
    private Long lastReceiveDate;


    /**
     * 字段属性的名称
     */
    public enum FIELDS {
        TOTAL_COUNT("totalCount"),
        MONTH_COUNT("monthCount"),
        TODAY_COUNT("todayCount"),
        LAST_RECEIVE_DATE("lastReceiveDate"),
        ;

        private final String name;

        FIELDS(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static CouponUserStatisticCache.FIELDS of(String fieldName) {
            return Stream.of(values())
                    .filter(bean -> bean.getName().equals(fieldName))
                    .findAny()
                    .orElseThrow(() -> new IllegalArgumentException("CouponUserStatisticCache.FIELDS字段 [fieldName=" + fieldName + "] 不存在"));
        }
    }
}
