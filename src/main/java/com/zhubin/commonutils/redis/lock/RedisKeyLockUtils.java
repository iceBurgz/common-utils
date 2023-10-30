package com.zhubin.commonutils.redis.lock;


import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


/**
 * @author zhubin
 * @date 2023/10/30
 * @description redis分布式锁
 */
@Slf4j
public class RedisKeyLockUtils {

    private static final String RELEASE_LOCK_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 获取redis锁
     * 注：Redis 2.6.12版本之后整合setnx功能，使用set命令加上NX参数即可实现setnx操作，但旧版本的setnx先上锁再设置过期时间，不是原子性的，需要使用lua脚本保证，防止Redis宕机
     * @param lockKey 锁
     * @param lockValue 值
     * @param expireTime 过期时间
     * @return boolean
     */
    public boolean tryLock(String lockKey, String lockValue, long expireTime) {

        try {
            Boolean setIfAbsent = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, expireTime, TimeUnit.MILLISECONDS);
            return Objects.nonNull(setIfAbsent) && setIfAbsent;
        } catch (Exception e) {
            log.error("获取redis锁异常，错误信息: ", e);
        }
        return false;
    }

    /**
     * 使用lua脚本释放Redis锁
     * @param lockKey 锁
     * @param lockValue 值
     * @return boolean
     */
    public boolean releaseLuaLock(String lockKey, String lockValue) {

        try {
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setResultType(Long.class);
            redisScript.setScriptText(RELEASE_LOCK_SCRIPT);
            Long execute = redisTemplate.execute(redisScript, Collections.singletonList(lockKey), lockValue);
            return Objects.nonNull(execute) && execute > 0;
        } catch (Exception e) {
            log.error("释放redis锁异常，错误信息: ", e);
        }
        return false;
    }

}

