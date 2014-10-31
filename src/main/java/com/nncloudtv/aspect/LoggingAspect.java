package com.nncloudtv.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class LoggingAspect {
    
    @Before("execution(* com.nncloudtv.web.api.ApiMisc.echo(..))")
    public void logApiEcho(JoinPoint joinPoint) {
        
        System.out.println("[aspect] " + joinPoint.getSignature().getName());
        
    }
}
