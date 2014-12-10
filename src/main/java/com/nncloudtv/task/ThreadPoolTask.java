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
    
    public static String threadPoolReport = null;
    
    @Scheduled(fixedRate = TP_INTERVAL)
    synchronized public static void reportthreadPool() {
        
        ScheduledThreadPoolExecutor scheduler = NNF.getScheduler();
        threadPoolReport = String.format("[scheduler] pool size is %d, %d is queued, %d of them are active", scheduler.getPoolSize(), scheduler.getTaskCount(), scheduler.getActiveCount());
        System.out.println(threadPoolReport);
    }
    
}
