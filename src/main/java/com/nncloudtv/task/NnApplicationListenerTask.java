package com.nncloudtv.task;

import java.util.logging.Logger;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class NnApplicationListenerTask implements ApplicationListener<ContextRefreshedEvent> {
    
    protected static Logger log = Logger.getLogger(NnApplicationListenerTask.class.getName());
    
    public void onApplicationEvent(ContextRefreshedEvent event) {
        
        log.info("EVENT: " + event.toString());
        
    }
    
}
