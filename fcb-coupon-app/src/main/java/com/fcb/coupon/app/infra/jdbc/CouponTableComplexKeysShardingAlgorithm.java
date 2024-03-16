package com.fcb.coupon.app.infra.jdbc;

import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingValue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * @author 唐陆军
 * @Description 优惠券表拆分策略
 * @createTime 2021年06月10日 18:01:00
 */
public class CouponTableComplexKeysShardingAlgorithm implements ComplexKeysShardingAlgorithm<Long> {

    /*
     * 拆分表的数量
     */
    private final int TABLE_NUM = 32;

    /**
     * @param collection               当前数据源的可用表
     * @param complexKeysShardingValue
     * @return
     */
    @Override
    public Collection<String> doSharding(Collection collection, ComplexKeysShardingValue complexKeysShardingValue) {

        Map<String, Collection<Long>> shardingValuesMap = complexKeysShardingValue.getColumnNameAndShardingValuesMap();

        Collection<Long> shardingValues = shardingValuesMap.get("id");

        Collection<String> actualTables = new HashSet<>();

        for (Long id : shardingValues) {
            String actualTable = complexKeysShardingValue.getLogicTableName() + String.format("%03d", (id % TABLE_NUM) + 1);
            //这里一定要判断在这个数据源的表
            if (collection.contains(actualTable)) {
                actualTables.add(actualTable);
            }
        }

        return actualTables;
    }
}

