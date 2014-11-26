package com.nncloudtv.task;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import com.nncloudtv.lib.CacheFactory;

public class ApplicationListenerTask implements ApplicationListener<ContextRefreshedEvent> {
    
    static boolean startup = false;
    
    synchronized public void onApplicationEvent(ContextRefreshedEvent event) {
        
        System.out.println("[onContextRefreshedEvent]");
        
        if (startup == false) {
            
            startup = true;
            System.out.println("[onContextRefreshedEvent] startup");
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
}
