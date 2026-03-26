package com.scpt.ats.flowengine.common;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class ControllerAopConfig {
    private static final Logger log = LoggerFactory.getLogger(ControllerAopConfig.class);

    @Pointcut("execution(* com.scpt.ats.flowengine.web.*Controller.*(..))")
    public void controller() {
    }

    @Around("controller()")
    public Object aroundController(final ProceedingJoinPoint pjp) throws Throwable {
        try {
            return pjp.proceed();
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            return "fucked";
        }
    }
}
