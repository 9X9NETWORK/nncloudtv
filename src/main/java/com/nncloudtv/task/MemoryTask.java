package com.nncloudtv.task;

import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class MemoryTask implements ScheduledTask {
    
    protected static Logger log = Logger.getLogger(MemoryTask.class.getName());
    
    @Scheduled(fixedRate = GC_INTERVAL)
    synchronized public static void triggerGC() {
        
        long max   = Runtime.getRuntime().maxMemory();
        long total = Runtime.getRuntime().totalMemory();
        long free  = Runtime.getRuntime().freeMemory();
        
        System.out.println("[memory] max = " + FileUtils.byteCountToDisplaySize(max)
                             +  ", total = " + FileUtils.byteCountToDisplaySize(total)
                             +   ", free = " + FileUtils.byteCountToDisplaySize(free));
        if (max == total && free < total / 100)
            log.severe("available memory is less than 1%");
        System.gc(); // trigger garbage collection
    }
    
}
