package org.infinity.passport.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.infinity.passport.annotation.ExecutionSwitch;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Pointcut configuration
 */
@Aspect
@Configuration
public class ExecutionSwitchAspect {

    private final Environment env;

    public ExecutionSwitchAspect(Environment env) {
        this.env = env;
    }

    @Around("@annotation(annotation)")
    public Object switchAround(ProceedingJoinPoint joinPoint, ExecutionSwitch annotation) throws Throwable {
        if ("true".equals(env.getProperty(annotation.on()))) {
            // Proceed to execute method
            return joinPoint.proceed();
        }
        return null;
    }
}
