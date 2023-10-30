package com.zhubin.commonutils.redis.exception;


import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class DistributeLockException extends RuntimeException {

    private final Throwable throwable;

    public DistributeLockException(String result, Throwable throwable) {
        super(result);
        this.throwable = throwable;
    }
}
