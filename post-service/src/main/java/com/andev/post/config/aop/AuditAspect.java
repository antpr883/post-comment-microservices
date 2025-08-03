package com.andev.post.config.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
public class AuditAspect {

    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    @Pointcut("within(@AuditLog *)") // classes annotated with @AuditLog
    public void beanAnnotatedWithAuditLog() {}

    @Pointcut("execution(* *(..))") // all methods
    public void allMethods() {}

    @Pointcut("beanAnnotatedWithAuditLog() && allMethods() && !@annotation(SkipAudit)") // skip methods annotated with
    // @SkipAudit
    public void auditableMethods() {}

    @Around("auditableMethods()")
    public Object logAudit(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Class<?> targetClass = joinPoint.getTarget().getClass();

        Map<String, Object> auditMap = new HashMap<>();
        auditMap.put("class", targetClass.getSimpleName());
        auditMap.put("method", method.getName());
        auditMap.put("args", Arrays.toString(joinPoint.getArgs()));
        auditMap.put("user", AuditUtils.getUserName());
        auditMap.put("timestamp", Instant.now().toEpochMilli());

        try {
            Object result = joinPoint.proceed();
            auditMap.put("result", result != null ? result.toString() : "null");

            auditLogger.info(new ObjectMapper().writeValueAsString(auditMap));
            return result;
        } finally {
            MDC.clear();
        }
    }
}
