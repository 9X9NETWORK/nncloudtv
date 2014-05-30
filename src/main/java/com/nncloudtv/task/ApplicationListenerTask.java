package com.nncloudtv.task;

import java.util.logging.Logger;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.nncloudtv.lib.CacheFactory;

@Component
public class ApplicationListenerTask implements ApplicationListener<ContextRefreshedEvent> {
    
    protected static Logger log = Logger.getLogger(ApplicationListenerTask.class.getName());
    
    public void onApplicationEvent(ContextRefreshedEvent event) {
        
        log.info("on ContextRefreshedEvent");
        
        // to initialize memcache
        if (CacheFactory.isEnabled && !CacheFactory.isRunning) {
            try {
                CacheFactory.isEnabled = false;
                CacheFactory.reconfigClient();
            } finally {
                CacheFactory.isEnabled = true;
            }
        }
    }
}
