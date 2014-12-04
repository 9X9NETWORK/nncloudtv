package com.nncloudtv.task;

import java.util.logging.Logger;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.nncloudtv.lib.CacheFactory;
import com.nncloudtv.lib.NnDateUtil;

@Service
@EnableScheduling
public class CacheTask implements ScheduledTask {
    
    protected static Logger log = Logger.getLogger(CacheTask.class.getName());
    
    @Scheduled(fixedDelay = MC_INTERVAL)
    synchronized public static void checkingMemcacheServer() {
        
        if (CacheFactory.isEnabled) {
            
            System.out.println("[cache] checking memcache server(s) ...");
            long before = NnDateUtil.timestamp();
            CacheFactory.reconfigClient();
            System.out.println(String.format("[cache] reconfig costs %d milliseconds", NnDateUtil.timestamp() - before));
        } else {
            log.info("memcache is not enabled");
        }
    }
    
}
