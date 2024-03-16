package com.fcb.coupon.backend.remote.dto.input;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class OrgIdsInput implements Serializable {

    private List<Long> ids;
}
