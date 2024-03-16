package com.fcb.coupon.app.model.dto;

import com.fcb.coupon.common.enums.LogOprThemeType;
import com.fcb.coupon.common.enums.LogOprType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 操作日志入参
 * @author YangHanBin
 * @date 2021-06-16 23:07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OprLogDo {
    /**
     * 操作用户id
     */
    private Long oprUserId;
    /**
     * 操作用户名
     */
    private String oprUserName;
    /**
     * 操作描述1
     */
    private String oprContent;
    /**
     * 引用主键id 券主键/券活动主键
     */
    private Long refId;
    /**
     * 操作券/券活动 1:券  2:券活动
     */
    private LogOprThemeType oprThemeType;
    /**
     * 操作类型
     * 1.新建 2.提交审核 3.审核 4.驳回 5.生券 6.编辑 7.复制 8.关闭 9.发券
     * 10.导入券码 11.删除 12.查看 101.作废 102.冻结 103.解冻 104.延期
     */
    private LogOprType oprType;

    /**
     * 其它扩展数据
     */
    private String extData;
}
