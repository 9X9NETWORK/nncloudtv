package com.nncloudtv.task;

import java.util.logging.Logger;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.stereotype.Component;

@Component
public class NnApplicationListenerTask implements ApplicationListener<ApplicationEvent> {
    
    protected static Logger log = Logger.getLogger(NnApplicationListenerTask.class.getName());
    
    public void onApplicationEvent(ApplicationEvent event) {
        
        log.info("EVENT" + event.toString());
        
        if (event instanceof ContextStartedEvent) {
            
            log.info("Application started!");
        }
        
    }
    
}
