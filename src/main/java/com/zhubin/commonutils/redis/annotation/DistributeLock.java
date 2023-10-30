package com.zhubin.commonutils.redis.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zhubin
 * @date 2023/10/30
 * @description 分布式锁注解，请勿在同一个类中使用
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributeLock {

    /**
     * 单个锁的key，可根据参数使用SPEL语法解析
     * 例：
     * 1. 无需使用SPEL语法解析，如key = "keyString"
     * 2. 使用SPEL语法解析参数，如key = "'AAA' + #dto.code + #dto.code.concat(#dto.id)"，可探索更多SPEL语法
     * @return 单个锁的key
     */
    String key();

    /**
     * 锁失效时间，默认3秒
     * @return 锁失效时间
     */
    long expireTime() default 3 * 1000L;

    /**
     * 自定义异常提示
     * @return 自定义异常提示
     */
    String exceptionMsg() default "";

    /**
     * 自旋
     * @return 自旋
     */
    boolean spin() default false;

    /**
     * 自旋时间
     * @return 自旋时间
     */
    long waitingTime() default 0;

    /**
     * 总自旋时间
     * @return 总自旋时间
     */
    long maxWaitingTime() default 0;

}
