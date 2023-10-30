package com.zhubin.commonutils.redis.function;

/**
 * @author zhubin
 * @date 2023/10/30
 * @description 无参执行函数
 */
@FunctionalInterface
public interface ConsumerNoParams {

    /**
     * 执行的方法
     */
    void exec();

}
