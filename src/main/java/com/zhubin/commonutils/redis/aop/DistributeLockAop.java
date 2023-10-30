package com.zhubin.commonutils.redis.aop;

import com.zhubin.commonutils.redis.DistributeLockUtil;
import com.zhubin.commonutils.redis.annotation.DistributeLock;
import com.zhubin.commonutils.redis.exception.DistributeLockException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

/**
 * @author zhubin
 * @date 2023/10/30
 * @description 分布式锁aop
 */
@Aspect
@Slf4j
public class DistributeLockAop {

    private static final String HASH_SYMBOL = "#";

    /**
     * SPEL表达式解析器
     */
    private final SpelExpressionParser spelExpressionParser = new SpelExpressionParser();

    /**
     * 参数名发现器
     */
    private final DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Pointcut("@annotation(com.zhubin.commonutils.redis.annotation.DistributeLock)")
    public void distributeLockAop() {

    }

    @Around(value = "distributeLockAop()")
    public Object tryLock(ProceedingJoinPoint joinPoint) {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributeLock lock = method.getAnnotation(DistributeLock.class);

        // 拿到锁的key，密钥，失效时间
        String lockKey = parseLockKey(lock.key(), method, joinPoint);
        long expireTime = lock.expireTime();
        String exceptionMsg = lock.exceptionMsg();
        long waitingTime = lock.waitingTime();
        long maxWaitingTime = lock.maxWaitingTime();

        // 获取锁，获得则通过，未通过则抛已知异常
        if (lock.spin()) {
            return DistributeLockUtil.supplySpinLock(() -> {
                try {
                    return joinPoint.proceed();
                } catch (Throwable e) {
                    log.error("Distribute joinPoint proceed fail, ", e);
                    throw new DistributeLockException("lock proceed fail", e);
                }
            }, lockKey, expireTime, exceptionMsg, waitingTime, maxWaitingTime);
        }
        return DistributeLockUtil.supplyExclusiveLock(() -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable e) {
                log.error("Distribute joinPoint proceed fail, ", e);
                throw new DistributeLockException("lock proceed fail", e);
            }
        }, lockKey, expireTime, exceptionMsg);
    }

    private String parseLockKey(String lockString, Method method, ProceedingJoinPoint joinPoint) {

        if (StringUtils.isBlank(lockString)) {
            throw new IllegalArgumentException(String.format("The key of DistributeLock cannot be empty, the name of method is %s", method.getName()));
        }

        // 无需SPEL校验
        if (!lockString.contains(HASH_SYMBOL)) {
            return lockString;
        }

        // 需要SPEL校验
        // 获得方法参数名数组，使用解析对象存放参数和值的映射关系
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
        if (parameterNames != null && parameterNames.length > 0) {
            EvaluationContext context = new StandardEvaluationContext();
            // 获取方法参数值
            Object[] args = joinPoint.getArgs();
            for (int i = 0; i < args.length; i++) {
                // 替换spel里的变量值为实际值
                context.setVariable(parameterNames[i], args[i]);
            }
            Expression expression = spelExpressionParser.parseExpression(lockString);
            return expression.getValue(context, String.class);
        } else {
            throw new IllegalArgumentException(String.format("DistributeLock cannot discover parameter name from method, the name of method is %s", method.getName()));
        }
    }

}
