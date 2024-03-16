package com.fcb.coupon.backend.model.query;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.core.toolkit.support.SerializedLambda;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

/**
 * Class属性名称选择器
 * @author YangHanBin
 * @date 2021-09-02 20:39
 */
@Slf4j
public class LambdaFieldNameSelector<T> {
    @Getter
    private final Set<String> fieldNames = new HashSet<>();

    public LambdaFieldNameSelector() {
    }

    public LambdaFieldNameSelector(Class<T> cls) {

    }

    public final LambdaFieldNameSelector<T> select(SFunction<T, ?> column) {

        SerializedLambda lambda = LambdaUtils.resolve(column);
        String methodName = lambda.getImplMethodName();
        if (methodName.startsWith("get")) {
            this.fieldNames.add(StrUtil.lowerFirst(methodName.substring(3)));
        } else if (methodName.startsWith("is")) {
            this.fieldNames.add(StrUtil.lowerFirst(methodName.substring(2)));
        } else {
            log.warn("LambdaFieldSelectWrapper methodName=[{}] can not add.", methodName);
        }
        return this;
    }

    public boolean add(String fieldName) {
        return this.fieldNames.add(fieldName);
    }

    public boolean addAll(Set<String> fieldNames) {
        return this.fieldNames.addAll(fieldNames);
    }
}
