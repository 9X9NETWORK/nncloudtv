package com.nncloudtv.task;

import java.util.HashSet;
import java.util.Map.Entry;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.nncloudtv.lib.NNF;
import com.nncloudtv.model.CounterShard;
import com.nncloudtv.service.CounterFactory;

@Service
@EnableScheduling
public class CounterTask extends CounterFactory {
    
    public static final int CC_INTERVAL = 294001; // clean counter interval (milliseconds)
    
    @Scheduled(fixedDelay = CC_INTERVAL)
    public void cleanDirtyCounter() {
        synchronized (dirtyCounters) {
            HashSet<Entry<String, Integer>> counterSet = new HashSet<Entry<String,Integer>>(dirtyCounters.entrySet());
            HashSet<CounterShard> shardSet = new HashSet<CounterShard>();
            dirtyCounters.clear();
            for (Entry<String, Integer> entry : counterSet) {
                shardSet.add(increment(entry.getKey(), entry.getValue()));
                System.out.println(String.format("[counter] increment %2d, \"%s\"", entry.getValue(), entry.getKey()));
            }
            NNF.getShardDao().saveAll(shardSet);
        }
    }
}
