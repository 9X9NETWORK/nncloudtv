package com.nncloudtv.task;

import java.util.Date;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.nncloudtv.lib.CacheFactory;

@Service
@EnableScheduling
public class ScheduledTask {
    
    protected static Logger log = Logger.getLogger(ScheduledTask.class.getName());
    
    public final int MEMCACHE_CHECK_INTERVAL = 100000; // in milliseconds
    public final int GC_INTERVAL = 200000; // in milliseconds
    
    @Scheduled(fixedDelay = GC_INTERVAL)
    public void triggerGC() {
        
        System.gc(); // trigger garbage collection
        log.info("memory: max = " + Runtime.getRuntime().maxMemory()
                   + ", total = " + Runtime.getRuntime().totalMemory()
                   +  ", free = " + Runtime.getRuntime().freeMemory());
    }
    
    @Scheduled(fixedDelay = MEMCACHE_CHECK_INTERVAL)
    public void checkingMemcacheServer() {
        
        if (CacheFactory.isEnabled) {
            
            log.info("checking memcache server(s) ...");
            long now = new Date().getTime();
            CacheFactory.reconfigClient();
            log.info("memcache reconfig costs " + (new Date().getTime() - now) + " milliseconds");
        }
        
    }
    
}
