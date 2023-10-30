package com.zhubin.commonutils.redis.lock.impl;

import com.zhubin.commonutils.redis.lock.LockAction;
import com.zhubin.commonutils.redis.lock.RedisKeyLockUtils;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * @author zhubin
 * @date 2022/3/24
 * @description 默认实现锁动作
 */
@Slf4j
public class DefaultLockActionRedisImpl implements LockAction {

    @Resource
    private RedisKeyLockUtils redisKeyLockUtils;

    @Override
    public boolean tryLock(String key, String value, long expireTime) {

        return redisKeyLockUtils.tryLock(key, value, expireTime);
    }

    @Override
    public boolean releaseLock(String key, String value) {

        return redisKeyLockUtils.releaseLuaLock(key, value);
    }

}
