package com.nncloudtv.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class LoggingAspect {
    
    @After("com.nncloudtv.web.api.ApiContext.isProductionSite(..)")
    public void logApiEcho(JoinPoint joinPoint, Object result) {
        
        System.out.println("[aspect] " + joinPoint.getSignature().getName());
        System.out.println("[aspect] " + result);
        
    }
}
