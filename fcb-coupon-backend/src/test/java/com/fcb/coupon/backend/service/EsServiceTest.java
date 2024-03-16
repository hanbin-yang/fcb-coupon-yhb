package com.fcb.coupon.backend.service;

import com.fcb.coupon.BaseTest;
import com.fcb.coupon.backend.elasticsearch.CouponEsDoc;
import com.fcb.coupon.backend.service.CouponEsDocService;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import javax.annotation.Resource;

/**
 * TODO
 *
 * @Author Weihq
 * @Date 2021-06-16 11:28
 **/
public class EsServiceTest extends BaseTest{

    @Resource
    private CouponEsDocService couponEsDocService;

    @Test
    public void testEs() {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//        //券ID
//        if(condition.getId() != null){
//            boolQueryBuilder.filter(QueryBuilders.termQuery(CouponEsDoc.ID, condition.getId()));
//        }

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(boolQueryBuilder);

        // 当前页码
        int current = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(current, pageSize);
        queryBuilder.withPageable(pageable);

        Page<CouponEsDoc> pageResponse = couponEsDocService.searchPage(queryBuilder);

        System.out.println(pageResponse);
    }
}
