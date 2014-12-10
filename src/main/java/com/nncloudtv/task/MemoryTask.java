package com.nncloudtv.task;

import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.nncloudtv.lib.QueueFactory;

@Service
@EnableScheduling
public class MemoryTask implements ScheduledTask {
    
    protected static Logger log = Logger.getLogger(MemoryTask.class.getName());
    
    public static String memoryUsageReport = null;
    
    @Scheduled(fixedRate = GC_INTERVAL)
    synchronized public static void triggerGC() {
        
        System.gc(); // trigger garbage collection
        
        long max   = Runtime.getRuntime().maxMemory();
        long total = Runtime.getRuntime().totalMemory();
        long free  = Runtime.getRuntime().freeMemory();
        memoryUsageReport = String.format("[memory] max = %s, total = %s, free = %s",
                                      FileUtils.byteCountToDisplaySize(max),
                                      FileUtils.byteCountToDisplaySize(total),
                                      FileUtils.byteCountToDisplaySize(free));
        System.out.println(memoryUsageReport);
        if (max == total && free < total / 100)
            log.severe("available memory is less than 1%");
        
        // test
        QueueFactory.publishMessage(memoryUsageReport);
    }
    
}
