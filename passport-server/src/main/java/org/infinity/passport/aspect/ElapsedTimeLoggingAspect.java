package org.infinity.passport.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.infinity.passport.config.ApplicationConstants;
import org.infinity.passport.config.ApplicationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;

/**
 * Aspect for logging elapsed time of Spring components.
 */
@Aspect
@ConditionalOnProperty(prefix = "application.elapsed-time-logging", value = "enabled", havingValue = "true")
@Configuration
@Slf4j
public class ElapsedTimeLoggingAspect {

    private static final String SERVICE_PACKAGE    = "within(" + ApplicationConstants.BASE_PACKAGE + ".service..*)";
    private static final String CONTROLLER_PACKAGE = "within(" + ApplicationConstants.BASE_PACKAGE + ".controller..*)";
    private static final String HEADER_KEY         = "X-ELAPSED";
    private static final int    SECOND             = 1000;
    private static final int    MINUTE             = 60000;

    private final ApplicationProperties applicationProperties;

    public ElapsedTimeLoggingAspect(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    /**
     * Refer to http://www.imooc.com/article/297283
     *
     * @param joinPoint join point
     * @return return value
     * @throws Throwable if exception occurs
     */
    @Around(CONTROLLER_PACKAGE)
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Object result = joinPoint.proceed();
        stopWatch.stop();
        long elapsed = stopWatch.getTotalTimeMillis();

        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = servletRequestAttributes != null ? servletRequestAttributes.getResponse() : null;
        if (response != null) {
            // Store execution time to each http header
            if (elapsed < SECOND) {
                response.setHeader(HEADER_KEY, "" + elapsed + "ms");
            } else if (elapsed < MINUTE) {
                response.setHeader(HEADER_KEY, "" + elapsed / SECOND + "s");
            } else {
                response.setHeader(HEADER_KEY, "" + elapsed / (MINUTE) + "m");
            }
        }
        outputLog(joinPoint, elapsed);
        return result;
    }

    @Around(SERVICE_PACKAGE)
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Object result = joinPoint.proceed();
        stopWatch.stop();
        long elapsed = stopWatch.getTotalTimeMillis();
        outputLog(joinPoint, elapsed);
        return result;
    }

    private void outputLog(ProceedingJoinPoint joinPoint, long elapsed) {
        if (elapsed > applicationProperties.getElapsedTimeLogging().getSlowExecutionThreshold()) {
            if (elapsed < SECOND) {
                log.warn("Found slow running method {}.{}() over {}ms",
                        joinPoint.getSignature().getDeclaringType().getSimpleName(), joinPoint.getSignature().getName(), elapsed);
            } else if (elapsed < MINUTE) {
                log.warn("Found slow running method {}.{}() over {}s",
                        joinPoint.getSignature().getDeclaringType().getSimpleName(), joinPoint.getSignature().getName(), elapsed / 1000);
            } else {
                log.warn("Found slow running method {}.{}() over {}m",
                        joinPoint.getSignature().getDeclaringType().getSimpleName(), joinPoint.getSignature().getName(), elapsed / (1000 * 60));
            }
        }
    }
}
