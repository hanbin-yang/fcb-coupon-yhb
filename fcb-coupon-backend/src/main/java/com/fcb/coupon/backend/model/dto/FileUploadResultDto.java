package com.fcb.coupon.backend.model.dto;

import com.fcb.coupon.common.enums.AsyncStatusEnum;
import lombok.Builder;
import lombok.Data;

/*
优惠券活动导出结果
 */
@Builder
@Data
public class FileUploadResultDto {

    private String uploadFile;

    private AsyncStatusEnum statusEnum;

    private String errorReason;
}
