package com.lily.parcelhubcore.shared.annotation;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;

@Aspect
@Component
public class ControllerMetricsAspect {

    private static final String TIMER_NAME = "parcel.controller.requests";
    private static final String EXCEPTION_COUNTER_NAME = "parcel.controller.exceptions";

    @Resource
    private MeterRegistry meterRegistry;

    @Resource
    private HttpServletRequest request;

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void restControllerClass() {
    }

    @Around("restControllerClass() && execution(public * *(..))")
    public Object recordControllerMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        var startNanos = System.nanoTime();
        // endpoint: class name#method name
        var endpoint = joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "#"
                + joinPoint.getSignature().getName();

        var httpMethod = request.getMethod();
        var uriPattern = resolveBestMatchingPattern();
        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            meterRegistry.counter(
                    EXCEPTION_COUNTER_NAME,
                    "endpoint", endpoint,
                    "method", httpMethod,
                    "uri", uriPattern,
                    "exception", ex.getClass().getSimpleName()
            ).increment();

            throw ex;
        } finally {
            long durationNanos = System.nanoTime() - startNanos;

            Timer.builder(TIMER_NAME)
                    .description("Controller request duration by endpoint")
                    .tags(
                            "endpoint", endpoint,
                            "method", httpMethod,
                            "uri", uriPattern,
                            "outcome", "success"
                    )
                    .serviceLevelObjectives(
                            Duration.ofMillis(50),
                            Duration.ofMillis(100),
                            Duration.ofMillis(200),
                            Duration.ofMillis(500),
                            Duration.ofSeconds(1),
                            Duration.ofSeconds(2),
                            Duration.ofSeconds(5)
                    )
                    .register(meterRegistry)
                    .record(durationNanos, TimeUnit.NANOSECONDS);
        }
    }

    private String resolveBestMatchingPattern() {
        Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (pattern == null) {
            return "unknown";
        }
        return pattern.toString();
    }
}
