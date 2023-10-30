package com.zhubin.commonutils.redis.config;

import com.zhubin.commonutils.redis.DistributeLockUtil;
import com.zhubin.commonutils.redis.aop.DistributeLockAop;
import com.zhubin.commonutils.redis.lock.LockAction;
import com.zhubin.commonutils.redis.lock.RedisKeyLockUtils;
import com.zhubin.commonutils.redis.lock.impl.DefaultLockActionRedisImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhubin
 * @date 2022/3/25
 * @description 分布式锁操作默认实现配置
 */
@Configuration
public class DefaultLockActionConfig {

    @Bean("lockActionImpl")
    public LockAction defaultDistributeLockConfig() {
        return new DefaultLockActionRedisImpl();
    }

    @Bean
    public RedisKeyLockUtils redisKeyLockUtils() {
        return new RedisKeyLockUtils();
    }

    @Bean
    public DistributeLockAop distributeLockAop() {
        return new DistributeLockAop();
    }

    @Bean
    public DistributeLockUtil distributeLockUtil() {
        return new DistributeLockUtil();
    }

}
