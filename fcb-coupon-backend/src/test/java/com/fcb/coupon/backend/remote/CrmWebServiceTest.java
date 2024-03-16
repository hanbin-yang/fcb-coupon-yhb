package com.fcb.coupon.backend.remote;

import com.fcb.coupon.BaseTest;
import com.fcb.coupon.backend.remote.client.CrmWebService;
import com.fcb.coupon.backend.remote.dto.out.UserActivityDto;
import com.fcb.coupon.common.dto.ResponseDto;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 *
 * @Author WeiHaiQi
 * @Date 2021-06-28 16:16
 **/
public class CrmWebServiceTest extends BaseTest {

    @Resource
    private CrmWebService crmWebService;

    @Test
    public void queryOngoingActivityListTest() {
        List<String> unionIdList = new ArrayList<>();
        unionIdList.add("C21CAF046EFCB4BAD334B8BC7C184709");
        ResponseDto<List<UserActivityDto>> crmResponse = crmWebService.queryOngoingActivityList(unionIdList);

        System.out.println(crmResponse);
    }
}
