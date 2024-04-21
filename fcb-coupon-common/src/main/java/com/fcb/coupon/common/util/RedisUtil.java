package com.fcb.coupon.common.util;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.fcb.coupon.common.dto.RedisLockResult;
import com.fcb.coupon.common.util.function.VoidSupplier;
import org.apache.commons.lang3.RandomUtils;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.LongCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author YangHanBin
 *
 */
public class RedisUtil {
    private static final Logger logger = LoggerFactory.getLogger(RedisUtil.class);

    private static final RedissonClient redissonClient = SpringBeanFactory.getBean(RedissonClient.class);

    // 自增主键 key
    private static final String GENERATE_ID_KEY = "coupon:generateId:";

    /**
     * 自带锁续期
     * @param keyName    redis key
     * @param waitSeconds 等待秒数 0不等待
     * @param supplier   业务代码提供者
     * @param <V>        返回类型
     * @return 返回RedisLockResult包装对象 -> obj:业务代码返回对象
     */
    public static <V> RedisLockResult<V> executeTryLock(String keyName, long waitSeconds, Supplier<V> supplier) {
        return executeTryLock(keyName, waitSeconds, -1, TimeUnit.SECONDS, supplier);
    }

    public static RedisLockResult<Void> executeTryLock(String keyName, long waitSeconds, VoidSupplier supplier) {
        return executeTryLock(keyName, waitSeconds, -1, TimeUnit.SECONDS, supplier);
    }

    /**
     * 不带锁续期，需要指定 expireSeconds
     * @param keyName   redis key
     * @param waitSeconds  锁最大等待时间 秒
     * @param expireSeconds  redis过期时间 秒
     * @param supplier  supplier
     * @param <V> 返回类型
     * @return 返回RedisLockResult包装对象 -> obj:业务代码返回对象
     */
    public static <V> RedisLockResult<V> executeTryLock(String keyName, long waitSeconds, long expireSeconds, Supplier<V> supplier) {
        return executeTryLock(keyName, waitSeconds, expireSeconds, TimeUnit.SECONDS, supplier);
    }

    public static RedisLockResult<Void> executeTryLock(String keyName, long waitSeconds, long expireSeconds, VoidSupplier supplier) {
        return executeTryLock(keyName, waitSeconds, expireSeconds, TimeUnit.SECONDS, supplier);
    }

    /**
     * @param keyName    redis key
     * @param waitTime   等待时间 0不等待
     * @param expireTime redis过期时间
     * @param timeUnit   时间单位
     * @param supplier   业务代码提供者
     * @param <V>        返回类型
     * @return 返回RedisLockResult包装对象 -> obj:业务代码返回对象
     */
    public static <V> RedisLockResult<V> executeTryLock(String keyName, long waitTime, long expireTime, TimeUnit timeUnit, Supplier<V> supplier) {
        return doExecuteTryLock(keyName, waitTime, expireTime, timeUnit, supplier);
    }
    public static RedisLockResult<Void> executeTryLock(String keyName, long waitTime, long expireTime, TimeUnit timeUnit, VoidSupplier supplier) {
        return doExecuteTryLock(keyName, waitTime, expireTime, timeUnit, supplier);
    }

    private static <V> RedisLockResult<V> doExecuteTryLock(String keyName, long waitTime, long expireTime, TimeUnit timeUnit, Object supplier) {
        RLock lock = redissonClient.getLock(keyName);
        // 上锁
        boolean lockFlag;
        try {
            lockFlag = lock.tryLock(waitTime, expireTime, timeUnit);
        } catch (Exception e) {
            logger.error("尝试获取redis锁异常：message={}, keyName={}", e.getMessage(), keyName, e);
            throw new RuntimeException("尝试获取redis锁异常, keyName=" + keyName, e);
        }

        if (!lockFlag) {
            return RedisLockResult.fail();
        }

        StopWatch stopWatch = new StopWatch("executeTryLock-keyName");
        try {
            stopWatch.start(keyName);
            if (supplier instanceof Supplier){
                @SuppressWarnings("unchecked")
                V result = ((Supplier<V>) supplier).get();
                return RedisLockResult.success(result);
            }
            ((VoidSupplier) supplier).get();
        } finally {
            try {
                lock.unlock();
            } catch (Exception e) {
                stopWatch.stop();
                logger.error(stopWatch.prettyPrint());
                logger.error("RedisUtil#executeTryLock解锁失败", e);
            }
        }

        return RedisLockResult.success(null);
    }

    /**
     * @param keyName redis key
     * @param expireTime redis失效时间
     * @param timeUnit 失效时间单位
     * @param supplier java supplier
     * @param <V> 业务代码返回值类型
     * @return 业务代码返回类型
     */
    public static <V> V executeLock(String keyName, long expireTime, TimeUnit timeUnit, Supplier<V> supplier) {
        return doExecuteLock(keyName, expireTime, timeUnit, supplier);
    }

    public static void executeLock(String keyName, long expireTime, TimeUnit timeUnit, VoidSupplier supplier) {
        doExecuteLock(keyName, expireTime, timeUnit, supplier);
    }

    private static <V> V doExecuteLock(String keyName, long expireTime, TimeUnit timeUnit, Object supplier) {
        RLock lock = redissonClient.getLock(keyName);
        // 上锁
        try {
            lock.lock(expireTime, timeUnit);
        } catch (Exception e) {
            logger.error("RedisUtil#executeLock获取redis锁异常，lockName={}", keyName, e);
            throw new RuntimeException("RedisUtil#executeLock获取redis锁异常, keyName=" + keyName, e);
        }

        StopWatch stopWatch = new StopWatch("executeLock-keyName");
        try {
            stopWatch.start(keyName);
            // 业务代码
            if (supplier instanceof Supplier){
                @SuppressWarnings("unchecked")
                V result = ((Supplier<V>) supplier).get();
                return result;
            }
            ((VoidSupplier) supplier).get();
        } finally {
            try {
                lock.unlock();
            } catch (Exception e) {
                stopWatch.stop();
                logger.error(stopWatch.prettyPrint());
                logger.error("RedisUtil#executeLock解锁失败: required expireTime={}ms", timeUnit.toMillis(expireTime), e);
            }
        }
        return null;
    }

    /**
     * 生成自增主键
     * @return Long
     */
    public static Long generateId() {
        String day = new SimpleDateFormat("yyMMdd").format(new Date());
        String keyName = GENERATE_ID_KEY + day;
        long incrLong = 0;
        try {
            RAtomicLong rAtomicLong = redissonClient.getAtomicLong(keyName);
            incrLong = rAtomicLong.incrementAndGet();
            // 明天凌晨10-20分钟 随机
            long expireSeconds = tomorrowRandomExpireSeconds(10, 20, TimeUnit.MINUTES);
            rAtomicLong.expire(expireSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("redis生成分布式主键失败 message={}", e.getMessage(), e);
            throw e;
        }
        String incrVal = String.format("%010d", incrLong);
        return Long.parseLong(day + incrVal);
    }

    /**
     * 批量生成自增主键
     * @param count 生成数量
     * @return list 主键升序排列
     */
    public static Queue<Long> generateIds(int count) {
        String day = new SimpleDateFormat("yyMMdd").format(new Date());
        String keyName = GENERATE_ID_KEY + day;
        long incrLong = 0;
        try {
            RAtomicLong rAtomicLong = redissonClient.getAtomicLong(keyName);
            incrLong = rAtomicLong.addAndGet(count);
            // 明天凌晨10-20分钟 随机
            long expireSeconds = tomorrowRandomExpireSeconds(10, 20, TimeUnit.MINUTES);
            rAtomicLong.expire(expireSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("redis批量生成分布式主键失败 eMessage={}", e.getMessage(), e);
            throw e;
        }
        String incrVal = String.format("%010d", incrLong - count + 1);
        Queue<Long> ids = new LinkedList<>();
        long baseLong = Long.parseLong(day + incrVal);
        for (int i = 0; i < count; i++) {
            ids.add(baseLong + i);
        }
        return ids;
    }

    /**
     * 获取一个随机的过期时间（秒）
     * @param min 最小的时间数
     * @param max 最大的时间数
     * @param timeUnit  入参min和max的时间单位
     * @return 随机秒数
     */
    public static long randomExpireSeconds(long min, long max, TimeUnit timeUnit) {
        return RandomUtils.nextLong(timeUnit.toSeconds(min), timeUnit.toSeconds(max));
    }

    /**
     * 获取明天指定时间段的随机过期时间
     * @param min 最小的时间数
     * @param max 最大的时间数
     * @param timeUnit 入参min和max的时间单位
     * @return 随机秒数
     */
    public static long tomorrowRandomExpireSeconds(long min, long max, TimeUnit timeUnit) {
        // 次日凌晨偏移当前时间的秒数
        long tomorrowOffsetNowSeconds = DateUtil.between(DateTime.now(), DateUtil.beginOfDay(DateUtil.tomorrow()), DateUnit.SECOND);
        // += 指定时间段的随机时间
        tomorrowOffsetNowSeconds += randomExpireSeconds(min, max, timeUnit);
        return tomorrowOffsetNowSeconds;
    }

    /**
     * 设置指定日期的过期时间
     * @param expireDate 指定的日期
     * @return 秒
     */
    public static long specifiedDateExpireSeconds(Date expireDate) {
        DateTime beginDate = DateTime.now();
        if (beginDate.after(expireDate)) {
            throw new IllegalArgumentException("expireDate不能早于当前时间 expireDate=" + DateUtil.format(expireDate, DatePattern.NORM_DATETIME_PATTERN));
        }
        return DateUtil.between(beginDate, expireDate, DateUnit.SECOND);
    }

    private static final String LUA_LOCK_SCRIPT = "if redis.call('setnx', KEYS[1], ARGV[1]) == 1 then return redis.call('expire', KEYS[1], ARGV[2]) else return 0 end";

    private static final String LUA_UNLOCK_SCRIPT = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";

    public static boolean tryLock(String lockKey, String lockValue, Integer expireSeconds) {
        return redissonClient.getScript(LongCodec.INSTANCE).eval(RScript.Mode.READ_WRITE, LUA_LOCK_SCRIPT, RScript.ReturnType.BOOLEAN, Collections.singletonList(lockKey), lockValue, expireSeconds);
    }

    public static boolean tryUnLock(String lockKey, String lockValue) {
        return redissonClient.getScript(LongCodec.INSTANCE).eval(RScript.Mode.READ_WRITE, LUA_UNLOCK_SCRIPT, RScript.ReturnType.BOOLEAN, Collections.singletonList(lockKey), lockValue);
    }
}
