package com.lily.parcelhubcore.shared.annotation;

import static com.lily.parcelhubcore.shared.constants.Contants.MDC_REQUEST_ID;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson2.JSON;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ControllerAccessLogAspect {

    @Around("execution(* com.lily.parcelhubcore.parcel.api.controller..*.*(..))")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        String requestId = MDC.get(MDC_REQUEST_ID);
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        String argsJson = toJson(filterArgs(joinPoint.getArgs()));

        try {
            var result = joinPoint.proceed();
            log.info("[业务接口调用][成功][requestId={}][{}.{}][request={}][response={}]", requestId, className, methodName, argsJson, toJson(result));

            return result;
        } catch (Throwable ex) {
            log.error("[业务接口调用][失败][requestId={}][{}.{}][request={}][error={}]", requestId, className, methodName, argsJson, ex.getMessage(), ex);
            throw ex;
        }
    }

    private List<Object> filterArgs(Object[] args) {
        var result = new ArrayList<>();
        if (args == null) {
            return result;
        }

        for (Object arg : args) {
            if (arg instanceof ServletRequest || arg instanceof ServletResponse || arg instanceof BindingResult) {
                continue;
            }
            result.add(arg);
        }
        return result;
    }

    private String toJson(Object obj) {
        try {
            return JSON.toJSONString(obj);
        } catch (Exception e) {
            return "[json serialize failed]";
        }
    }
}
