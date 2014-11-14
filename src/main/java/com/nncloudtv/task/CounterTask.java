package com.nncloudtv.task;

import java.util.Map.Entry;
import java.util.Set;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.nncloudtv.service.CounterFactory;

@Service
@EnableScheduling
public class CounterTask extends CounterFactory {
    
    public static final int CC_INTERVAL = 294001; // clean counter interval (milliseconds)
    
    @Scheduled(fixedDelay = CC_INTERVAL)
    public void cleanDirtyCounter() {
        synchronized (dirtyCounters) {
            Set<Entry<String,Integer>> entrySet = dirtyCounters.entrySet();
            for (Entry<String, Integer> entry : entrySet) {
                System.out.println(String.format("[counter] \"%s\" increment %d", entry.getKey(), entry.getValue()));
                increment(entry.getKey(), entry.getValue());
            }
            dirtyCounters.clear();
        }
    }
}
