package com.andev.comment.config.aop;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * Enhanced AOP Aspect for comprehensive audit and performance logging.
 * Provides detailed tracking of comment service operations with contextual information.
 *
 * @author Comment Service Team
 * @since 1.0
 */
@Slf4j
@Aspect
@Component
public class AuditAspect {

    @Value("${app.performance.slow-operation-threshold:1000}")
    private long slowOperationThreshold;

    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("AUDIT");
    private static final Logger PERFORMANCE_LOGGER = LoggerFactory.getLogger("PERFORMANCE");

    // ========== POINTCUTS ==========

    @Pointcut("within(@AuditLog *)") // classes annotated with @AuditLog
    public void beanAnnotatedWithAuditLog() {}

    @Pointcut("@annotation(AuditLog)") // methods annotated with @AuditLog
    public void methodAnnotatedWithAuditLog() {}

    @Pointcut("beanAnnotatedWithAuditLog() || methodAnnotatedWithAuditLog()")
    public void auditableMethods() {}

    @Pointcut("@annotation(SkipAudit)")
    public void skipAuditMethods() {}

    @Around("auditableMethods() && !skipAuditMethods()")
    public Object logAudit(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Class<?> targetClass = joinPoint.getTarget().getClass();

        long startTime = System.currentTimeMillis();

        Map<String, Object> auditMap = new HashMap<>();
        auditMap.put("class", targetClass.getSimpleName());
        auditMap.put("method", method.getName());
        auditMap.put("args", Arrays.toString(joinPoint.getArgs()));
        auditMap.put("user", AuditUtils.getUserName());
        auditMap.put("timestamp", Instant.now().toEpochMilli());

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            auditMap.put("result", "success");
            auditMap.put("executionTimeMs", executionTime);
            AUDIT_LOGGER.info(new ObjectMapper().writeValueAsString(auditMap));

            // Performance logging for slow operations
            if (executionTime > slowOperationThreshold) {
                PERFORMANCE_LOGGER.warn(
                        "SLOW_OPERATION: {}.{} took {}ms (threshold: {}ms)",
                        targetClass.getSimpleName(),
                        method.getName(),
                        executionTime,
                        slowOperationThreshold);
            }

            return result;
        } catch (Throwable throwable) {
            long executionTime = System.currentTimeMillis() - startTime;

            auditMap.put("result", "error");
            auditMap.put("error", throwable.getMessage());
            auditMap.put("executionTimeMs", executionTime);
            AUDIT_LOGGER.error(new ObjectMapper().writeValueAsString(auditMap));
            throw throwable;
        } finally {
            MDC.clear();
        }
    }
}
