package com.fcb.coupon.backend.elasticsearch.repository;

import com.fcb.coupon.backend.elasticsearch.CouponEsDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponDocRepository extends ElasticsearchRepository<CouponEsDoc, Long> {

}