package com.fcb.coupon.backend.remote.dto.input;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月24日 19:00:00
 */
@Data
public class BrokerIdInput implements Serializable {

    private List<Long> brokerIds;
}
