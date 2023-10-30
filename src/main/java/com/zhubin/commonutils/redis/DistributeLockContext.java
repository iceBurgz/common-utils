package com.zhubin.commonutils.redis;

import com.zhubin.commonutils.common.utils.SnowflakeIdUtils;
import com.zhubin.commonutils.redis.exception.DistributeLockTimeoutException;
import com.zhubin.commonutils.redis.function.ConsumerNoParams;
import com.zhubin.commonutils.redis.lock.LockAction;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author zhubin
 * @date 2023/10/30
 * @description 分布式锁对象
 */
@Slf4j
public class DistributeLockContext {

    /**
     * 默认获取锁失败的提示文案
     */
    private static final String DEFAULT_EXCEPTION_MSG = "服务繁忙，请稍后再试";

    /**
     * 防呆最大自旋时间
     */
    private static final Long FOOL_PROOF_MAX_WAITING_TIME = 3 * 1000L;

    /**
     * 防呆默认自旋时间
     */
    private static final Long FOOL_PROOF_DEFAULT_WAITING_TIME = 300L;

    /**
     * 锁的秘钥
     */
    private final String secret;

    /**
     * 锁的key
     */
    @Getter
    private final String lockKey;

    /**
     * 锁失效时间，单位为毫秒
     */
    @Getter
    private final Long expireTime;

    /**
     * 异常提示文案
     */
    @Getter
    private final String exceptionMsg;

    /**
     * 状态：是否获取到了锁
     */
    @Getter
    private Boolean gainLock;

    /**
     * 是否开启自旋锁
     */
    @Getter
    private final Boolean spinLock;

    /**
     * 自旋锁每次自旋等待时间，单位为毫秒
     */
    @Getter
    private Long waitingTime;

    /**
     * 最大总计自旋等待时间，单位为毫秒
     */
    @Getter
    private Long maxWaitingTime;

    /**
     * 剩余自旋次数
     */
    @Getter
    private Long spinTimes;

    /**
     * 自旋过的次数
     */
    @Getter
    private Long spunTimes;

    /**
     * 操作锁方法
     */
    private final LockAction lockAction;

    /**
     * 锁对象执行有返回值的函数；成功则返回结果，并释放锁；失败则抛出异常
     * @param supplier 函数方法
     * @return 执行结果
     */
    public <T> T execute(Supplier<T> supplier) {

        if (!getGainLock()) {
            this.gainLock = tryLock();
        }
        return supplyExecute(supplier);
    }

    /**
     * 锁对象执行无返回值的函数；成功则正常，并释放锁；失败则抛出异常
     */
    public void execute(ConsumerNoParams consumer) {

        if (!getGainLock()) {
            this.gainLock = tryLock();
        }
        runExecute(consumer);
    }

    /**
     * 尝试获取一次锁
     * @return 是否获取到了锁
     */
    public boolean tryLock() {

        this.gainLock = this.lockAction.tryLock(this.lockKey, this.secret, this.expireTime);
        return this.gainLock;
    }

    /**
     * 释放锁
     * @return 释放锁是否成功
     */
    public boolean releaseLock() {

        return this.lockAction.releaseLock(this.lockKey, this.secret);
    }

    private <T> T supplyExecute(Supplier<T> supplier) {

        if (!this.gainLock) {
            if (this.spinLock && this.spinTimes > 0) {
                spin();
                return supplyExecute(supplier);
            }
            throw new DistributeLockTimeoutException(lockKey, expireTime, exceptionMsg, spinLock, waitingTime, maxWaitingTime);
        }
        try {
            if (null == supplier) {
                throw new NullPointerException(String.format("Supplier cannot execute, because supplier is undefined, key = %s", this.lockKey));
            }
            return supplier.get();
        } finally {
            releaseLock();
        }
    }

    private void runExecute(ConsumerNoParams consumer) {

        if (!this.gainLock) {
            if (this.spinLock && this.spinTimes > 0) {
                spin();
                runExecute(consumer);
                // 进入自旋执行完逻辑需要return退出
                return;
            }
            throw new DistributeLockTimeoutException(lockKey, expireTime, exceptionMsg, spinLock, waitingTime, maxWaitingTime);
        }
        try {
            if (null == consumer) {
                throw new NullPointerException(String.format("Consumer cannot execute, because consumer is undefined, key = %s", this.lockKey));
            }
            consumer.exec();
        } finally {
            releaseLock();
        }
    }

    public static DistributeLockContext buildLockContext(String lockKey, Long expireTime, String exceptionMsg,
                                                         Long waitingTime, Boolean spinLock, Long maxWaitingTime,
                                                         LockAction lockAction) {

        return new DistributeLockContext(lockKey, expireTime, exceptionMsg, waitingTime, spinLock, maxWaitingTime, lockAction);
    }

    private DistributeLockContext(String lockKey, Long expireTime, String exceptionMsg, Long waitingTime, Boolean spinLock,
                                  Long maxWaitingTime, LockAction lockAction) {

        if (Objects.nonNull(expireTime) && expireTime <= 0L) {
            throw new IllegalArgumentException("The expireTime of distributeLock must be positive");
        }

        this.lockKey = lockKey;
        // 雪花算法根据机器IP和机器名称生成全局唯一ID
        this.secret = SnowflakeIdUtils.generatedStringId();
        this.expireTime = expireTime;
        this.exceptionMsg = StringUtils.isBlank(exceptionMsg) ? DEFAULT_EXCEPTION_MSG : exceptionMsg;
        this.spinLock = spinLock;
        this.lockAction = lockAction;
        if (this.spinLock) {
            if (Objects.nonNull(waitingTime) && waitingTime <= 0L) {
                throw new IllegalArgumentException("The waitingTime of distributeLock must be positive");
            }
            if (Objects.nonNull(maxWaitingTime) && maxWaitingTime <= 0L) {
                throw new IllegalArgumentException("The maxWaitingTime of distributeLock must be positive");
            }
            this.maxWaitingTime = Objects.isNull(maxWaitingTime) ? 0L : maxWaitingTime;
            // 1. 防呆设计，当设置的自旋时间大于3秒时，会拖垮服务，会自动改为默认自旋时间
            this.waitingTime = Objects.nonNull(waitingTime) && waitingTime > FOOL_PROOF_MAX_WAITING_TIME ? FOOL_PROOF_DEFAULT_WAITING_TIME : waitingTime;
            // 2. 防止自旋时间大于等于锁失效时间，会被其他新线程抢夺锁，会自动改为失效时间的1/3
            this.waitingTime = Objects.isNull(waitingTime) || waitingTime >= expireTime ? expireTime / 3L : waitingTime;
            // 3. 计算自旋次数
            this.spinTimes = this.maxWaitingTime / this.waitingTime;
            // 4. 初始已自旋次数为0
            this.spunTimes = 0L;
        }
        this.gainLock = false;
    }

    /**
     * 自旋，当超过最大自旋时间时结束
     */
    private void spin() {

        // 抛出已知异常，打印日志
        try {
            TimeUnit.MILLISECONDS.sleep(this.waitingTime);
        } catch (InterruptedException e) {
            log.error(String.format("Distribute lock sleep in spin exist error, key = %s, waitingTime= %s", this.lockKey, this.waitingTime), e);
            throw new DistributeLockTimeoutException(lockKey, expireTime, exceptionMsg, spinLock, waitingTime, maxWaitingTime);
        }
        // 每次自旋，扣减一次自旋次数，已自旋次数+1
        --this.spinTimes;
        ++this.spunTimes;
        this.gainLock = tryLock();
    }

}
