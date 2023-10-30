package com.zhubin.commonutils.redis;

import com.zhubin.commonutils.redis.function.ConsumerNoParams;
import com.zhubin.commonutils.redis.lock.LockAction;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.function.Supplier;

/**
 * @author zhubin
 * @date 2023/10/30
 * @description 分布式锁
 */
public class DistributeLockUtil implements ApplicationContextAware {

    private static LockAction lockAction;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        lockAction = applicationContext.getBean(LockAction.class);
    }

    /**
     * 构建有返回值的自旋锁对象，执行函数并返回
     * @param supplier 有返回值的函数
     * @param lockKey 锁的key
     * @param expireTime 锁的失效时间，单位为毫秒
     * @param exceptionMsg 异常提示
     * @param waitingTime 自旋等待时间，单位为毫秒
     * @param maxWaitingTime 最大自旋等待时间，单位为毫秒
     * @param <T> 返回结果泛型
     * @return 有返回值的自旋锁对象
     */
    public static <T> T supplySpinLock(Supplier<T> supplier, String lockKey, Long expireTime, String exceptionMsg,
                                       Long waitingTime, Long maxWaitingTime) {

        DistributeLockContext lockContext = DistributeLockContext.buildLockContext(
                lockKey, expireTime, exceptionMsg, waitingTime, true, maxWaitingTime, lockAction);
        return lockContext.execute(supplier);
    }

    /**
     * 构建有返回值的排他锁对象，执行函数并返回
     * @param supplier 有返回值的函数
     * @param lockKey 锁的key
     * @param expireTime 锁的失效时间，单位为毫秒
     * @param exceptionMsg 异常提示
     * @param <T> 返回结果泛型
     * @return 有返回值的排他锁对象
     */
    public static <T> T supplyExclusiveLock(Supplier<T> supplier, String lockKey, Long expireTime, String exceptionMsg) {

        DistributeLockContext lockContext = DistributeLockContext.buildLockContext(
                lockKey, expireTime, exceptionMsg, null, false, null, lockAction);
        return lockContext.execute(supplier);
    }

    /**
     * 构建无返回值的自旋锁对象，执行函数
     * @param consumer 消费函数
     * @param lockKey 锁的key
     * @param expireTime 锁的失效时间，单位为毫秒
     * @param exceptionMsg 异常提示
     * @param waitingTime 自旋等待时间，单位为毫秒
     * @param maxWaitingTime 最大自旋等待时间，单位为毫秒
     */
    public static void runSpinLock(ConsumerNoParams consumer, String lockKey, Long expireTime, String exceptionMsg,
                                   Long waitingTime, Long maxWaitingTime) {

        DistributeLockContext lockContext = DistributeLockContext.buildLockContext(
                lockKey, expireTime, exceptionMsg, waitingTime, true, maxWaitingTime, lockAction);
        lockContext.execute(consumer);
    }

    /**
     * 构建无返回值的排他锁对象，执行函数
     * @param consumer 消费函数
     * @param lockKey 锁的key
     * @param expireTime 锁的失效时间，单位为毫秒
     * @param exceptionMsg 异常提示
     */
    public static void runExclusiveLock(ConsumerNoParams consumer, String lockKey, Long expireTime, String exceptionMsg) {

        DistributeLockContext lockContext = DistributeLockContext.buildLockContext(
                lockKey, expireTime, exceptionMsg, null, false, null, lockAction);
        lockContext.execute(consumer);
    }

    /**
     * 构建锁对象，上锁
     * @description 获取到锁对象后，可使用【执行方法、判断是否获取到锁方法、释放锁方法、再次获取锁方法】
     * @param key 锁的key
     * @param expireTime 失效时间，单位为毫秒
     * @param exceptionMsg 异常提示
     * @return 锁对象
     */
    public static DistributeLockContext tryLockExclusive(String key, long expireTime, String exceptionMsg) {

        DistributeLockContext lockContext = DistributeLockContext.buildLockContext(
                key, expireTime, exceptionMsg, null, false, null, lockAction);
        lockContext.tryLock();
        return lockContext;
    }

    /**
     * 构建锁对象，上锁
     * @description 获取到锁对象后，可使用【执行方法、判断是否获取到锁方法、释放锁方法、再次获取锁方法】
     * @param key 锁的key
     * @param expireTime 失效时间，单位为毫秒
     * @return 锁对象
     */
    public static DistributeLockContext tryLockExclusive(String key, long expireTime) {

        DistributeLockContext lockContext = DistributeLockContext.buildLockContext(
                key, expireTime, null, null, false, null, lockAction);
        lockContext.tryLock();
        return lockContext;
    }

    /**
     * 构建锁对象，上锁
     * @description 获取到锁对象后，可使用【执行方法、判断是否获取到锁方法、释放锁方法、再次获取锁方法】
     * @param key 锁的key
     * @param expireTime 失效时间，单位为毫秒
     * @param exceptionMsg 异常提示
     * @param waitingTime 自旋等待时间，单位为毫秒
     * @param maxWaitingTime 最大自旋等待时间，单位为毫秒
     * @return 锁对象
     */
    public static DistributeLockContext tryLockSpin(String key, long expireTime, String exceptionMsg, Long waitingTime, Long maxWaitingTime) {

        DistributeLockContext lockContext = DistributeLockContext.buildLockContext(
                key, expireTime, exceptionMsg, waitingTime, true, maxWaitingTime, lockAction);
        lockContext.tryLock();
        return lockContext;
    }

    /**
     * 构建锁对象，上锁
     * @description 获取到锁对象后，可使用【执行方法、判断是否获取到锁方法、释放锁方法、再次获取锁方法】
     * @param key 锁的key
     * @param expireTime 失效时间，单位为毫秒
     * @param waitingTime 自旋等待时间，单位为毫秒
     * @param maxWaitingTime 最大自旋等待时间，单位为毫秒
     * @return 锁对象
     */
    public static DistributeLockContext tryLockSpin(String key, long expireTime, Long waitingTime, Long maxWaitingTime) {

        DistributeLockContext lockContext = DistributeLockContext.buildLockContext(
                key, expireTime, null, waitingTime, true, maxWaitingTime, lockAction);
        lockContext.tryLock();
        return lockContext;
    }

    /**
     * 上锁
     * @param key 锁的key
     * @param value 锁的value
     * @param expireTime 失效时间，单位为毫秒
     * @deprecated 请使用构建锁对象方法上锁并获取锁对象 DistributeLockContext.tryLock(String key, long expireTime)
     * @return 是否上锁成功
     */
    @Deprecated
    public static boolean tryLock(String key, String value, long expireTime) {

        return lockAction.tryLock(key, value, expireTime);
    }

    /**
     * 释放锁
     * @param key 锁的key
     * @param value 锁的value
     * @deprecated 请构建锁对象后调用锁对象的释放锁方法
     * @return 是否释放锁成功
     */
    @Deprecated
    public static boolean releaseLock(String key, String value) {

        return lockAction.releaseLock(key, value);
    }

}
