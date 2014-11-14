package com.nncloudtv.task;

import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.nncloudtv.service.CounterFactory;

@Service
@EnableScheduling
public class CounterTask extends CounterFactory {
    
    protected static Logger log = Logger.getLogger(CounterTask.class.getName());
    
    public final int CC_INTERVAL = 294001; // clean counter interval (milliseconds)
    
    @Scheduled(fixedDelay = CC_INTERVAL)
    public void cleanDirtyCounter() {
        Set<Entry<String,Integer>> entrySet = dirtyCounters.entrySet();
        for (Entry<String, Integer> entry : entrySet) {
            System.out.println(String.format("[counter] \"%s\" increment %d", entry.getKey(), entry.getValue()));
            increment(entry.getKey(), entry.getValue());
        }
        dirtyCounters.clear();
    }
    
}
