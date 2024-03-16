package com.fcb.coupon.backend.elasticsearch.helper;

import com.fcb.coupon.common.util.DateUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * ES查询构建器
 *
 * @Author WeiHaiQi
 * @Date 2021-08-04 10:13
 **/
public class QueryBuildHelper {

    /**
     * ES查询构建者
     */
    private NativeSearchQueryBuilder queryBuilder;
    /**
     * ES查询条件构建者
     */
    private ParamBuilder paramBuilder;

    public QueryBuildHelper(){
        queryBuilder = new NativeSearchQueryBuilder();
        paramBuilder = new ParamBuilder();
        withQuery(paramBuilder.getBoolQueryBuilder());
    }

    /**
     * 绑定条件
     * @param boolQuery
     * @return
     */
    public QueryBuildHelper withQuery(BoolQueryBuilder boolQuery) {
        queryBuilder.withQuery(boolQuery);
        return this;
    }

    /**
     * 设置分页
     * @param current       页码
     * @param pageSize      页面大小
     * @return
     */
    public QueryBuildHelper setPage(int current, int pageSize) {
        current = current <= 0 ? 0 : current - 1;
        pageSize = pageSize <= 0 ? 10 : pageSize;
        Pageable pageable = org.springframework.data.domain.PageRequest.of(current, pageSize);
        queryBuilder.withPageable(pageable);
        return this;
    }

    /**
     * 倒序
     * @param field
     * @return
     */
    public NativeSearchQueryBuilder descSort(String field) {
        queryBuilder.withSort(SortBuilders.fieldSort(field).order(SortOrder.DESC));
        return queryBuilder;
    }

    /**
     * 正序
     * @param field
     * @return
     */
    public NativeSearchQueryBuilder ascSort(String field) {
        queryBuilder.withSort(SortBuilders.fieldSort(field).order(SortOrder.ASC));
        return queryBuilder;
    }

    public NativeSearchQueryBuilder getQueryBuilder() {
        return queryBuilder;
    }

    public void setQueryBuilder(NativeSearchQueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    public ParamBuilder getParamBuilder() {
        return paramBuilder;
    }

    public void setParamBuilder(ParamBuilder paramBuilder) {
        this.paramBuilder = paramBuilder;
        withQuery(paramBuilder.boolQueryBuilder);
    }

    /**
     * 查询参数构建
     */
    public class ParamBuilder {

        private BoolQueryBuilder boolQueryBuilder;

        public ParamBuilder() {
            boolQueryBuilder = QueryBuilders.boolQuery();
        }

        /**
         * 包含
         * @param field     ES的字段名
         * @param datas     列表值
         * @return
         */
        public ParamBuilder andIn(String field, List<?> datas) {
            if (CollectionUtils.isNotEmpty(datas)) {
                boolQueryBuilder.filter(QueryBuilders.termsQuery(field,datas));
            }
            return this;
        }

        /**
         * 加前缀模糊
         * @param field     ES的字段名
         * @param content   字符内容
         * @return
         */
        public ParamBuilder andPrefixLike(String field, String content) {
            if (StringUtils.isNotBlank(content)) {
                PrefixQueryBuilder prefixQueryBuilder = QueryBuilders.prefixQuery(field, content);
                boolQueryBuilder.filter(prefixQueryBuilder);
            }
            return this;
        }

        /**
         * 等于
         * @param field     ES的字段名
         * @param val       整数
         * @return
         */
        public ParamBuilder andEq(String field, Integer val) {
            if(Objects.nonNull(val)){
                boolQueryBuilder.filter(QueryBuilders.termQuery(field, val));
            }
            return this;
        }

        /**
         * 等于
         * @param field     ES的字段名
         * @param val       长整数
         * @return
         */
        public ParamBuilder andEq(String field, Long val) {
            if(Objects.nonNull(val)){
                boolQueryBuilder.filter(QueryBuilders.termQuery(field, val));
            }
            return this;
        }

        /**
         * 等于
         * @param field     ES的字段名
         * @param val       字符
         * @return
         */
        public ParamBuilder andEq(String field, String val) {
            if(StringUtils.isNotBlank(val)){
                boolQueryBuilder.filter(QueryBuilders.termQuery(field, val));
            }
            return this;
        }

        /**
         * 大于某时间
         * @param field     ES的字段名
         * @param time      时间
         * @return
         */
        public ParamBuilder andGt(String field, Date time) {
            if(Objects.nonNull(time)){
                String startTime = DateUtils.parseDateToString(time,DateUtils.FORMAT_YYYY_MM_DD_HH_MM_SS);
                boolQueryBuilder.filter(QueryBuilders.rangeQuery(field).gt(startTime));
            }
            return this;
        }

        /**
         * 大于或等于某时间
         * @param field     ES的字段名
         * @param time      时间
         * @return
         */
        public ParamBuilder andGte(String field, Date time) {
            if(Objects.nonNull(time)){
                String startTime = DateUtils.parseDateToString(time,DateUtils.FORMAT_YYYY_MM_DD_HH_MM_SS);
                boolQueryBuilder.filter(QueryBuilders.rangeQuery(field).gte(startTime));
            }
            return this;
        }

        /**
         * 小于某时间
         * @param field     ES的字段名
         * @param time      时间
         * @return
         */
        public ParamBuilder andLt(String field, Date time) {
            if(Objects.nonNull(time)){
                String endTime = DateUtils.parseDateToString(time,DateUtils.FORMAT_YYYY_MM_DD_HH_MM_SS);
                boolQueryBuilder.filter(QueryBuilders.rangeQuery(field).lt(endTime));
            }
            return this;
        }

        /**
         * 小于或等于某时间
         * @param field     ES的字段名
         * @param time      时间
         * @return
         */
        public ParamBuilder andLte(String field, Date time) {
            if(Objects.nonNull(time)){
                String endTime = DateUtils.parseDateToString(time,DateUtils.FORMAT_YYYY_MM_DD_HH_MM_SS);
                boolQueryBuilder.filter(QueryBuilders.rangeQuery(field).lte(endTime));
            }
            return this;
        }

        /**
         * isNull
         * @param field     ES的字段名
         * @return
         */
        public ParamBuilder andIsNull(String field) {
            boolQueryBuilder.mustNot(new ExistsQueryBuilder(field));
            return this;
        }

        /**
         * and (组合条件)
         * @param query 其他组条件
         * @return
         */
        public ParamBuilder andQueryBuilder(BoolQueryBuilder query) {
            boolQueryBuilder.filter(query);
            return this;
        }

        public BoolQueryBuilder getBoolQueryBuilder() {
            return boolQueryBuilder;
        }

        public void setBoolQueryBuilder(BoolQueryBuilder boolQueryBuilder) {
            this.boolQueryBuilder = boolQueryBuilder;
        }
    }
}
