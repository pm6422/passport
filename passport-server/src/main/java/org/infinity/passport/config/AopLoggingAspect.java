package org.infinity.passport.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.infinity.passport.utils.RequestIdHolder;
import org.infinity.passport.utils.id.IdGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Aspect for logging execution arguments and result of the method.
 */
@Aspect
@ConditionalOnProperty(prefix = "application.aop-logging", value = "enabled", havingValue = "true")
@Configuration
@Slf4j
public class AopLoggingAspect {

    public static final String                REQUEST_ID = "X-REQUEST-ID";
    @Resource
    private             ApplicationProperties applicationProperties;

    /**
     * Log method arguments and result of controller
     * <p>
     * Refer to http://www.imooc.com/article/297283
     *
     * @param joinPoint join point
     * @return return value
     * @throws Throwable if exception occurs
     */
    @Around("execution(@(org.springframework.web.bind.annotation.*Mapping) * *(..))")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = servletRequestAttributes != null ? servletRequestAttributes.getRequest() : null;
            HttpServletResponse response = servletRequestAttributes != null ? servletRequestAttributes.getResponse() : null;
            beforeRun(joinPoint, request);
            Object result = joinPoint.proceed();
            afterRun(joinPoint, response, result);
            return result;
        } catch (IllegalArgumentException e) {
            // Catch illegal argument exception and re-throw
            log.error("Illegal argument[s]: {} in {}.{}()",
                    Arrays.toString(joinPoint.getArgs()),
                    joinPoint.getSignature().getDeclaringType().getSimpleName(),
                    joinPoint.getSignature().getName());
            throw e;
        } finally {
            if (StringUtils.isNotEmpty(RequestIdHolder.getRequestId())) {
                RequestIdHolder.destroy();
            }
        }
    }

    public void beforeRun(ProceedingJoinPoint joinPoint, HttpServletRequest request) {
        if (printLog(joinPoint)) {
            return;
        }
        // Store request id
        String requestId = Optional.ofNullable(request.getHeader(REQUEST_ID)).orElse("R" + IdGenerator.generateTimestampId());
        RequestIdHolder.setRequestId(requestId);

        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        Object[] arguments = joinPoint.getArgs();
        Map<String, Object> paramMap = new HashMap<>(arguments.length);
        for (int i = 0; i < arguments.length; i++) {
            if (isValidArgument(arguments[i])) {
                paramMap.put(paramNames[i], arguments[i]);
            }
        }
        log.info("{} Request: {}.{}() with argument[s] = {}",
                RequestIdHolder.getRequestId(),
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(),
                paramMap);
    }

    private void afterRun(ProceedingJoinPoint joinPoint, HttpServletResponse response, Object result) {
        if (printLog(joinPoint)) {
            return;
        }
        Optional.ofNullable(response).ifPresent(resp -> resp.setHeader(REQUEST_ID, RequestIdHolder.getRequestId()));
        log.info("{} Response: {}.{}() with result = {}",
                RequestIdHolder.getRequestId(),
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(),
                result);
    }

    private boolean printLog(ProceedingJoinPoint joinPoint) {
        return !log.isInfoEnabled() || !matchLogMethod(joinPoint);
    }

    private boolean matchLogMethod(ProceedingJoinPoint joinPoint) {
        if (!applicationProperties.getAopLogging().isMethodWhitelistMode()) {
            return true;
        }
        String method = joinPoint.getSignature().getDeclaringType().getSimpleName() + "." +
                joinPoint.getSignature().getName();
        return applicationProperties.getAopLogging().getMethodWhitelist().contains(method);
    }

    private boolean isValidArgument(Object argument) {
        return !(argument instanceof ServletRequest) && !(argument instanceof ServletResponse);
    }
}