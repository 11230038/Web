package com.example.end.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class OperationAuditAspect {

    private final OperationAuditRecorder operationAuditRecorder;

    public OperationAuditAspect(OperationAuditRecorder operationAuditRecorder) {
        this.operationAuditRecorder = operationAuditRecorder;
    }

    @Around("@annotation(com.example.end.audit.Log)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = method.getName();
        Object params = joinPoint.getArgs();

        try {
            Object result = joinPoint.proceed();
            operationAuditRecorder.record(className, methodName, params, result, startTime);
            return result;
        } catch (Throwable ex) {
            operationAuditRecorder.record(className, methodName, params, ex.getMessage(), startTime);
            throw ex;
        }
    }
}
