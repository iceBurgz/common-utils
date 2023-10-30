package com.zhubin.commonutils.redis.exception;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;


@Getter
@EqualsAndHashCode(callSuper = true)
@ToString
public class DistributeLockTimeoutException extends RuntimeException {

    /**
     * 单个锁的key，可根据参数使用SPEL语法解析
     * 例：
     * 1. 无需使用SPEL语法解析，如key = "keyString"
     * 2. 使用SPEL语法解析参数，如key = "'AAA' + #dto.code + #dto.code.concat(#dto.id)"，可探索更多SPEL语法
     */
    private final String lockKey;

    /**
     * 锁失效时间，默认3秒
     */
    private final Long expireTime;

    /**
     * 自定义异常提示
     */
    private final String exceptionMsg;

    /**
     * 自旋
     */
    private final Boolean spin;

    /**
     * 自旋时间
     */
    private final Long waitingTime;

    /**
     * 总自旋时间
     */
    private final Long maxWaitingTime;

    public DistributeLockTimeoutException(String lockKey, Long expireTime, String exceptionMsg, Boolean spin, Long waitingTime, Long maxWaitingTime) {
        super(exceptionMsg);
        this.lockKey = lockKey;
        this.expireTime = expireTime;
        this.exceptionMsg = exceptionMsg;
        this.spin = spin;
        this.waitingTime = waitingTime;
        this.maxWaitingTime = maxWaitingTime;
    }
}
