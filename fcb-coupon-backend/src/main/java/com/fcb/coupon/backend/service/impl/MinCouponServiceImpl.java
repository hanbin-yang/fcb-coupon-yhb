package com.fcb.coupon.backend.service.impl;

import com.fcb.coupon.backend.remote.client.CrmWebService;
import com.fcb.coupon.backend.remote.dto.input.BrokerInfoSimpleInputDto;
import com.fcb.coupon.backend.remote.dto.out.ActivityOutDto;
import com.fcb.coupon.backend.remote.dto.out.BrokerInfoSimpleDto;
import com.fcb.coupon.backend.remote.client.BrokerClient;
import com.fcb.coupon.backend.remote.dto.out.UserActivityDto;
import com.fcb.coupon.backend.service.MinCouponService;
import com.fcb.coupon.common.dto.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;
import javax.annotation.Resource;
import java.util.*;

/**
 * 保底券服务实现
 *
 * @Author WeiHaiQi
 * @Date 2021-06-21 20:48
 **/
@Service
@Slf4j
public class MinCouponServiceImpl implements MinCouponService {

    @Resource
    private BrokerClient brokerClient;
    @Resource
    private CrmWebService crmWebService;
    @Resource(name = "kafkaTemplate")
    private KafkaTemplate kafkaTemplate;

    @Override
    public void sendMinCoupon(List<String> mobiles) {
        try {
            List<String> unionIdList = new ArrayList<>();

            BrokerInfoSimpleInputDto param = new BrokerInfoSimpleInputDto();
            param.setPhoneNoList(mobiles);
            ResponseDto<List<BrokerInfoSimpleDto>> brokerResult = brokerClient.getBrokerInfoListByPhones(param);
            List<BrokerInfoSimpleDto> brokerInfoList = brokerResult.getData();

            if (CollectionUtils.isEmpty(brokerInfoList)) {
                log.info("用户信息不存在 mobiles：{}",mobiles);
                return;
            }

            brokerInfoList.forEach(dto -> {
                unionIdList.add(dto.getUnionId());
            });

            if (CollectionUtils.isEmpty(unionIdList)) {
                return;
            }

            ResponseDto<List<UserActivityDto>> crmResponse = crmWebService.queryOngoingActivityList(unionIdList);
            List<UserActivityDto> result = crmResponse.getData();

            if (CollectionUtils.isEmpty(result)) {
                log.info("#用户{}没有可以发放的保底券信息",unionIdList);
                return;
            }

            Map<String,List<ActivityOutDto>> unionIdMap = new HashMap<>();
            result.forEach(v->{
                unionIdMap.put(v.getUnionId(),v.getActivityList());
            });

            if (unionIdMap.size() == 0) {
                return;
            }

            log.info("以下userId发送保底券消息：{}", mobiles);
            unionIdMap.forEach((unionId,activityList)-> {
                //遍历活动获取保底券对应的活动ID
                List<Long> activityIds = new ArrayList<>();
                activityList.forEach(dto->{
                    activityIds.add(dto.getId());
                });

                Map<String, Object> map = new HashMap<>();
                map.put("unionId", unionId);
                map.put("initFlag", false);
                map.put("initiateTime", new Date());
                map.put("activityIds", activityIds);
                log.info("此unionId发送保底券消息：{} 保底券活动id：{}", unionId,JSON.toJSONString(activityIds));
                kafkaTemplate.send("ODY_BAODI_ACTIVITY", JSON.toJSON(map).toString());
                log.info("此unionId发送保底券消息完成：{}", unionId);
            });
        } catch (Exception e) {
            log.error("#保底券活动发券异常 {} {}",mobiles,e);
        }
    }
}
