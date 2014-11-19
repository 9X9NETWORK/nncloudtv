package com.nncloudtv.task;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
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
            
            HashSet<Entry<String, Integer>> counterSet = new HashSet<Entry<String,Integer>>(dirtyCounters.entrySet());
            dirtyCounters.clear();
            Iterator<Entry<String, Integer>> it = counterSet.iterator();
            while (it.hasNext()) {
                
                Entry<String, Integer> entry = it.next();
                increment(entry.getKey(), entry.getValue());
                System.out.println(String.format("[counter] \"%s\" increment %d", entry.getKey(), entry.getValue()));
                it.remove();
            }
        }
    }
}
