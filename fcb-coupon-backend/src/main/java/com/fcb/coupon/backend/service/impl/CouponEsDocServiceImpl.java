package com.fcb.coupon.backend.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fcb.coupon.backend.elasticsearch.CouponEsDoc;
import com.fcb.coupon.backend.elasticsearch.repository.CouponDocRepository;
import com.fcb.coupon.backend.service.CouponEsDocService;
import com.fcb.coupon.common.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.UpdateByQueryAction;
import org.elasticsearch.index.reindex.UpdateByQueryRequestBuilder;
import org.elasticsearch.script.Script;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.elasticsearch.core.query.UpdateQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 券ES的Service实现类
 */
@Slf4j
@Service
public class CouponEsDocServiceImpl implements CouponEsDocService {

    @Autowired
    protected CouponDocRepository couponDocRepository;
    @Autowired
    private ElasticsearchTemplate esTemplate;
    private String COUPON_INDEX_NAME = "fcb-coupon-data-back";

    @Override
    public void save(CouponEsDoc couponEsDoc) {
        couponDocRepository.save(couponEsDoc);
    }

    @Override
    public void saveBatch(List<CouponEsDoc> couponEsDocs) {
        couponDocRepository.saveAll(couponEsDocs);
    }

    public void saveOrUpdateByVersion(CouponEsDoc couponEsDoc) {
        CouponEsDoc couponEsDocOld = couponDocRepository.findById(couponEsDoc.getId()).orElse(null);
        //保存
        if (couponEsDocOld == null) {
            save(couponEsDoc);
            return;
        }
        if (couponEsDocOld.getVersionNo() == null) {
            couponEsDocOld.setVersionNo(1);
        }
        //比较版本号
        if (couponEsDocOld.getVersionNo() > couponEsDoc.getVersionNo()) {
            //版本比较旧
            return;
        }

        save(couponEsDoc);
    }

    @Override
    public void saveOrUpdate(CouponEsDoc couponEsDoc) {
        CouponEsDoc couponEsDocOld = couponDocRepository.findById(couponEsDoc.getId()).orElse(null);
        if (couponEsDocOld == null) {//保存
            save(couponEsDoc);
            return;
        }

        //更新
        JSONObject objOld = (JSONObject) JSONObject.toJSON(couponEsDocOld);
        String docNewStr = JSON.toJSONString(couponEsDoc);
        // 新值覆盖，null值不覆盖
        objOld.putAll((JSONObject) JSON.parse(docNewStr));
        couponEsDoc = JSONObject.toJavaObject(objOld, CouponEsDoc.class);
        save(couponEsDoc);
    }

    @Override
    public void updateById(CouponEsDoc couponEsDoc) {
        Optional<CouponEsDoc> couponEsDocOld = couponDocRepository.findById(couponEsDoc.getId());
        JSONObject objOld = (JSONObject) JSONObject.toJSON(couponEsDocOld);
        String docNewStr = JSON.toJSONString(couponEsDoc);
        // 新值覆盖，null值不覆盖
        objOld.putAll((JSONObject) JSON.parse(docNewStr));
        couponEsDoc = JSONObject.toJavaObject(objOld, CouponEsDoc.class);
        save(couponEsDoc);
    }

    /**
     * 根据couponThemeId更新字段
     *
     * @param couponThemeId
     * @param couponEsDoc
     * @return
     */
    @Override
    public long updateSelectedFieldsByCouponThemeId(Long couponThemeId, CouponEsDoc couponEsDoc) {
        log.info("updateFieldsByCouponThemeId根据couponThemeId更新couponEs start: couponEsDoc={}, couponThemeId={}", JSON.toJSONString(couponEsDoc), couponThemeId);

        StringBuilder sb = new StringBuilder();
        if (couponEsDoc.getEndTime() != null) {
            String endTimeStr = DateUtils.parseDateToString(couponEsDoc.getEndTime(), DateUtils.FORMAT_YYYY_MM_DD_HH_MM_SS);
            sb.append("ctx._source['").append(CouponEsDoc.END_TIME).append("']='").append(endTimeStr).append("';");
        }

        String nowTimeStr = DateUtils.parseDateToString(new Date(), DateUtils.FORMAT_YYYY_MM_DD_HH_MM_SS);
        sb.append("ctx._source['").append(CouponEsDoc.UPDATE_TIME).append("']='").append(nowTimeStr).append("';");

        Script scriptStr = new Script(sb.toString());
        UpdateByQueryRequestBuilder updateByQuery = UpdateByQueryAction.INSTANCE.newRequestBuilder(esTemplate.getClient());
        updateByQuery.source(COUPON_INDEX_NAME)
                .filter(QueryBuilders.termQuery(CouponEsDoc.COUPON_THEME_ID, String.valueOf(couponThemeId)))
                .script(scriptStr);

        long time = System.currentTimeMillis();
        BulkByScrollResponse response = updateByQuery.get();
        long updateTotal = response.getUpdated();
        time = System.currentTimeMillis() - time;
        log.info("updateFieldsByCouponThemeId根据couponThemeId更新couponEs end: couponEsDoc={}, couponThemeId={}, updateTotal={}, 耗时{}s", JSON.toJSONString(couponEsDoc), couponThemeId, updateTotal, time / 1000);
        return response.getUpdated();
    }

    @Override
    public void updateBatch(List<CouponEsDoc> list) {
        List<UpdateQuery> updateList = new ArrayList<>();
        for (CouponEsDoc e : list) {
            IndexRequest indexRequest = new IndexRequest(COUPON_INDEX_NAME, "coupon");
            Map<String, Object> map = new HashMap<>();
            if (e.getStatus() != null) {
                map.put(CouponEsDoc.STATUS, e.getStatus());
            }
            if (e.getBindTel() != null) {
                map.put(CouponEsDoc.BIND_TEL, e.getBindTel());
            }
            if (e.getUpdateTime() != null) {
                map.put(CouponEsDoc.UPDATE_TIME, DateUtil.format(e.getUpdateTime(), "yyyy-MM-dd HH:mm:ss"));
            }

            indexRequest.source(map);
            UpdateQuery updateQuery = new UpdateQueryBuilder()
                    .withIndexName(COUPON_INDEX_NAME)
                    .withType("coupon")
                    .withId(String.valueOf(e.getId()))
                    .withClass(CouponEsDoc.class)
                    .withIndexRequest(indexRequest)
                    .withDoUpsert(true)
                    .build();
            updateList.add(updateQuery);
        }

        try {
            esTemplate.bulkUpdate(updateList);
            esTemplate.refresh(CouponEsDoc.class);
        } catch (Exception e) {
            log.error("elasticsearch updateBatch error: e={}, input={}", e.getMessage(), JSON.toJSONString(list), e);
            throw e;
        }
    }

    @Override
    public Page<CouponEsDoc> searchPage(NativeSearchQueryBuilder queryBuilder) {
        log.info("searchCouponPage 搜索查询DSL:【{}】", queryBuilder.build().getQuery());
        Page<CouponEsDoc> result = couponDocRepository.search(queryBuilder.build());
        return result;
    }
}
