package com.nncloudtv.task;

import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Service
@EnableScheduling
public class ScheduledTask {
    
    protected static Logger log = Logger.getLogger(ScheduledTask.class.getName());
    
    @Scheduled(fixedDelay = 10000)
    public void checkingMemcacheServer() {
        
        log.info("Checking memcache server");
    }
    
}
