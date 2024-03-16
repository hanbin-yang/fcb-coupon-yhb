package com.fcb.coupon.app.infra.jdbc;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.fcb.coupon.common.util.RedisUtil;
import org.springframework.stereotype.Component;

/**
 * @author 唐陆军
 * @Description 主键ID生成器
 * @createTime 2021年06月11日 15:22:00
 */
@Component
public class PrimaryKeyIdGenerator implements IdentifierGenerator {

    @Override
    public Long nextId(Object entity) {
        return RedisUtil.generateId();
    }
}