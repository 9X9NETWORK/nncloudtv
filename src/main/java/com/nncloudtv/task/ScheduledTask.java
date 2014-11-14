package com.nncloudtv.task;

import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.nncloudtv.lib.CacheFactory;
import com.nncloudtv.lib.NnDateUtil;

@Service
@EnableScheduling
public class ScheduledTask {
    
    protected static Logger log = Logger.getLogger(ScheduledTask.class.getName());
    
    static final int MC_INTERVAL = 584141; // memcache check interval (milliseconds)
    static final int GC_INTERVAL = 604171; // garbage collection interval (milliseconds)
    
    @Scheduled(fixedRate = GC_INTERVAL)
    synchronized public void triggerGC() {
        
        System.gc(); // trigger garbage collection
        
        long max   = Runtime.getRuntime().maxMemory();
        long total = Runtime.getRuntime().totalMemory();
        long free  = Runtime.getRuntime().freeMemory();
        
        System.out.println("[memory] max = " + FileUtils.byteCountToDisplaySize(max)
                             +  ", total = " + FileUtils.byteCountToDisplaySize(total)
                             +   ", free = " + FileUtils.byteCountToDisplaySize(free));
        if (max == total && free < total / 100) {
            log.severe("available memory is less than 1%");
        }
    }
    
    @Scheduled(fixedDelay = MC_INTERVAL)
    synchronized public void checkingMemcacheServer() {
        
        if (CacheFactory.isEnabled) {
            
            System.out.println("[memcache] checking memcache server(s) ...");
            long before = NnDateUtil.timestamp();
            CacheFactory.reconfigClient();
            System.out.println(String.format("[memcache] reconfig costs %d milliseconds", NnDateUtil.timestamp() - before));
        }
        
    }
    
}
