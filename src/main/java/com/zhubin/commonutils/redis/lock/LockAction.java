package com.zhubin.commonutils.redis.lock;

/**
 * @author zhubin
 * @date 2023/10/30
 * @description 锁的操作
 */
public interface LockAction {

    /**
     * 尝试获取到锁
     * @param key 锁的key
     * @param value 锁的秘钥
     * @param expireTime 锁的过期时间
     * @return 是否获取到了锁
     */
    boolean tryLock(String key, String value, long expireTime);

    /**
     * 释放锁
     *
     * @param key   锁的key
     * @param value 锁的秘钥
     * @return
     */
    boolean releaseLock(String key, String value);

}
