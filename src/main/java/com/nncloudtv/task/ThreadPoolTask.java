package com.nncloudtv.task;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Logger;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.nncloudtv.lib.NNF;

@Service
@EnableScheduling
public class ThreadPoolTask implements ScheduledTask {
    
    protected static Logger log = Logger.getLogger(ThreadPoolTask.class.getName());
    
    @Scheduled(fixedRate = TP_INTERVAL)
    synchronized public static void reportthreadPool() {
        
        ScheduledThreadPoolExecutor scheduler = NNF.getScheduler();
        System.out.println(String.format("[scheduler] %d task(s) queued in pool, pool size is %d", scheduler.getQueue().size(), scheduler.getPoolSize()));
    }
    
}
