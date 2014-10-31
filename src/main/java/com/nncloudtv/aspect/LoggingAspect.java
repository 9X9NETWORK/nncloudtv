package com.nncloudtv.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class LoggingAspect {
    
    @After("execution(* com.nncloudtv.web.api.ApiContext.isProductionSite(..))")
    public void logApiEcho(JoinPoint joinPoint, Object result) {
        
        System.out.println("[aspect] " + joinPoint.getSignature().getName());
        System.out.println("[aspect] " + result);
        
    }
}
