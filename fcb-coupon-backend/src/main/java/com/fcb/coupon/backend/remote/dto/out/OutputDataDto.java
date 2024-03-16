package com.fcb.coupon.backend.remote.dto.out;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 通用传出dto
 *
 * @Author mashiqionbg
 * @Date 2021-08-05 18:51
 **/
@Data
public class OutputDataDto<T> implements Serializable {
    private static final long serialVersionUID = -6487750026490963881L;
    private Integer total;
    private List<T> listObj;


}
